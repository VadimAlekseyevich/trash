"""Лабораторная работа № 3: дерево решений для пингвинов Палмера.

Алгоритм обучается без sklearn и других готовых реализаций деревьев.
"""

from __future__ import annotations

import argparse
import csv
import json
from collections import Counter
from dataclasses import dataclass
from pathlib import Path
from statistics import median

SPECIES = ("Adelie", "Gentoo", "Chinstrap")
FEATURES = {
    "bill_length_mm": "Длина клюва, мм",
    "bill_depth_mm": "Высота клюва, мм",
    "flipper_length_mm": "Длина плавника, мм",
    "body_mass_g": "Масса тела, г",
}
VARIANTS = {
    1: ("bill_length_mm", "bill_depth_mm"),
    2: ("flipper_length_mm", "body_mass_g"),
    3: ("bill_length_mm", "flipper_length_mm"),
    4: ("bill_depth_mm", "body_mass_g"),
    5: ("bill_length_mm", "body_mass_g"),
    6: ("bill_depth_mm", "flipper_length_mm"),
    7: ("bill_length_mm", "bill_depth_mm", "flipper_length_mm"),
    8: ("bill_length_mm", "bill_depth_mm", "body_mass_g"),
    9: ("bill_length_mm", "flipper_length_mm", "body_mass_g"),
    10: ("bill_depth_mm", "flipper_length_mm", "body_mass_g"),
    11: tuple(FEATURES),
}


@dataclass(frozen=True)
class Penguin:
    row_number: int
    species: str
    values: dict[str, float]


@dataclass
class Node:
    prediction: str
    counts: dict[str, int]
    gini: float
    feature: str | None = None
    threshold: float | None = None
    gain: float = 0.0
    left: Node | None = None
    right: Node | None = None

    @property
    def size(self) -> int:
        return sum(self.counts.values())

    @property
    def is_leaf(self) -> bool:
        return self.feature is None


def read_penguins(path: Path, features: tuple[str, ...]) -> tuple[list[Penguin], int]:
    """Загружает признаки, исключая строки с NA; возвращает (данные, пропуски)."""
    samples: list[Penguin] = []
    skipped = 0
    with path.open(encoding="utf-8-sig", newline="") as source:
        reader = csv.DictReader(source)
        required = {"species", *features}
        if not required.issubset(reader.fieldnames or []):
            raise ValueError(f"В CSV отсутствуют столбцы: {sorted(required - set(reader.fieldnames or []))}")
        for row_number, row in enumerate(reader, start=2):
            if row["species"] not in SPECIES:
                raise ValueError(f"Строка {row_number}: неизвестный вид {row['species']!r}")
            if any(row[name] is None or row[name].strip().upper() in ("", "NA") for name in features):
                skipped += 1
                continue
            try:
                values = {name: float(row[name]) for name in features}
            except ValueError as exc:
                raise ValueError(f"Строка {row_number}: неверное числовое значение") from exc
            samples.append(Penguin(row_number, row["species"], values))
    if not samples:
        raise ValueError("После исключения NA не осталось наблюдений")
    return samples, skipped


def gini(counts: Counter[str] | dict[str, int]) -> float:
    """I_G = 1 - сумма(p_j ** 2)."""
    total = sum(counts.values())
    return 1.0 - sum((count / total) ** 2 for count in counts.values()) if total else 0.0


def find_best_split(samples: list[Penguin], available: tuple[str, ...]):
    """Перебор середин соседних уникальных значений и минимум взвешенного Джини.

    Классы в отсортированном массиве учитываются накопительными счётчиками:
    каждая кандидатная граница оценивается без повторного обхода всей выборки.
    """
    best = None
    total = len(samples)
    for feature in available:
        ordered = sorted((sample.values[feature], sample.species) for sample in samples)
        left_counts: Counter[str] = Counter()
        right_counts: Counter[str] = Counter(species for _, species in ordered)
        for i in range(total - 1):
            value, species = ordered[i]
            left_counts[species] += 1
            right_counts[species] -= 1
            if value == ordered[i + 1][0]:
                continue
            left_size = i + 1
            right_size = total - left_size
            weighted = (left_size * gini(left_counts) + right_size * gini(right_counts)) / total
            threshold = (value + ordered[i + 1][0]) / 2
            # При равенстве побеждает более ранний признак/порог: результат воспроизводим.
            if best is None or weighted < best[2] - 1e-12:
                best = (feature, threshold, weighted)
    return best


