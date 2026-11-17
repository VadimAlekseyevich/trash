package edu.lab.testing;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** First Selenium WebDriver test from lab #3, repeated for Chrome and Firefox. */
class LitecartSeleniumTest {
    private static final String EXPECTED_TITLE = "Purple Duck | Rubber Ducks | My Store";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private static DemoLitecartServer demoSite;
    private static String siteUrl;

    @BeforeAll
    static void startSite() throws IOException {
        String requestedUrl = System.getProperty("siteUrl", "demo").trim();
        if ("demo".equalsIgnoreCase(requestedUrl)) {
            demoSite = DemoLitecartServer.start();
            siteUrl = demoSite.url();
            System.out.println("Local demo (not real Litecart): " + siteUrl);
        } else if (requestedUrl.startsWith("http://") || requestedUrl.startsWith("https://")) {
            siteUrl = requestedUrl.endsWith("/") ? requestedUrl : requestedUrl + "/";
            System.out.println("External Litecart: " + siteUrl);
        } else {
            throw new IllegalArgumentException("siteUrl must be 'demo' or an HTTP(S) URL");
        }
    }

    @AfterAll
    static void stopSite() {
        if (demoSite != null) {
            demoSite.close();
        }
    }

    @Test
    @DisplayName("Chrome: открыть товар Purple Duck")
    void chromeOpensPurpleDuck() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,900");
        if (isHeadless()) {
            options.addArguments("--headless=new");
        }
        String binary = System.getProperty("chromeBinary", "");
        if (!binary.isBlank()) {
            options.setBinary(binary);
        }
        verifyPurpleDuck(new ChromeDriver(options), "chrome");
    }

    @Test
    @DisplayName("Firefox: открыть товар Purple Duck")
    void firefoxOpensPurpleDuck() {
        FirefoxOptions options = new FirefoxOptions();
        if (isHeadless()) {
            options.addArguments("-headless");
        }
        String binary = System.getProperty("firefoxBinary", "");
        if (!binary.isBlank()) {
            options.setBinary(binary);
        }
        verifyPurpleDuck(new FirefoxDriver(options), "firefox");
    }

    private static void verifyPurpleDuck(WebDriver driver, String browser) {
        try {
            driver.get(siteUrl);
            WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
            WebElement product = wait.until(
                    ExpectedConditions.elementToBeClickable(By.partialLinkText("Purple Duck")));
            product.click();
            wait.until(ExpectedConditions.titleIs(EXPECTED_TITLE));
            assertEquals(EXPECTED_TITLE, driver.getTitle());
            saveScreenshot(driver, browser);
            pauseToInspect();
        } catch (RuntimeException | AssertionError failure) {
            saveScreenshot(driver, browser + "-failure");
            throw failure;
        } finally {
            driver.quit();
        }
    }

    private static boolean isHeadless() {
        return Boolean.parseBoolean(System.getProperty("headless", "false"));
    }

    private static void saveScreenshot(WebDriver driver, String filename) {
        try {
            Path directory = Path.of("target", "screenshots");
            Files.createDirectories(directory);
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path file = directory.resolve(filename + ".png");
            Files.write(file, png);
            System.out.println("Screenshot: " + file.toAbsolutePath());
        } catch (Exception screenshotError) {
            System.err.println("Unable to save screenshot: " + screenshotError.getMessage());
        }
    }

    private static void pauseToInspect() {
        int seconds = Integer.parseInt(System.getProperty("pauseSeconds", "0"));
        if (seconds < 0 || seconds > 60) {
            throw new IllegalArgumentException("pauseSeconds must be between 0 and 60");
        }
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during pause", interruption);
        }
    }
}
