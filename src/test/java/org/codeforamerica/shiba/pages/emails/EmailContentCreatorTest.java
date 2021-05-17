package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.SuccessMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
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

    private List<String> programs;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        emailContentCreator = new EmailContentCreator(messageSource, "test", successMessageService);
        programs = List.of(Program.SNAP);
    }

    @Test
    void includesTheConfirmationNumber() {
        String emailContent = emailContentCreator.createClientHTML("someNumber",
                programs,
                SnapExpeditedEligibility.UNDETERMINED,
                CcapExpeditedEligibility.UNDETERMINED,
                Locale.ENGLISH);

        assertThat(emailContent).contains("someNumber");
    }

    @Test
    void includesVerificationDocuments() {
        String emailContent = emailContentCreator.createClientHTML("someNumber",
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
            "ELIGIBLE,You will receive a call from your county within 24 hours about your application for food support. The call may come from an unknown number.<br><br>You will need to complete an interview with a caseworker.",
            "NOT_ELIGIBLE,You will receive a letter in the mail with next steps for your application for food support in 7-10 days.<br><br>You will need to complete an interview with a caseworker.",
            "UNDETERMINED,You will receive a letter in the mail with next steps for your application for food support in 7-10 days.<br><br>You will need to complete an interview with a caseworker.",
    })
    void createContentForExpedited(SnapExpeditedEligibility snapExpeditedEligibility, String expeditedEligibilityContent) {
        String emailContent = emailContentCreator.createClientHTML("someNumber",
                programs,
                snapExpeditedEligibility,
                CcapExpeditedEligibility.UNDETERMINED,
                Locale.ENGLISH);

        assertThat(emailContent).contains(expeditedEligibilityContent);
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
                "<p>To ask about your application status, find your county's contact information <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">here</a>.</p>" +
                "</body><html>");
    }


    @Test
    void shouldCreateConfirmationEmailFromDemo() {
        emailContentCreator = new EmailContentCreator(messageSource, "demo", successMessageService);

        String emailContent = emailContentCreator.createClientHTML("someNumber",
                programs,
                SnapExpeditedEligibility.UNDETERMINED,
                CcapExpeditedEligibility.UNDETERMINED,
                Locale.ENGLISH);
        assertThat(emailContent).contains("This e-mail is for demo purposes only");
    }
}