def build_tree(samples: list[Penguin], available: tuple[str, ...], purity: float = 0.9) -> Node:
    """Рекурсивно строит дерево. Вдоль каждой ветви признак выбирается один раз."""
    if not samples:
        raise ValueError("Нельзя построить дерево по пустой выборке")
    if not 0.0 < purity <= 1.0:
        raise ValueError("Чистота должна лежать в интервале (0; 1]")
    counts = Counter(sample.species for sample in samples)
    prediction = max(SPECIES, key=lambda species: counts[species])
    node = Node(prediction, dict(counts), gini(counts))
    if counts[prediction] / len(samples) >= purity or not available:
        return node

    best = find_best_split(samples, available)
    if best is None:
        return node
    feature, threshold, weighted = best
    gain = node.gini - weighted
    if gain <= 1e-12:
        return node

    left = [sample for sample in samples if sample.values[feature] <= threshold]
    right = [sample for sample in samples if sample.values[feature] > threshold]
    if not left or not right:
        return node
    remaining = tuple(name for name in available if name != feature)
    node.feature, node.threshold, node.gain = feature, threshold, gain
    node.left = build_tree(left, remaining, purity)
    node.right = build_tree(right, remaining, purity)
    return node


def predict(node: Node, values: dict[str, float]) -> str:
    """Классификация неизвестного пингвина по проверкам в узлах."""
    while not node.is_leaf:
        assert node.feature is not None and node.threshold is not None
        node = node.left if values[node.feature] <= node.threshold else node.right
        assert node is not None
    return node.prediction


def evaluate(tree: Node, samples: list[Penguin]) -> dict:
    """Точность на ТОМ ЖЕ наборе, что использовался для обучения (по заданию)."""
    confusion = {actual: {pred: 0 for pred in SPECIES} for actual in SPECIES}
    for sample in samples:
        confusion[sample.species][predict(tree, sample.values)] += 1
    by_species = {}
    for species in SPECIES:
        count = sum(confusion[species].values())
        correct = confusion[species][species]
        by_species[species] = {
            "total": count, "correct": correct,
            "accuracy_percent": round(100 * correct / count, 2) if count else None,
        }
    correct_total = sum(confusion[species][species] for species in SPECIES)
    return {
        "sample_count": len(samples),
        "correct": correct_total,
        "accuracy_percent": round(100 * correct_total / len(samples), 2),
        "by_species": by_species,
        "confusion_matrix": confusion,
    }


def save_predictions(path: Path, tree: Node, samples: list[Penguin], features: tuple[str, ...]):
    with path.open("w", newline="", encoding="utf-8") as target:
        writer = csv.writer(target)
        writer.writerow(("source_row", "actual_species", "predicted_species", "correct", *features))
        for sample in samples:
            result = predict(tree, sample.values)
            writer.writerow((sample.row_number, sample.species, result, int(result == sample.species),
                             *(sample.values[name] for name in features)))


def plot_tree(tree: Node, path: Path):
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    positions = {}
    leaf_index = 0

    def arrange(node: Node, depth: int) -> float:
        nonlocal leaf_index
        if node.is_leaf:
            x = leaf_index
            leaf_index += 1
        else:
            x = (arrange(node.left, depth + 1) + arrange(node.right, depth + 1)) / 2
        positions[id(node)] = (x, -depth)
        return x

    arrange(tree, 0)
    max_depth = -min(y for _, y in positions.values())
    fig, ax = plt.subplots(figsize=(max(12, leaf_index * 2.1), max(5, (max_depth + 1) * 2.2)))

    def draw(node: Node):
        x, y = positions[id(node)]
        if node.is_leaf:
            label = f"Лист: {node.prediction}\nn={node.size}; Gini={node.gini:.3f}"
        else:
            label = (f"{FEATURES[node.feature]} ≤ {node.threshold:g}\n"
                     f"n={node.size}; Gini={node.gini:.3f}; Δ={node.gain:.3f}")
            for child, side in ((node.left, "≤"), (node.right, ">")):
                cx, cy = positions[id(child)]
                ax.plot([x, cx], [y, cy], color="#65738a", linewidth=1.3, zorder=0)
                ax.text((x + cx) / 2, (y + cy) / 2, side, fontsize=9)
                draw(child)
        ax.text(x, y, label, ha="center", va="center", fontsize=9,
                bbox={"boxstyle": "round,pad=0.45", "facecolor": "#e2f0ff" if not node.is_leaf else "#e5f5e0",
                      "edgecolor": "#52637b"}, zorder=2)

    draw(tree)
    ax.set_xlim(-1, leaf_index)
    ax.set_ylim(-max_depth - 0.6, 0.7)
    ax.axis("off")
    fig.savefig(path, dpi=180, bbox_inches="tight")
    plt.close(fig)


