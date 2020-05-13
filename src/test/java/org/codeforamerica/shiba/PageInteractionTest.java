package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PageInteractionTest extends AbstractBasePageTest {

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
        ChooseProgramsPage chooseProgramsPage = languagePreferencesPage.clickPrimaryButton();
        assertThat(chooseProgramsPage.getTitle()).isEqualTo("Choose Programs");
        languagePreferencesPage = chooseProgramsPage.goBack();

        assertThat(languagePreferencesPage.getSelectedSpokenLanguage()).isEqualTo(spokenLanguage);
        assertThat(languagePreferencesPage.getSelectedWrittenLanguage()).isEqualTo(writtenLanguage);
        assertThat(languagePreferencesPage.getNeedInterpreterSelection()).isEqualTo(needInterpreter);
    }

    @Nested
    class ProgramSelectionPage {
        @Test
        void shouldKeepProgramSelectionAfterContinuing() {
            PrepareToApplyPage prepareToApplyPage = landingPage.clickPrimaryButton();
            LanguagePreferencesPage languagePreferencesPage = prepareToApplyPage.clickPrimaryButton();
            ChooseProgramsPage page = languagePreferencesPage.clickPrimaryButton();

            String cashPrograms = "Cash programs";
            page.chooseProgram(cashPrograms);
            String emergencyAssistance = "Emergency assistance";
            page.chooseProgram(emergencyAssistance);

            HowItWorksPage howItWorks = page.clickPrimaryButton();
            ChooseProgramsPage chooseProgramsPage = howItWorks.goBack();

            List<String> selectedPrograms = chooseProgramsPage.selectedPrograms();
            assertThat(selectedPrograms).containsOnly(cashPrograms, emergencyAssistance);
        }

        @Test
        void shouldFailValidationIfNoProgramIsSelected() {
            PrepareToApplyPage prepareToApplyPage = landingPage.clickPrimaryButton();
            LanguagePreferencesPage languagePreferencesPage = prepareToApplyPage.clickPrimaryButton();
            ChooseProgramsPage page = languagePreferencesPage.clickPrimaryButton();

            page.clickPrimaryButton();

            assertThat(page.hasError()).isTrue();
        }

        @Test
        void shouldClearValidationErrorWhenUserSelectsAtLeastOneProgram() {
            PrepareToApplyPage prepareToApplyPage = landingPage.clickPrimaryButton();
            LanguagePreferencesPage languagePreferencesPage = prepareToApplyPage.clickPrimaryButton();
            ChooseProgramsPage page = languagePreferencesPage.clickPrimaryButton();

            page.clickPrimaryButton();
            page.chooseProgram("Emergency assistance");
            HowItWorksPage howItWorksPage = page.clickPrimaryButton();
            ChooseProgramsPage chooseProgramsPage = howItWorksPage.goBack();

            assertThat(chooseProgramsPage.getTitle()).isEqualTo("Choose Programs");
            assertThat(chooseProgramsPage.hasError()).isFalse();
        }
    }

    @Test
    void shouldDisplayUserSelectedOneProgram() {
        PrepareToApplyPage prepareToApplyPage = landingPage.clickPrimaryButton();
        LanguagePreferencesPage languagePreferencesPage = prepareToApplyPage.clickPrimaryButton();
        ChooseProgramsPage chooseProgramPage = languagePreferencesPage.clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();

        assertThat(howItWorksPage.getTitle()).isEqualTo("How It Works");
        assertThat(howItWorksPage.headerIncludesProgram("emergency")).isTrue();
    }

    @Test
    void shouldNavigateToTheBasicInfoScreen() {
        ChooseProgramsPage chooseProgramPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        IntermediaryPage<HowItWorksPage, TestFinalPage> page = chooseProgramPage.clickPrimaryButton().clickPrimaryButton();

        assertThat(page.getTitle()).isEqualTo("Intro: Basic Info");
    }
}
