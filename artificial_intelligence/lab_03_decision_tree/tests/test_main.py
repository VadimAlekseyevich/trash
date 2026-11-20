"""Небольшие автоматические проверки собственного алгоритма дерева решений."""

import csv
import json
import subprocess
import tempfile
import unittest
from collections import Counter
from pathlib import Path
import sys

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from main import (Penguin, SPECIES, VARIANTS, build_tree, evaluate, find_best_split,
                  gini, predict, read_penguins, save_predictions)


class DecisionTreeTests(unittest.TestCase):
    def test_variant_two_is_default_in_cli(self):
        """Запуск без --variant должен обучать по плавнику и массе тела."""
        project_dir = Path(__file__).resolve().parents[1]
        self.assertEqual(VARIANTS[2], ("flipper_length_mm", "body_mass_g"))
        with tempfile.TemporaryDirectory() as tmp:
            output = Path(tmp)
            run = subprocess.run(
                [sys.executable, str(project_dir / "main.py"), "--no-plots",
                 "--output-dir", str(output)],
                check=True, capture_output=True, text=True,
            )
            self.assertIn("Вариант 2", run.stdout)
            metrics = json.loads((output / "metrics.json").read_text(encoding="utf-8"))
            self.assertEqual(metrics["variant"], 2)
            self.assertEqual(metrics["features"], list(VARIANTS[2]))
            self.assertEqual(metrics["sample_count"], 342)
            self.assertEqual(metrics["excluded_na"], 2)
            with (output / "predictions.csv").open(encoding="utf-8", newline="") as file:
                header = next(csv.reader(file))
            self.assertEqual(header[-2:], list(VARIANTS[2]))
            self.assertNotIn("bill_length_mm", header)
            self.assertNotIn("bill_depth_mm", header)

    def test_gini(self):
        self.assertEqual(gini(Counter({"Adelie": 5})), 0.0)
        self.assertAlmostEqual(gini(Counter({"Adelie": 1, "Gentoo": 1})), 0.5)
        self.assertAlmostEqual(gini(Counter({s: 1 for s in SPECIES})), 2 / 3)

    def test_split_between_unique_values(self):
        rows = [Penguin(i, species, {"x": x}) for i, (x, species) in
                enumerate([(1, "Adelie"), (2, "Adelie"), (4, "Gentoo"), (7, "Gentoo")])]
        self.assertEqual(find_best_split(rows, ("x",)), ("x", 3.0, 0.0))
        tree = build_tree(rows, ("x",))
        self.assertEqual(tree.threshold, 3.0)
        self.assertEqual(predict(tree, {"x": 3.0}), "Adelie")
        self.assertEqual(predict(tree, {"x": 3.1}), "Gentoo")

    def test_no_split_on_constant_feature(self):
        rows = [Penguin(i, species, {"x": 1.0}) for i, species in enumerate(SPECIES)]
        self.assertIsNone(find_best_split(rows, ("x",)))
        self.assertTrue(build_tree(rows, ("x",)).is_leaf)

    def test_stop_at_ninety_percent(self):
        rows = [Penguin(i, "Adelie" if i < 9 else "Gentoo", {"x": float(i)}) for i in range(10)]
        self.assertTrue(build_tree(rows, ("x",)).is_leaf)

    def test_feature_not_reused_on_path(self):
        path = Path(__file__).resolve().parents[1] / "penguins.csv"
        rows, _ = read_penguins(path, VARIANTS[11])
        tree = build_tree(rows, VARIANTS[11])

        def walk(node, seen):
            if node.is_leaf:
                return
            self.assertNotIn(node.feature, seen)
            walk(node.left, seen | {node.feature})
            walk(node.right, seen | {node.feature})

        walk(tree, set())

    def test_csv_data_and_all_variants(self):
        path = Path(__file__).resolve().parents[1] / "penguins.csv"
        for variant, features in VARIANTS.items():
            with self.subTest(variant=variant):
                rows, skipped = read_penguins(path, features)
                self.assertEqual((len(rows), skipped), (342, 2))
                tree = build_tree(rows, features)
                metrics = evaluate(tree, rows)
                self.assertEqual(sum(v["total"] for v in metrics["by_species"].values()), 342)
                self.assertGreaterEqual(metrics["correct"], 152)
                self.assertEqual(metrics["correct"], sum(v["correct"] for v in metrics["by_species"].values()))

    def test_predictions_csv(self):
        rows = [Penguin(2, "Adelie", {"x": 1.0}), Penguin(3, "Gentoo", {"x": 2.0})]
        tree = build_tree(rows, ("x",), purity=1.0)
        with tempfile.TemporaryDirectory() as temp:
            destination = Path(temp) / "predictions.csv"
            save_predictions(destination, tree, rows, ("x",))
            self.assertEqual(len(destination.read_text(encoding="utf-8").splitlines()), 3)


if __name__ == "__main__":
    unittest.main()