def plot_regions(tree: Node, samples: list[Penguin], features: tuple[str, ...], path: Path):
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    import numpy as np
    from matplotlib.colors import ListedColormap

    x_name, y_name = features[:2]
    # Для 3-4 признаков показывается двумерный СРЕЗ, а не полная граница в 3D/4D.
    medians = {name: median(s.values[name] for s in samples) for name in features[2:]}
    x_values = [s.values[x_name] for s in samples]
    y_values = [s.values[y_name] for s in samples]
    x_margin = (max(x_values) - min(x_values)) * 0.06 or 1
    y_margin = (max(y_values) - min(y_values)) * 0.06 or 1
    xx, yy = np.meshgrid(np.linspace(min(x_values) - x_margin, max(x_values) + x_margin, 280),
                         np.linspace(min(y_values) - y_margin, max(y_values) + y_margin, 280))
    indexes = np.fromiter((SPECIES.index(predict(tree, {x_name: float(x), y_name: float(y), **medians}))
                           for x, y in zip(xx.flat, yy.flat)), dtype=int, count=xx.size)
    palette = ["#8bb9e9", "#eaa1ab", "#9ed3ab"]
    fig, ax = plt.subplots(figsize=(10, 7.3))
    ax.contourf(xx, yy, indexes.reshape(xx.shape), levels=[-0.5, 0.5, 1.5, 2.5],
                cmap=ListedColormap(palette), alpha=0.45)
    for i, species in enumerate(SPECIES):
        selected = [s for s in samples if s.species == species]
        ax.scatter([s.values[x_name] for s in selected], [s.values[y_name] for s in selected],
                   s=22, label=species, c=palette[i], edgecolors="#344054", linewidths=0.3)
    ax.set_xlabel(FEATURES[x_name])
    ax.set_ylabel(FEATURES[y_name])
    ax.set_title("Разделение пространства признаков деревом решений")
    if medians:
        description = "; ".join(f"{FEATURES[name]}={value:g}" for name, value in medians.items())
        ax.text(0.01, -0.16, "Цвет фона: срез при медианных значениях остальных признаков: " + description +
                "\nТочки: реальные наблюдения с их собственными значениями всех признаков.",
                transform=ax.transAxes, fontsize=9, va="top")
    ax.legend(loc="best")
    ax.grid(alpha=0.15)
    fig.savefig(path, dpi=180, bbox_inches="tight")
    plt.close(fig)


def main() -> None:
    parser = argparse.ArgumentParser(description="ЛР №3 — собственное дерево решений (Palmer penguins)")
    parser.add_argument("--variant", type=int, choices=sorted(VARIANTS), default=3,
                        help="Номер варианта из таблицы 1 методички (по умолчанию 3: длина клюва и плавника)")
    parser.add_argument("--data", type=Path, default=Path(__file__).with_name("penguins.csv"))
    parser.add_argument("--output-dir", type=Path, default=Path(__file__).with_name("output"))
    parser.add_argument("--no-plots", action="store_true", help="Не сохранять PNG-графики")
    args = parser.parse_args()

    features = VARIANTS[args.variant]
    samples, skipped = read_penguins(args.data, features)
    tree = build_tree(samples, features)
    result = evaluate(tree, samples)
    result.update({"variant": args.variant, "features": list(features), "excluded_na": skipped,
                   "purity_threshold": 0.9,
                   "evaluation_note": "Оценка на обучающей выборке согласно методичке; не test accuracy."})
    args.output_dir.mkdir(parents=True, exist_ok=True)
    (args.output_dir / "metrics.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n",
                                                  encoding="utf-8")
    save_predictions(args.output_dir / "predictions.csv", tree, samples, features)
    if not args.no_plots:
        plot_tree(tree, args.output_dir / "decision_tree.png")
        plot_regions(tree, samples, features, args.output_dir / "decision_regions.png")

    print(f"Вариант {args.variant}; признаки: {', '.join(features)}")
    print(f"Обучающих примеров: {len(samples)}; исключено строк с NA: {skipped}")
    print(f"Общая точность на обучении: {result['accuracy_percent']:.2f}%")
    for species, item in result["by_species"].items():
        print(f"  {species:9s}: {item['correct']}/{item['total']} ({item['accuracy_percent']:.2f}%)")
    print(f"Результаты сохранены: {args.output_dir.resolve()}")


if __name__ == "__main__":
    main()
