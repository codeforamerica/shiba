package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.ChooseProgramsPage;
import org.codeforamerica.shiba.pages.LandingPage;
import org.codeforamerica.shiba.pages.LanguagePreferencesPage;
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

    @Test
    void shouldNavigateToLanguageSelectionPage() {
        PrepareToApplyPage prepareToApplyPage = landingPage.clickPrimaryButton();
        LanguagePreferencesPage languagePreferencesPage = prepareToApplyPage.clickPrimaryButton();

        assertThat(languagePreferencesPage.getTitle()).isEqualTo("Language Preferences");
    }

    @Test
    void shouldKeepSelectionsOnLanguageSelectionPage_afterContinuing() {
        PrepareToApplyPage prepareToApplyPage = landingPage.clickPrimaryButton();
        LanguagePreferencesPage languagePreferencesPage = prepareToApplyPage.clickPrimaryButton();

        String spokenLanguage = "Soomaali";
        languagePreferencesPage.selectSpokenLanguage(spokenLanguage);
        String writtenLanguage = "Hmoob";
        languagePreferencesPage.selectWrittenLanguage(writtenLanguage);
        String needInterpreter = "No";
        languagePreferencesPage.selectNeedInterpereter(needInterpreter);
        ChooseProgramsPage chooseProgramsPage = languagePreferencesPage.submitUsingPrimaryButton();
        assertThat(chooseProgramsPage.getTitle()).isEqualTo("Choose Programs");
        languagePreferencesPage = chooseProgramsPage.goBack();

        assertThat(languagePreferencesPage.getSelectedSpokenLanguage()).isEqualTo(spokenLanguage);
        assertThat(languagePreferencesPage.getSelectedWrittenLanguage()).isEqualTo(writtenLanguage);
        assertThat(languagePreferencesPage.getNeedInterpreterSelection()).isEqualTo(needInterpreter);
    }
}
