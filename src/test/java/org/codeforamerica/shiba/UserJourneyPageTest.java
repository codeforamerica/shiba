package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.DatePart.*;

public class UserJourneyPageTest extends AbstractBasePageTest {

    private LandingPage landingPage;

    @Override
    @BeforeEach
    void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl + "/pages/landing");
        landingPage = new LandingPage(super.driver);
    }

    @Test
    void shouldShowTheLandingPage() {
        assertThat(landingPage.getTitle()).isEqualTo("Landing Page");
    }

    @Test
    void shouldNavigateToPrepareToApplyPage() {
        IntermediaryPage<LandingPage, LanguagePreferencesPage> prepareToApplyPage = landingPage.clickPrimaryButton();

        assertThat(prepareToApplyPage.getTitle()).isEqualTo("Prepare To Apply");
    }

    @Test
    void shouldNavigateBackToTheLandingPage() {
        IntermediaryPage<LandingPage, LanguagePreferencesPage> prepareToApplyPage = landingPage.clickPrimaryButton();
        LandingPage landingPage = prepareToApplyPage.goBack();

        assertThat(landingPage.getTitle()).isEqualTo("Landing Page");
    }

    @Test
    void shouldNavigateToLanguageSelectionPage() {
        LanguagePreferencesPage languagePreferencesPage = landingPage.clickPrimaryButton().clickPrimaryButton();

        assertThat(languagePreferencesPage.getTitle()).isEqualTo("Language Preferences");
    }

    @Test
    void shouldKeepSelectionsOnLanguageSelectionPage_afterContinuing() {
        LanguagePreferencesPage languagePreferencesPage = landingPage.clickPrimaryButton().clickPrimaryButton();

        String spokenLanguage = "Soomaali";
        languagePreferencesPage.selectSpokenLanguage(spokenLanguage);
        String writtenLanguage = "Hmoob";
        languagePreferencesPage.selectWrittenLanguage(writtenLanguage);
        String needInterpreter = "No";
        languagePreferencesPage.selectNeedInterpreter(needInterpreter);
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
            ChooseProgramsPage chooseProgramsPage = landingPage
                    .clickPrimaryButton()
                    .clickPrimaryButton()
                    .clickPrimaryButton();

            String cashPrograms = "Cash programs";
            chooseProgramsPage.chooseProgram(cashPrograms);
            String emergencyAssistance = "Emergency assistance";
            chooseProgramsPage.chooseProgram(emergencyAssistance);

            HowItWorksPage howItWorks = chooseProgramsPage.clickPrimaryButton();
            chooseProgramsPage = howItWorks.goBack();

            List<String> selectedPrograms = chooseProgramsPage.selectedPrograms();
            assertThat(selectedPrograms).containsOnly(cashPrograms, emergencyAssistance);
        }

        @Test
        void shouldFailValidationIfNoProgramIsSelected() {
            ChooseProgramsPage chooseProgramsPage = landingPage.clickPrimaryButton()
                    .clickPrimaryButton()
                    .clickPrimaryButton();

            chooseProgramsPage.clickPrimaryButton();

            assertThat(chooseProgramsPage.hasError()).isTrue();
        }

        @Test
        void shouldClearValidationErrorWhenUserSelectsAtLeastOneProgram() {
            ChooseProgramsPage chooseProgramsPage = landingPage
                    .clickPrimaryButton()
                    .clickPrimaryButton()
                    .clickPrimaryButton();

            chooseProgramsPage.clickPrimaryButton();
            chooseProgramsPage.chooseProgram("Emergency assistance");
            HowItWorksPage howItWorksPage = chooseProgramsPage.clickPrimaryButton();
            chooseProgramsPage = howItWorksPage.goBack();

            assertThat(chooseProgramsPage.getTitle()).isEqualTo("Choose Programs");
            assertThat(chooseProgramsPage.hasError()).isFalse();
        }
    }

    @Test
    void shouldDisplayUserSelectedOneProgram() {
        ChooseProgramsPage chooseProgramsPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramsPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramsPage.clickPrimaryButton();

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
        IntermediaryPage<HowItWorksPage, PersonalInfoPage> page = chooseProgramPage.clickPrimaryButton().clickPrimaryButton();

        assertThat(page.getTitle()).isEqualTo("Intro: Basic Info");
    }

    @Nested
    class PersonalInfo {
        PersonalInfoPage page;

        @BeforeEach
        void setup() {
            ChooseProgramsPage chooseProgramPage = landingPage
                    .clickPrimaryButton()
                    .clickPrimaryButton()
                    .clickPrimaryButton();
            chooseProgramPage.chooseProgram("Emergency assistance");
            HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();
            page = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
            page.enterFirstName("defaultFirstName");
            page.enterLastName("defaultLastName");
        }

        @Test
        void shouldNavigateToThePersonalInfoScreen() {
            assertThat(page.getTitle()).isEqualTo("Personal Info");
        }

        @Test
        void shouldPreservePersonalInformation() {
            String firstName = "John";
            page.enterFirstName(firstName);
            String lastName = "Doe";
            page.enterLastName(lastName);
            String otherName = "Jane Doe";
            page.enterOtherName(otherName);
            String birthMonth = "12";
            page.enterBirthDate(MONTH, birthMonth);
            String birthDay = "07";
            page.enterBirthDate(DAY, birthDay);
            String birthYear = "1999";
            page.enterBirthDate(YEAR, birthYear);
            String ssn = "123456789";
            page.enterSSN(ssn);

            page.clickPrimaryButton();
            driver.navigate().back();

            assertThat(page.getFirstNameValue()).isEqualTo(firstName);
            assertThat(page.getLastNameValue()).isEqualTo(lastName);
            assertThat(page.getOtherNameValue()).isEqualTo(otherName);
            assertThat(page.getBirthDateValue(MONTH)).isEqualTo(birthMonth);
            assertThat(page.getBirthDateValue(DAY)).isEqualTo(birthDay);
            assertThat(page.getBirthDateValue(YEAR)).isEqualTo(birthYear);
            assertThat(page.getSsnValue()).isEqualTo(ssn);
        }

        @Test
        void shouldRequireExactly9DigitsOrEmptyStringForSSN() {
            String firstName = "John";
            page.enterFirstName(firstName);
            String lastName = "Doe";
            page.enterLastName(lastName);
            String ssn = "12345678";
            page.enterSSN(ssn);
            page.clickPrimaryButton();

            assertThat(page.getTitle()).isEqualTo("Personal Info");
            assertThat(page.hasSSNError()).isTrue();


            ssn = "1234567890";
            page.enterSSN(ssn);
            page.clickPrimaryButton();

            assertThat(page.getTitle()).isEqualTo("Personal Info");
            assertThat(page.hasSSNError()).isTrue();


            ssn = "";
            page.enterSSN(ssn);
            page.clickPrimaryButton();

            assertThat(page.getTitle()).isEqualTo("Contact Info");
        }

        @Test
        void shouldStayOnThePageAndIncludeAnErrorWhenFirstNameIsBlank() {
            page.enterFirstName(" ");
            page.enterLastName("a");
            page.clickPrimaryButton();
            assertThat(page.getTitle()).isEqualTo("Personal Info");
            assertThat(page.hasFirstNameError()).isTrue();
        }

        @Test
        void shouldStayOnThePageAndIncludeAnErrorWhenLastNameIsBlank() {
            page.enterFirstName("a");
            page.enterLastName("  ");
            page.clickPrimaryButton();
            assertThat(page.getTitle()).isEqualTo("Personal Info");
            assertThat(page.hasLastNameError()).isTrue();
        }

        @Test
        void shouldPreserveMaritalStatus() {
            String maritalStatus = "Never married";
            page.selectMaritalStatus(maritalStatus);
            page.clickPrimaryButton();
            driver.navigate().back();

            assertThat(page.getMaritalStatus()).isEqualTo(maritalStatus);
        }

        @Test
        void shouldPreserveSex() {
            String sex = "Male";
            page.selectSex(sex);
            page.clickPrimaryButton();
            driver.navigate().back();

            assertThat(page.getSex()).isEqualTo(sex);
        }

        @Nested
        class LivedInMNWholeLife {
            @Test
            void shouldPreserveSelection() {
                page.movedToMNWithinLastYear("Yes");
                page.clickPrimaryButton();
                driver.navigate().back();

                assertThat(page.getLivedInMNWholeLife()).isEqualTo("Yes");
            }

            @Test
            void shouldAskForMoveDateAndPreviousCityIfMovedToMNWithinLastYear() {
                page.movedToMNWithinLastYear("Yes");

                assertThat(page.displaysAllMoveToMNInputs()).isTrue();
                String month = "03";
                page.enterMoveToMNDatePart(MONTH, month);
                String day = "16";
                page.enterMoveToMNDatePart(DAY, day);
                String year = "2020";
                page.enterMoveToMNDatePart(YEAR, year);
                String city = "Chicago";
                page.enterPreviousCity(city);
                page.clickPrimaryButton();
                driver.navigate().back();

                assertThat(page.getMoveToMNDate(MONTH)).isEqualTo(month);
                assertThat(page.getMoveToMNDate(DAY)).isEqualTo(day);
                assertThat(page.getMoveToMNDate(YEAR)).isEqualTo(year);
                assertThat(page.getPreviousCity()).isEqualTo(city);
            }

            @Test
            void shouldNotAskForMoveDateIfDidNotMoveToMNWithinLastYear() {
                page.movedToMNWithinLastYear("No");

                assertThat(page.displaysNoMoveToMNInputs()).isTrue();
            }
        }
    }

    @Test
    void shouldNavigateToContactInfoScreen() {
        ChooseProgramsPage chooseProgramPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();
        PersonalInfoPage personalInfoPage = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
        personalInfoPage.enterFirstName("defaultFirstName");
        personalInfoPage.enterLastName("defaultLastName");
        personalInfoPage.enterSSN("000000000");
        ContactInfoPage contactInfoPage = personalInfoPage.clickPrimaryButton();

        assertThat(contactInfoPage.getTitle()).isEqualTo("Contact Info");
    }

    @Test
    void shouldFailValidationForContactInfo_whenEmailMeIsChecked_andEmailIsBlank() {
        ChooseProgramsPage chooseProgramPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();
        PersonalInfoPage personalInfoPage = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
        personalInfoPage.enterFirstName("defaultFirstName");
        personalInfoPage.enterLastName("defaultLastName");
        personalInfoPage.enterSSN("000000000");

        ContactInfoPage contactInfoPage = personalInfoPage.clickPrimaryButton();

        contactInfoPage.enterPhoneNumber("123");
        contactInfoPage.selectContactChoice("Email me");

        contactInfoPage.clickPrimaryButton();
        assertThat(contactInfoPage.getTitle()).isEqualTo("Contact Info");
        assertThat(contactInfoPage.hasEmailError()).isTrue();
    }

    @Test
    void shouldPassValidationForContactInfo_whenEmailMeIsNotChecked_andEmailIsBlank() {
        ChooseProgramsPage chooseProgramPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();
        PersonalInfoPage personalInfoPage = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
        personalInfoPage.enterFirstName("defaultFirstName");
        personalInfoPage.enterLastName("defaultLastName");
        personalInfoPage.enterSSN("000000000");

        ContactInfoPage contactInfoPage = personalInfoPage.clickPrimaryButton();

        contactInfoPage.enterPhoneNumber("123");

        SuccessPage successPage = contactInfoPage.clickPrimaryButton().clickPrimaryButton();
        assertThat(successPage.getTitle()).isEqualTo("Thanks");
    }

    @Test
    void shouldPassValidationForContactInfo_whenEmailMeIsChecked_andEmailIsNotBlank() {
        ChooseProgramsPage chooseProgramPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();
        PersonalInfoPage personalInfoPage = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
        personalInfoPage.enterFirstName("defaultFirstName");
        personalInfoPage.enterLastName("defaultLastName");
        personalInfoPage.enterSSN("000000000");

        ContactInfoPage contactInfoPage = personalInfoPage.clickPrimaryButton();
        contactInfoPage.enterPhoneNumber("123");
        contactInfoPage.enterEmail("something");
        contactInfoPage.selectContactChoice("Email me");

        ThanksPage thanksPage = contactInfoPage.clickPrimaryButton();
        assertThat(thanksPage.getTitle()).isEqualTo("Thanks");
    }

    @Test
    void shouldNavigateToWeDoNotRecommendMinimalFlowPage() {
        ChooseProgramsPage chooseProgramPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();
        PersonalInfoPage personalInfoPage = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
        personalInfoPage.enterFirstName("defaultFirstName");
        personalInfoPage.enterLastName("defaultLastName");

        ContactInfoPage contactInfoPage = personalInfoPage.clickPrimaryButton();
        contactInfoPage.enterPhoneNumber("123");

        ThanksPage thanksPage = contactInfoPage.clickPrimaryButton();
        WeDoNotRecommendMinimalFlowPage weDoNotRecommendMinimalFlowPage = thanksPage.clickSubtleLink();
        assertThat(weDoNotRecommendMinimalFlowPage.getTitle()).isEqualTo("We do not recommend minimal flow");
    }

    @Test
    void shouldDownloadPDFWhenClickDownloadMyReceipt() {
        ChooseProgramsPage chooseProgramPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();
        PersonalInfoPage personalInfoPage = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
        personalInfoPage.enterFirstName("defaultFirstName");
        personalInfoPage.enterLastName("defaultLastName");
        personalInfoPage.enterSSN("000000000");
        ContactInfoPage contactInfoPage = personalInfoPage.clickPrimaryButton();
        contactInfoPage.enterPhoneNumber("123");
        SuccessPage successPage = contactInfoPage
                .clickPrimaryButton()
                .clickSubtleLink()
                .clickSubtleLink();

        successPage.downloadReceipt();
        await().until(() -> path.resolve("DHS-5223.pdf").toFile().exists());
    }

    @Test
    void shouldDownloadXMLWhenClickDownloadXML() {
        ChooseProgramsPage chooseProgramPage = landingPage
                .clickPrimaryButton()
                .clickPrimaryButton()
                .clickPrimaryButton();
        chooseProgramPage.chooseProgram("Emergency assistance");
        HowItWorksPage howItWorksPage = chooseProgramPage.clickPrimaryButton();
        PersonalInfoPage personalInfoPage = howItWorksPage.clickPrimaryButton().clickPrimaryButton();
        personalInfoPage.enterFirstName("defaultFirstName");
        personalInfoPage.enterLastName("defaultLastName");
        personalInfoPage.enterSSN("000000000");
        ContactInfoPage contactInfoPage = personalInfoPage.clickPrimaryButton();
        contactInfoPage.enterPhoneNumber("123");
        SuccessPage successPage = contactInfoPage
                .clickPrimaryButton()
                .clickSubtleLink()
                .clickSubtleLink();

        successPage.downloadXML();
        await().until(() -> path.resolve("ApplyMN.xml").toFile().exists());
    }
}
