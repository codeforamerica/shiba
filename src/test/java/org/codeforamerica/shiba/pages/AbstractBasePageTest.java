package org.codeforamerica.shiba.pages;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.codeforamerica.shiba.metrics.Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.main.allow-bean-definition-overriding=true"})
@ExtendWith(SpringExtension.class)
abstract class AbstractBasePageTest {
    static protected RemoteWebDriver driver;

    protected Path path;

    protected String baseUrl;

    @LocalServerPort
    protected String localServerPort;

    protected Page testPage;

    static class MetricsTestConfigurationWithExistingStartTime {
        @Bean
        public Metrics metrics() {
            Metrics metrics = new Metrics();
            metrics.setStartTimeOnce(Instant.now());
            return metrics;
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    @TestConfiguration
    class PageInteractionTestConfiguration {
        @Bean
        public CustomScopeConfigurer customScopeConfigurer() {
            CustomScopeConfigurer configurer = new CustomScopeConfigurer();
            configurer.addScope(WebApplicationContext.SCOPE_SESSION, new SimpleThreadScope());
            return configurer;
        }
    }

    @BeforeAll
    static void beforeAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() throws IOException {
        baseUrl = String.format("http://localhost:%s", localServerPort);
        ChromeOptions options = new ChromeOptions();
        path = Files.createTempDirectory("");
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory", path.toString());
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        testPage = new Page(driver);
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    void navigateTo(String pageName) {
        driver.navigate().to(baseUrl + "/pages/" + pageName);
    }

    @SuppressWarnings("unused")
    public static void takeSnapShot(String fileWithPath) {
        TakesScreenshot screenshot = driver;
        Path sourceFile = screenshot.getScreenshotAs(OutputType.FILE).toPath();
        Path destinationFile = new File(fileWithPath).toPath();
        try {
            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
