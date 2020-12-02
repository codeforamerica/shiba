package org.codeforamerica.shiba;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.pages.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public abstract class AbstractBasePageTest {
    static protected RemoteWebDriver driver;

    protected Path path;

    protected String baseUrl;

    @LocalServerPort
    protected String localServerPort;

    protected Page testPage;

    @BeforeAll
    static void beforeAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    protected void setUp() throws IOException {
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

    protected void navigateTo(String pageName) {
        driver.navigate().to(baseUrl + "/pages/" + pageName);
    }

    protected String getPdfFieldText(PDAcroForm pdAcroForm, String fieldName) {
        return pdAcroForm.getField(fieldName).getValueAsString();
    }

    protected Optional<File> getCafFile() {
        return Arrays.stream(path.toFile().listFiles())
                .filter(file -> file.getName().contains("_CAF_") && file.getName().endsWith(".pdf"))
                .findFirst();
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
