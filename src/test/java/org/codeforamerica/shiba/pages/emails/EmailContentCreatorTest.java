package org.codeforamerica.shiba.pages.emails;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.SNAP;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.ExpeditedCcap;
import org.codeforamerica.shiba.output.caf.ExpeditedSnap;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService;
import org.codeforamerica.shiba.pages.NextStepsContentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest()
class EmailContentCreatorTest {

  @Autowired
  private EmailContentCreator emailContentCreator;

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private NextStepsContentService nextStepsContentService;

  @Autowired
  private DocRecommendationMessageService docRecommendationMessageService;

  private List<String> programs;

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(ENGLISH);
    emailContentCreator = new EmailContentCreator(messageSource, "test", nextStepsContentService,
        docRecommendationMessageService);
    programs = List.of(SNAP);
  }


  @Test
  void includesCaseworkerInstructions() {
    String emailContent = emailContentCreator.createCaseworkerHTML();
    assertThat(emailContent).contains("This application was submitted on behalf of a client.");
  }

  @Test
  void shouldIncludeConfirmationIdAndIpWhenSendingDownloadAlert() {
    String confirmationId = "confirmation ID";
    String ip = "123.123.123.123";
    String content = emailContentCreator
        .createDownloadCafAlertContent(confirmationId, ip, ENGLISH);

    assertThat(content).isEqualTo(
        "The CAF with confirmation number confirmation ID was downloaded from IP address 123.123.123.123.");
  }

  @Test
  void shouldCreateNonCountyPartnerAlertEmail() {
    String confirmationId = "confirm Id";
    ZonedDateTime submissionTime = ZonedDateTime
        .of(LocalDateTime.of(2020, 1, 1, 11, 10), ZoneOffset.UTC);
    String nonCountyPartnerAlertEmailContent = emailContentCreator
        .createNonCountyPartnerAlert(confirmationId, submissionTime, ENGLISH);

    assertThat(nonCountyPartnerAlertEmailContent).isEqualTo(
        "Application confirm Id was submitted at 01/01/2020 05:10."
    );
  }

  @Test
  void shouldCreateLaterDocsConfirmationEmail() {
    String laterDocsConfirmationEmailSubject = emailContentCreator
        .createClientLaterDocsConfirmationEmailSubject(ENGLISH);
    String laterDocsConfirmationEmailBody = emailContentCreator
        .createClientLaterDocsConfirmationEmailBody(ENGLISH);
    assertThat(laterDocsConfirmationEmailSubject).isEqualTo("We received your documents");
    assertThat(laterDocsConfirmationEmailBody).isEqualTo("<html><body>" +
        "<p>We received your documents for your Minnesota Benefits application. Look out for mail about your case. You may need to complete additional steps.</p>"
        +
        "<p>To ask about your application status, find your county's contact information <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\" rel=\"noopener noreferrer\">here</a>.</p>"
        +
        "</body></html>");
  }

  @ParameterizedTest
  @CsvSource(value = {
      "UPLOADED_DOC,an uploaded document.",
      "CAF,a CAF application.",
      "CCAP,a CCAP application."
  })
  void shouldCreateResubmitEmail(Document document, String name) {
    String resubmitEmailBody = emailContentCreator
        .createResubmitEmailContent(document, ENGLISH);
    assertThat(resubmitEmailBody).isEqualTo("<html><body>" +
        "<p>Due to a technical issue, this MNBenefits file did not submit to the MNIT inbox. We are sharing it here instead. It is "
        + name + "</p>" +
        "</body></html>");
  }

  @Test
  void shouldCreateShortConfirmationEmail() {
    String emailContent = emailContentCreator.createShortClientConfirmationEmail("someNumber",
        ENGLISH);
    assertThat(emailContent).contains("<html><body>We received your Minnesota Benefits application."
        + "<br><br>Confirmation number: <strong>#someNumber</strong>"
        + "<br>Application status: <strong>In review</strong>"
        + "<br><br>"
        + "You may be able to receive more support. See “What benefits programs do I qualify for” at <a href=\"https://www.mnbenefits.org/faq#what-benefits-programs\" target=\"_blank\" rel=\"noopener noreferrer\">mnbenefits.org/faq</a>.<br><br>"
        + "**This is an automated message. Please do not reply to this message.**</body></html>");
    assertThat(emailContent).doesNotContain(
        "if you don’t hear from them in the time period we’ve noted");
    assertThat(emailContent).doesNotContain("Verification Docs");
  }

  @Test
  void shouldCreateNextStepsEmail() {
    programs = List.of(CCAP, EA, SNAP);
    String emailContent = emailContentCreator.createNextStepsEmail(
        "someNumber",
        programs,
        ExpeditedSnap.ELIGIBLE,
        ExpeditedCcap.ELIGIBLE,
        ENGLISH);
    assertThat(emailContent).contains(
        "<html><body><strong>You Are Eligible for Expedited SNAP:</strong><br>"
            + "Within 24 hours, <strong>expect a call</strong> from your county about your food assistance application.<br><br>"
            + "<strong>You Are Eligible for Expedited CCAP:</strong><br>"
            + "Within 5 days, your county will determine your childcare assistance case and <strong>send you a letter in the mail</strong>.<br><br>"
            + "<strong>Time to Hear Back:</strong><br>"
            + "In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your emergency assistance application. The letter will explain your next steps.<br><br>"
            + "<strong>When to Reach Out:</strong><br>"
            + "<a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\" rel=\"noopener noreferrer\">Call your county</a> if you don’t hear from them in the time period we’ve noted.<br><br>"
            + "<strong>Get More Help:</strong><br>You may be able to receive more support. See “What benefits programs do I qualify for” at <a href=\"https://www.mnbenefits.org/faq#what-benefits-programs\" target=\"_blank\" rel=\"noopener noreferrer\">mnbenefits.org/faq</a><br><br>**This is an automated message. Please do not reply to this message.**</body></html>");
  }
}
