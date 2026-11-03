# Лабораторная работа №2 — JUnit 4

**Тема:** использование JUnit для тестирования классов.

Это исходники и тесты, **не отчёт**. Реализованы три задания из присланной методички.

## Исходные данные

Взяты **все 57 сценариев T01–T57** из итогового отчёта лабораторной №1: **17 положительных, 40 отрицательных**. Данные находятся в `src/test/java/edu/lab/testing/TestCases.java`, длинные значения воспроизводятся через `String.repeat`. Это не посторонние тесты и не новый набор с другими ограничениями.

В первом отчёте заданы `isValid` и `validate`, но код тестируемого класса не прилагался. Поэтому учебный `UrlValidator` здесь реализован по правилам URL-поля из первого отчёта (проверяется синтаксис, без запросов в Интернет). Эти правила не являются универсальными ограничениями всех URL.

## Задания из методички

1. **Независимые тесты:** `IndependentUrlTest.java` содержит 57 отдельных методов `@Test`, каждый проверяет конкретный T01–T57. `AnnotationExamplesTest.java` демонстрирует `expected`, `timeout`, `@Ignore`, `Assume`, проверку конструкторов и инвариантов. Используются фикстуры `@BeforeClass`, `@Before`, `@After`, `@AfterClass`.
2. **Параметризованные тесты:** `ParameterizedUrlTest.java` повторяет те же 57 наборов через `@RunWith(Parameterized.class)` и `@Parameterized.Parameters`.
3. **Документирование событий:** `EventLogListener` обрабатывает события начала, окончания, ошибок, игнорирования и итогового результата. `LoggingRunner` регистрирует слушатель через `RunNotifier`. `RunTests` запускает весь набор через `JUnitCore` и записывает события в `target/junit-events.log`. `EventLogListenerTest` проверяет, что события действительно приходят. `AllTests` объединяет тесты через `@RunWith(Suite.class)`.

## Запуск

Требуется **JDK 17+ и Maven**. В каталоге `software_testing/lab_02_junit`:

```bash
bash run_tests.sh
```

Либо по отдельности:

```bash
mvn test
mvn -q test-compile exec:java -Dexec.mainClass=edu.lab.testing.RunTests -Dexec.classpathScope=test
```

Результаты Maven: `target/surefire-reports/`, протокол событий JUnit: `target/junit-events.log`. Один демонстрационный тест помечен `@Ignore` намеренно — он отображается как пропущенный, не как ошибка.

## Структура

```text
pom.xml
run_tests.sh
src/
  main/java/edu/lab/testing/
    UrlValidator.java
    ParsedUrl.java
    InvalidUrlException.java
  test/java/edu/lab/testing/
    TestCases.java
    UrlAssertions.java
    IndependentUrlTest.java
    ParameterizedUrlTest.java
    AnnotationExamplesTest.java
    EventLogListener.java
    EventLogListenerTest.java
    LoggingRunner.java
    AllTests.java
    RunTests.java
```

После коммита выполнение CI можно проверить во вкладке GitHub Actions. **Никакой отчёт для лабораторной №2 не создавался.**
