package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService;
import org.codeforamerica.shiba.pages.SuccessMessageService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.Program.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = NONE)
class EmailContentCreatorTest {
    @Autowired
    private EmailContentCreator emailContentCreator;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SuccessMessageService successMessageService;

    @Autowired
    private DocRecommendationMessageService docRecommendationMessageService;

    private List<String> programs;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        emailContentCreator = new EmailContentCreator(messageSource, "test", successMessageService, docRecommendationMessageService);
        programs = List.of(SNAP);
    }

    @Test
    void includesTheConfirmationNumber() {
        String emailContent = emailContentCreator.createClientHTML(new ApplicationData(),
                "someNumber",
                programs,
                SnapExpeditedEligibility.UNDETERMINED,
                CcapExpeditedEligibility.UNDETERMINED,
                Locale.ENGLISH);
        assertThat(emailContent).contains("someNumber");
    }

    @Test
    void includesVerificationDocuments() {
        String emailContent = emailContentCreator.createClientHTML(new ApplicationData(),
                "someNumber",
                programs,
                SnapExpeditedEligibility.UNDETERMINED,
                CcapExpeditedEligibility.UNDETERMINED,
                Locale.ENGLISH);
        assertThat(emailContent).contains("someNumber");
    }


    @Test
    void includesCaseworkerInstructions() {
        String emailContent = emailContentCreator.createCaseworkerHTML();
        assertThat(emailContent).contains("This application was submitted on behalf of a client.");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "ELIGIBLE,<html><body>We received your Minnesota Benefits application.<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br>You will receive a call from your county within 24 hours about your application for food support. The call may come from an unknown number.<br><br>You will need to complete an interview with a caseworker.<br><br>If you don't hear from your county within 3 days or want an update on your case",
            "NOT_ELIGIBLE,<html><body>We received your Minnesota Benefits application.<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br>You will receive a letter in the mail with next steps for your application for food support in 7-10 days.<br><br>You will need to complete an interview with a caseworker.<br><br>If you want an update on your case",
            "UNDETERMINED,<html><body>We received your Minnesota Benefits application.<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br>You will receive a letter in the mail with next steps for your application for food support in 7-10 days.<br><br>You will need to complete an interview with a caseworker.<br><br>If you want an update on your case",
    })
    void createContentForExpedited(SnapExpeditedEligibility snapExpeditedEligibility, String expeditedEligibilityContent) {
        String emailContent = emailContentCreator.createClientHTML(new ApplicationData(),
                "someNumber",
                programs,
                snapExpeditedEligibility,
                CcapExpeditedEligibility.UNDETERMINED,
                Locale.ENGLISH);
        assertThat(emailContent).contains(expeditedEligibilityContent);
        assertThat(emailContent).contains("please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\" rel=\"noopener noreferrer\">call your county.</a><br><br>You may be able to receive more support. See “What benefits programs do I qualify for” at <a href=\"https://www.mnbenefits.org/faq#what-benefits-programs\" target=\"_blank\" rel=\"noopener noreferrer\">mnbenefits.org/faq</a>.<br><br>**This is an automated message. Please do not reply to this message.**</body></html>");
    }

    @Test
    void shouldIncludeConfirmationIdAndIpWhenSendingDownloadAlert() {
        String confirmationId = "confirmation ID";
        String ip = "123.123.123.123";
        String content = emailContentCreator.createDownloadCafAlertContent(confirmationId, ip, Locale.ENGLISH);

        assertThat(content).isEqualTo("The CAF with confirmation number confirmation ID was downloaded from IP address 123.123.123.123.");
    }

    @Test
    void shouldCreateNonCountyPartnerAlertEmail() {
        String confirmationId = "confirm Id";
        ZonedDateTime submissionTime = ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 11, 10), ZoneOffset.UTC);
        String nonCountyPartnerAlertEmailContent = emailContentCreator.createNonCountyPartnerAlert(confirmationId, submissionTime, Locale.ENGLISH);

        assertThat(nonCountyPartnerAlertEmailContent).isEqualTo(
                "Application confirm Id was submitted at 01/01/2020 05:10."
        );
    }

    @Test
    void shouldCreateLaterDocsConfirmationEmail() {
        String laterDocsConfirmationEmailSubject = emailContentCreator.createClientLaterDocsConfirmationEmailSubject(Locale.ENGLISH);
        String laterDocsConfirmationEmailBody = emailContentCreator.createClientLaterDocsConfirmationEmailBody(Locale.ENGLISH);
        assertThat(laterDocsConfirmationEmailSubject).isEqualTo("We received your documents");
        assertThat(laterDocsConfirmationEmailBody).isEqualTo("<html><body>" +
                "<p>We received your documents for your Minnesota Benefits application. Look out for mail about your case. You may need to complete additional steps.</p>" +
                "<p>To ask about your application status, find your county's contact information <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\" rel=\"noopener noreferrer\">here</a>.</p>" +
                "</body></html>");
    }

    @Test
    void shouldCreateConfirmationEmailFromDemo() {
        emailContentCreator = new EmailContentCreator(messageSource, "demo", successMessageService, docRecommendationMessageService);

        String emailContent = emailContentCreator.createClientHTML(new ApplicationData(),
                "someNumber",
                List.of(CCAP),
                SnapExpeditedEligibility.UNDETERMINED,
                CcapExpeditedEligibility.ELIGIBLE,
                Locale.ENGLISH);
        assertThat(emailContent).contains("This e-mail is for demo purposes only. No application for benefits was submitted on your behalf.");
        assertThat(emailContent).contains("Your county will decide on your childcare case within the next 5 working days.");
    }

    @Test
    void shouldIncludeLaterDocsRecommendationsInConfirmationEmail() {
        programs = List.of(CCAP, SNAP, CASH, EA, GRH);

        ApplicationData applicationData = new ApplicationData();
        applicationData.setSubworkflows(new Subworkflows());
        applicationData.setPagesData(new PagesData());
        applicationData.setStartTimeOnce(Instant.now());
        applicationData.setId("someId");

        PagesData pagesData = new PagesDataBuilder().build(List.of(
                new PageDataBuilder("choosePrograms", Map.of("programs", programs)),
                // Show proof of income
                new PageDataBuilder("employmentStatus", Map.of("areYouWorking", List.of("true"))),
                // Sow proof of housing cost
                new PageDataBuilder("homeExpenses", Map.of("homeExpenses", List.of("RENT"))),
                // Show proof of job loss
                new PageDataBuilder("workSituation", Map.of("hasWorkSituation", List.of("true"))),
                // Show proof of medical expenses
                new PageDataBuilder("medicalExpenses", Map.of("medicalExpenses", List.of("MEDICAL_INSURANCE_PREMIUMS")))
        ));

        applicationData.setPagesData(pagesData);

        String emailContent = emailContentCreator.createClientHTML(applicationData,
                "someNumber",
                programs,
                SnapExpeditedEligibility.UNDETERMINED,
                CcapExpeditedEligibility.UNDETERMINED,
                Locale.ENGLISH);

        assertThat(emailContent).contains("""
                <html><body>We received your Minnesota Benefits application.<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br>You will receive a letter in the mail with next steps for your application for childcare, housing, emergency assistance, cash support and food support in 7-10 days.<br><br>You will need to complete an interview with a caseworker.<br><br>If you want an update on your case, please <a href="https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG" target="_blank" rel="noopener noreferrer">call your county.</a><br><br>You may be able to receive more support. See “What benefits programs do I qualify for” at <a href="https://www.mnbenefits.org/faq#what-benefits-programs" target="_blank" rel="noopener noreferrer">mnbenefits.org/faq</a>.<p><strong>Verification Docs:</strong><br>If you need to submit verification documents for your case, you can <a href="https://www.mnbenefits.org/#later-docs-upload" target="_blank" rel="noopener noreferrer">return to MNbenefits.org</a> to upload documents at any time.<br>You may need to share the following documents:<br><ul><li>Proof of Income: A document with employer and employee names and your total pre-tax income from the last 30 days (or total hours worked and rate of pay). Example: Paystubs</li><li>Proof of Housing Costs: A document showing total amount paid for housing. Examples: Rent receipts, lease, or mortgage statements</li><li>Proof of Job Loss: A document with your former employer’s name and signature, the last day you worked, and date and amount of your final paycheck. Example: Pink slip</li><li>Proof of Medical Expenses: Documents showing medical expenses that you paid for.</li></ul></p><br><br>**This is an automated message. Please do not reply to this message.**</body></html>""");
    }
}