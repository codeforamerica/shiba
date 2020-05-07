package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.LandingPage;
import org.codeforamerica.shiba.pages.PrepareToApplyPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PageInteractionTest extends BasePageTest {

    private LandingPage landingPage;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        String baseUrl = String.format("http://localhost:%s", localServerPort);
        driver.navigate().to(baseUrl + "/");
        landingPage = new LandingPage(super.driver);
    }

    @Test
    void shouldShowTheLandingPage() {
        assertThat(landingPage.getTitle()).isEqualTo("Landing Page");
    }

    @Test
    void shouldNavigateToPrepareToApplyPage() {
        PrepareToApplyPage prepareToApplyPage = landingPage.clickPrimaryButton();

        assertThat(prepareToApplyPage.getTitle()).isEqualTo("Prepare To Apply");
    }

    @Test
    void shouldNavigateBackToTheLandingPage() {
        PrepareToApplyPage prepareToApplyPage = landingPage.clickPrimaryButton();
        LandingPage landingPage = prepareToApplyPage.goBack();

        assertThat(landingPage.getTitle()).isEqualTo("Landing Page");
    }
}
