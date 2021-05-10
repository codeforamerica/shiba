package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.profiles.active=test"
})
class EmailContentCreatorTest {
    private final StaticMessageSource staticMessageSource = new StaticMessageSource();
    private EmailContentCreator emailContentCreator;

    @Value("${spring.profiles.active}")
    private String activeProfile;


    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        emailContentCreator = new EmailContentCreator(staticMessageSource, activeProfile);
        staticMessageSource.addMessage("email.snap-expedited-wait-time", Locale.ENGLISH, "you've been expedited!");
        staticMessageSource.addMessage("email.snap-nonexpedited-wait-time", Locale.ENGLISH, "not expedited :(");
        staticMessageSource.addMessage("email.client-body", Locale.ENGLISH, "confirmation email! {0} confirmation number: {1}");
        staticMessageSource.addMessage("email.download-caf-alert", Locale.ENGLISH, "confirmation number: {0} ip address: {1}.");
        staticMessageSource.addMessage("email.non-county-partner-alert", Locale.ENGLISH, "Application {0} was submitted at {1}.");
        staticMessageSource.addMessage("later-docs.confirmation-email-subject", Locale.ENGLISH, "We received your documents");
        staticMessageSource.addMessage("later-docs.confirmation-email-body", Locale.ENGLISH, "We received your documents for your Minnesota Benefits application. Look out for mail about your case. You may need to complete additional steps.");
        staticMessageSource.addMessage("later-docs.confirmation-email-body-link", Locale.ENGLISH, "To ask about your application status, find your county's contact information <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\">here</a>.");
        staticMessageSource.addMessage("email.demo-purposes-only", Locale.ENGLISH, "This e-mail is for demo purposes only. No application for benefits was submitted on your behalf.");
        staticMessageSource.addMessage("email.share-feedback", Locale.ENGLISH, "To share feedback, please get in touch with the Code for America Team, or fill out <a href=\"https://airtable.com/shrwudOXtR9q6WCXD\" target=\"_blank\">this form</a>.");
    }

    @Test
    void includesTheConfirmationNumber() {
        String emailContent = emailContentCreator.createClientHTML("someNumber", SnapExpeditedEligibility.UNDETERMINED, Locale.ENGLISH);

        assertThat(emailContent).contains("someNumber");
    }

    @Test
    void includesCaseworkerInstructions() {
        String emailContent = emailContentCreator.createCaseworkerHTML();

        assertThat(emailContent).contains("This application was submitted on behalf of a client.");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "ELIGIBLE, you've been expedited!",
            "NOT_ELIGIBLE, not expedited :(",
            "UNDETERMINED, not expedited :(",
    })
    void createContentForExpedited(SnapExpeditedEligibility snapExpeditedEligibility, String expeditedEligibilityContent) {
        String emailContent = emailContentCreator.createClientHTML("someNumber", snapExpeditedEligibility, Locale.ENGLISH);

        assertThat(emailContent).contains(expeditedEligibilityContent);
    }

    @Test
    void shouldIncludeConfirmationIdAndIpWhenSendingDownloadAlert() {
        String confirmationId = "confirmation ID";
        String ip = "123.123.123.123";

        String content = emailContentCreator.createDownloadCafAlertContent(confirmationId, ip, Locale.ENGLISH);

        assertThat(content).isEqualTo(String.format("confirmation number: %s ip address: %s.", confirmationId, ip));
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

    @Nested
    @Tag("demo-testing")
    class EmailContentCreatorDemoTest {
        @BeforeEach
        void setUp() {
            LocaleContextHolder.setLocale(Locale.ENGLISH);
            emailContentCreator = new EmailContentCreator(staticMessageSource, "demo");
            staticMessageSource.addMessage("email.snap-expedited-wait-time", Locale.ENGLISH, "you've been expedited!");
            staticMessageSource.addMessage("email.snap-nonexpedited-wait-time", Locale.ENGLISH, "not expedited :(");
            staticMessageSource.addMessage("email.client-body", Locale.ENGLISH, "confirmation email! {0} confirmation number: {1}");
            staticMessageSource.addMessage("email.demo-purposes-only", Locale.ENGLISH, "This e-mail is for demo purposes only. No application for benefits was submitted on your behalf.");
            staticMessageSource.addMessage("email.share-feedback", Locale.ENGLISH, "To share feedback, please get in touch with the Code for America Team, or fill out <a href=\"https://airtable.com/shrwudOXtR9q6WCXD\" target=\"_blank\">this form</a>.");
        }

        @Test
        void shouldCreateConfirmationEmailFromDemo() {
            String emailContent = emailContentCreator.createClientHTML("someNumber", SnapExpeditedEligibility.UNDETERMINED, Locale.ENGLISH);
            assertThat(emailContent).contains("This e-mail is for demo purposes only");
        }
    }
}