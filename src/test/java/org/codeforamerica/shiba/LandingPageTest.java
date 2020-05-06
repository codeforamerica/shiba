package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.LandingPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class LandingPageTest {

    private ChromeDriver driver;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
    }

    @Test
    void shouldShowTheLandingPage() {
        LandingPage landingPage = new LandingPage(driver);

        assertThat(landingPage.getHeader()).isEqualTo("Minnesota’s combined benefits application");
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }
}
