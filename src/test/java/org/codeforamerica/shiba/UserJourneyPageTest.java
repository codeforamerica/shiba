package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.pages.DatePart.*;

public class UserJourneyPageTest extends AbstractBasePageTest {

    private LandingPage landingPage;

    @TestConfiguration
    static class TestConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(), ZoneId.of("UTC"));
        }
    }

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


            ssn = "7234567890";
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
        contactInfoPage.enterPhoneNumber("7234567890");

        ThanksPage thanksPage = contactInfoPage.clickPrimaryButton();
        WeDoNotRecommendMinimalFlowPage weDoNotRecommendMinimalFlowPage = thanksPage.clickSubtleLink();
        assertThat(weDoNotRecommendMinimalFlowPage.getTitle()).isEqualTo("We do not recommend minimal flow");
    }

    @Test
    void shouldNavigateToHomeAddressPage() {
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
        contactInfoPage.enterPhoneNumber("7234567890");

        HomeAddressPage homeAddressPage = contactInfoPage.clickPrimaryButton().clickSubtleLink().clickSubtleLink();

        assertThat(homeAddressPage.getTitle()).isEqualTo("Home address");
    }

    @Test
    void shouldNavigateToMailingPageAfterCompletingHomeAddress() {
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
        contactInfoPage.enterPhoneNumber("7234567890");

        HomeAddressPage homeAddressPage = contactInfoPage
                .clickPrimaryButton()
                .clickSubtleLink()
                .clickSubtleLink();

        homeAddressPage.enterInput("zipCode", "12345");
        homeAddressPage.enterInput("city", "someCity");
        homeAddressPage.enterInput("streetAddress", "someStreetAddress");
        MailingAddressPage mailingAddressPage = homeAddressPage.clickPrimaryButton();

        assertThat(mailingAddressPage.getTitle()).isEqualTo("Mailing address");
    }

    @Test
    void shouldNavigateToSignThisApplicationPageAfterMailingAddress() {
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
        contactInfoPage.enterPhoneNumber("7234567890");

        HomeAddressPage homeAddressPage = contactInfoPage
                .clickPrimaryButton()
                .clickSubtleLink()
                .clickSubtleLink();

        homeAddressPage.enterInput("zipCode", "12345");
        homeAddressPage.enterInput("city", "someCity");
        homeAddressPage.enterInput("streetAddress", "someStreetAddress");
        MailingAddressPage mailingAddressPage = homeAddressPage.clickPrimaryButton();

        mailingAddressPage.enterInput("zipCode", "12345");
        mailingAddressPage.enterInput("city", "someCity");
        mailingAddressPage.enterInput("streetAddress", "someStreetAddress");

        SignThisApplicationPage signThisApplicationPage = mailingAddressPage.clickPrimaryButton();
        assertThat(signThisApplicationPage.getTitle()).isEqualTo("Sign this application");
    }

    @Test
    void shouldGoToSuccessPageAfterSignThisApplicationPage() {
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
        contactInfoPage.enterPhoneNumber("7234567890");

        HomeAddressPage homeAddressPage = contactInfoPage
                .clickPrimaryButton()
                .clickSubtleLink()
                .clickSubtleLink();

        homeAddressPage.enterInput("zipCode", "12345");
        homeAddressPage.enterInput("city", "someCity");
        homeAddressPage.enterInput("streetAddress", "someStreetAddress");
        MailingAddressPage mailingAddressPage = homeAddressPage.clickPrimaryButton();

        mailingAddressPage.enterInput("zipCode", "12345");
        mailingAddressPage.enterInput("city", "someCity");
        mailingAddressPage.enterInput("streetAddress", "someStreetAddress");

        SignThisApplicationPage signThisApplicationPage = mailingAddressPage.clickPrimaryButton();

        signThisApplicationPage.enterInput("applicantSignature", "some name");
        SuccessPage successPage = signThisApplicationPage.clickPrimaryButton();

        assertThat(successPage.getTitle()).isEqualTo("Success");
        assertThat(successPage.getSubmissionTime()).contains("January 1, 2020");
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
        contactInfoPage.enterPhoneNumber("7234567890");
        HomeAddressPage homeAddressPage = contactInfoPage
                .clickPrimaryButton()
                .clickSubtleLink()
                .clickSubtleLink();
        homeAddressPage.enterInput("zipCode", "12345");
        homeAddressPage.enterInput("city", "someCity");
        homeAddressPage.enterInput("streetAddress", "someStreetAddress");

        MailingAddressPage mailingAddressPage = homeAddressPage.clickPrimaryButton();

        mailingAddressPage.enterInput("zipCode", "12345");
        mailingAddressPage.enterInput("city", "someCity");
        mailingAddressPage.enterInput("streetAddress", "someStreetAddress");

        SignThisApplicationPage signThisApplicationPage = mailingAddressPage.clickPrimaryButton();

        signThisApplicationPage.enterInput("applicantSignature", "some name");
        SuccessPage successPage = signThisApplicationPage.clickPrimaryButton();

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
        contactInfoPage.enterPhoneNumber("7234567890");
        HomeAddressPage homeAddressPage = contactInfoPage
                .clickPrimaryButton()
                .clickSubtleLink()
                .clickSubtleLink();
        homeAddressPage.enterInput("zipCode", "12345");
        homeAddressPage.enterInput("city", "someCity");
        homeAddressPage.enterInput("streetAddress", "someStreetAddress");

        MailingAddressPage mailingAddressPage = homeAddressPage.clickPrimaryButton();

        mailingAddressPage.enterInput("zipCode", "12345");
        mailingAddressPage.enterInput("city", "someCity");
        mailingAddressPage.enterInput("streetAddress", "someStreetAddress");

        SignThisApplicationPage signThisApplicationPage = mailingAddressPage.clickPrimaryButton();

        signThisApplicationPage.enterInput("applicantSignature", "some name");
        SuccessPage successPage = signThisApplicationPage.clickPrimaryButton();

        successPage.downloadXML();
        await().until(() -> path.resolve("ApplyMN.xml").toFile().exists());
    }
}
