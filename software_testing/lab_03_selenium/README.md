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

## Установка настоящего Litecart 1.3.6 на Windows (архив преподавателя)

Этот раздел основан на приложенном архиве **`litecart-1.3.6.zip`**. В нём есть `public_html/index.php`, установщик `public_html/install/` и демонстрационные данные `public_html/install/data/demo/`, включая **Purple Duck** (SQL-запись и изображение).

В комплекте также упоминались `xampp-win32-7.1.11-0-VC14-installer.exe` и `litecart.mp4`. Их содержимое не проверялось; по имени первый является установщиком старой версии XAMPP, второй — видео, которое можно использовать как дополнительную подсказку.

1. **Установи XAMPP** из предоставленного преподавателем EXE в `C:\xampp` (либо используй уже установленную локальную связку Apache + PHP + MySQL). Запусти **XAMPP Control Panel**, затем нажми **Start** для **Apache** и **MySQL**.
2. **Распакуй ZIP** и скопируй **содержимое** папки `litecart-1.3.6\public_html\` в `C:\xampp\htdocs\litecart\`. Важно: файл `C:\xampp\htdocs\litecart\index.php` должен существовать. Не копируй всю внешнюю папку `litecart-1.3.6` как `litecart`: у неё другая структура.
3. Открой `http://localhost/phpmyadmin/` → **Создать** → создай пустую БД с именем, например, `litecart`. Не импортируй SQL вручную: штатный установщик создаст таблицы.
4. Перейди на `http://localhost/litecart/install/` и заполни форму:
   - **Hostname:** `localhost`; **Database:** `litecart`; **Username:** `root`; **Password:** пусто *только если у локального MySQL XAMPP пароль не установлен*. При другом пароле введи его.
   - **Table Prefix:** оставь `lc_`.
   - **Demo Data:** **поставь галочку «Install demo data»**. Без этого товара Purple Duck может не быть.
   - **Store Name:** `My Store` (важно для точного заголовка из методички).
   - Выбери **Country**, **Time Zone**, задай свой пароль администратора и заверши установку кнопкой **Install Now**.
5. Открой **`http://localhost/litecart/`** и убедись, что магазин доступен, а товар **Purple Duck** появился в каталоге. Если установщик предложит удалить/заблокировать папку `install` после установки, выполни его рекомендацию.
6. Открой терминал в `software_testing/lab_03_selenium` (нужны Java 17+, Maven, Chrome и Firefox) и запусти тесты **на настоящем магазине**:

   ```powershell
   mvn '-DsiteUrl=http://localhost/litecart/' '-DpauseSeconds=5' test
   ```

   Должны запуститься Chrome и Firefox, перейти с главной страницы по ссылке **Purple Duck** и проверить заголовок страницы `Purple Duck | Rubber Ducks | My Store`. Скриншоты будут в `target/screenshots/`.

Если Apache использует не 80-й порт (например, 8080), замени URL во всех шагах на `http://localhost:8080/litecart/`. Если установка закончилась ошибкой, проверь, что обе службы запущены, а данные подключения к БД совпадают с настройками XAMPP.

**Безопасность:** XAMPP 7.1.11 и Litecart 1.3.6 — устаревшие учебные версии. Используй их **только локально**, не открывай порты сервера в Интернет и не храни там настоящие личные данные/пароли. Для учебного теста достаточно локального окружения.

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
