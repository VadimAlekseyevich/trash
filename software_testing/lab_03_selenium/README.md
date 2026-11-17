# Лабораторная работа №3 — Selenium WebDriver (Chrome и Firefox)

**Тема по присланной методичке:** настройка инфраструктуры автоматизированного тестирования на базе Selenium WebDriver. Номер лабораторной в некоторых материалах может отличаться.

## Что сделано

- Отдельный Maven-проект на Java 17, Selenium 4.38.0 и JUnit 5.
- Два теста: `chromeOpensPurpleDuck` и `firefoxOpensPurpleDuck`.
- Оба теста открывают страницу магазина, нажимают на ссылку **Purple Duck** и ожидают заголовок **`Purple Duck | Rubber Ducks | My Store`**, как в методичке.
- Тесты делают скриншоты страницы результата в `target/screenshots/chrome.png` и `firefox.png` (при ошибке — `*-failure.png`).
- Поддерживаются Chrome и Firefox, обычный и фоновый (`headless`) режимы.
- Selenium Manager из Selenium 4 автоматически подбирает `chromedriver` и `geckodriver`: вручную копировать драйверы в папку не нужно. При первом запуске требуется доступ к интернету для зависимостей Maven и, при необходимости, драйверов.
- GitHub Actions запускает оба теста в Linux и сохраняет скриншоты как артефакт.

## Быстрый запуск без установки Litecart

Для проверки инфраструктуры в проект включён маленький **локальный демосайт**, имитирующий каталог и карточку Purple Duck. Это **не настоящий Litecart**: он нужен только для первого запуска Selenium без Apache, PHP и базы данных. По умолчанию тесты работают именно с ним.

Потребуются **JDK 17+**, **Maven**, **Google Chrome** и **Mozilla Firefox**. Выполните из корня клонированного репозитория:

```bash
cd software_testing/lab_03_selenium
mvn test -DpauseSeconds=5
```

Запустятся оба браузера по очереди: откроется магазин, тест нажмёт Purple Duck, проверит заголовок и задержит окно на 5 секунд, чтобы можно было увидеть результат. Затем браузер закроется автоматически.

Чтобы проверить без открытия окон:

```bash
mvn test -Dheadless=true
```

На Linux/macOS также есть `bash run_tests.sh` (по умолчанию headless).

### Только один браузер

**Chrome:**

```bash
mvn '-Dtest=LitecartSeleniumTest#chromeOpensPurpleDuck' -DpauseSeconds=5 test
```

**Firefox:**

```bash
mvn '-Dtest=LitecartSeleniumTest#firefoxOpensPurpleDuck' -DpauseSeconds=5 test
```

В PowerShell можно использовать те же команды. Для старых версий PowerShell аргументы `-D...` при необходимости заключайте в одинарные кавычки, как показано для `-Dtest`.

## Запуск на настоящем Litecart (вариант из методички)

В методичке тестируется сайт **`http://localhost/litecart/`**. Для этой проверки нужен **уже развёрнутый настоящий Litecart** (например, локально через Apache/PHP/MySQL). Проверьте, что главная страница открывается в браузере и на ней есть ссылка на товар **Purple Duck**.

После запуска Litecart:

```bash
mvn test -DsiteUrl=http://localhost/litecart/ -DpauseSeconds=5
```

При необходимости замените адрес, если сайт работает на другом хосте/порту. В настоящем Litecart название товара и `<title>` могут быть изменены настройками магазина. В тесте проверяется точный заголовок из методички; при другом заголовке скорректируйте константу `EXPECTED_TITLE` в `LitecartSeleniumTest.java`.

> Важно: автоматический демосайт проверяет корректность настройки Selenium и сценарий перехода. Только запуск с `-DsiteUrl` на настоящем Litecart проверяет реальный магазин.

## Результаты и управление запуском

| Параметр | Значение |
| --- | --- |
| `-Dheadless=true` | Без видимых окон браузера, удобно для CI |
| `-DpauseSeconds=5` | Оставить открытой карточку товара на 5 секунд после проверки (максимум 60) |
| `-DsiteUrl=http://localhost/litecart/` | Проверять настоящий Litecart вместо демосайта |
| `-DchromeBinary=/path/to/chromium` | При необходимости указать исполняемый файл Chromium/Chrome |
| `-DfirefoxBinary=/path/to/firefox` | При необходимости указать путь к Firefox |

После запуска:

- **Скриншоты:** `target/screenshots/chrome.png` и `target/screenshots/firefox.png`.
- **JUnit-отчёты:** `target/surefire-reports/` (текстовые и XML-файлы).
- **Успешное завершение:** Maven покажет `Tests run: 2, Failures: 0, Errors: 0` и `BUILD SUCCESS`.
- **GitHub:** вкладка **Actions** → workflow **Lab 03 - Selenium Chrome and Firefox** → артефакт `lab03-selenium-results`.

Если Selenium Manager не находит драйвер, проверьте установленный браузер и доступ к интернету. Если тест запускается на сервере без графического окружения, добавьте `-Dheadless=true`. Если тест на настоящем Litecart не находит Purple Duck, проверьте наличие товара на главной странице и правильность `siteUrl`.

## Файлы

```text
lab_03_selenium/
├── pom.xml
├── run_tests.sh
├── src/test/java/edu/lab/testing/
│   ├── DemoLitecartServer.java     # необязательный локальный стенд
│   └── LitecartSeleniumTest.java   # Chrome + Firefox, сценарий из методички
└── src/test/resources/demo/
    ├── index.html
    └── purple-duck.html
```
