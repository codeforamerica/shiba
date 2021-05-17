package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.output.caf.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.*;
import org.springframework.context.i18n.*;
import org.springframework.test.context.*;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = NONE)
class EmailContentCreatorTest {

    @Autowired
    private EmailContentCreator emailContentCreator;

    @Autowired
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        emailContentCreator = new EmailContentCreator(messageSource, "test");
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
            "ELIGIBLE,Your county will call you in the next 3 days for your phone interview.",
            "NOT_ELIGIBLE,Your county will mail you a notice that will arrive in the next week.",
            "UNDETERMINED,Your county will mail you a notice that will arrive in the next week.",
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
        emailContentCreator = new EmailContentCreator(messageSource, "demo");

        String emailContent = emailContentCreator.createClientHTML("someNumber", SnapExpeditedEligibility.UNDETERMINED, Locale.ENGLISH);
        assertThat(emailContent).contains("This e-mail is for demo purposes only");
    }
}