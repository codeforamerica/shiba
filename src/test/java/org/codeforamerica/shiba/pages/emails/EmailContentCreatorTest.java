package org.codeforamerica.shiba.pages.emails;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService;
import org.codeforamerica.shiba.pages.NextStepsContentService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
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
  void includesTheConfirmationNumber() {
    String emailContent = emailContentCreator.createFullClientConfirmationEmail(
        new ApplicationData(),
        "someNumber",
        programs,
        SnapExpeditedEligibility.UNDETERMINED,
        CcapExpeditedEligibility.UNDETERMINED,
        ENGLISH);
    assertThat(emailContent).contains("someNumber");
  }

  @Test
  void includesCaseworkerInstructions() {
    String emailContent = emailContentCreator.createCaseworkerHTML();
    assertThat(emailContent).contains("This application was submitted on behalf of a client.");
  }

  @ParameterizedTest
  @CsvSource(value = {
      "ELIGIBLE,<html><body>We received your Minnesota Benefits application.<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br>Within 24 hours, <strong>expect a call</strong> from your county about your food assistance application.<br><br>If you don't hear from your county within 3 days or want an update on your case, please <a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\" rel=\"noopener noreferrer\">call your county.</a><br><br>You may be able to receive more support. See “What benefits programs do I qualify for” at <a href=\"https://www.mnbenefits.org/faq#what-benefits-programs\" target=\"_blank\" rel=\"noopener noreferrer\">mnbenefits.org/faq</a>.<br><br>**This is an automated message. Please do not reply to this message.**</body></html>",
      "NOT_ELIGIBLE,<html><body>We received your Minnesota Benefits application.<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br>In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your food support application. The letter will explain your next steps.<br><br><a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\" rel=\"noopener noreferrer\">Call your county</a> if you don’t hear from them in the time period we’ve noted. <br><br>You may be able to receive more support. See “What benefits programs do I qualify for” at <a href=\"https://www.mnbenefits.org/faq#what-benefits-programs\" target=\"_blank\" rel=\"noopener noreferrer\">mnbenefits.org/faq</a>.<br><br>**This is an automated message. Please do not reply to this message.**</body></html>",
      "UNDETERMINED,<html><body>We received your Minnesota Benefits application.<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br>In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your food support application. The letter will explain your next steps.<br><br><a href=\"https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG\" target=\"_blank\" rel=\"noopener noreferrer\">Call your county</a> if you don’t hear from them in the time period we’ve noted. <br><br>You may be able to receive more support. See “What benefits programs do I qualify for” at <a href=\"https://www.mnbenefits.org/faq#what-benefits-programs\" target=\"_blank\" rel=\"noopener noreferrer\">mnbenefits.org/faq</a>.<br><br>**This is an automated message. Please do not reply to this message.**</body></html>",
  })
  void createContentForExpedited(SnapExpeditedEligibility snapExpeditedEligibility,
      String expeditedEligibilityContent) {
    String emailContent = emailContentCreator.createFullClientConfirmationEmail(
        new ApplicationData(),
        "someNumber",
        programs,
        snapExpeditedEligibility,
        CcapExpeditedEligibility.UNDETERMINED,
        ENGLISH);
    assertThat(emailContent).contains(expeditedEligibilityContent);
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
  void shouldCreateConfirmationEmailFromDemo() {
    emailContentCreator = new EmailContentCreator(messageSource, "demo", nextStepsContentService,
        docRecommendationMessageService);

    String emailContent = emailContentCreator.createFullClientConfirmationEmail(
        new ApplicationData(),
        "someNumber",
        List.of(CCAP),
        SnapExpeditedEligibility.UNDETERMINED,
        CcapExpeditedEligibility.ELIGIBLE,
        ENGLISH);
    assertThat(emailContent).contains(
        "This e-mail is for demo purposes only. No application for benefits was submitted on your behalf.");
    assertThat(emailContent).contains(
        "Within 5 days, your county will determine your childcare assistance case and <strong>send you a letter in the mail</strong>.");
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
        programs,
        SnapExpeditedEligibility.ELIGIBLE,
        CcapExpeditedEligibility.ELIGIBLE,
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

  @Test
  void shouldIncludeLaterDocsRecommendationsInFullConfirmationEmailAndDocUploadEmail() {
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
        new PageDataBuilder("medicalExpenses",
            Map.of("medicalExpenses", List.of("MEDICAL_INSURANCE_PREMIUMS")))
    ));

    applicationData.setPagesData(pagesData);

    String confirmationEmail = emailContentCreator.createFullClientConfirmationEmail(
        applicationData,
        "someNumber",
        programs,
        SnapExpeditedEligibility.UNDETERMINED,
        CcapExpeditedEligibility.UNDETERMINED,
        ENGLISH);
    assertThat(confirmationEmail).contains("""
        <html><body>We received your Minnesota Benefits application.<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br>In the next 7-10 days, <strong>expect to get a letter in the mail</strong> from your county about your childcare, housing, emergency assistance, cash support and food support application. The letter will explain your next steps.<br><br><a href="https://edocs.dhs.state.mn.us/lfserver/Public/DHS-5207-ENG" target="_blank" rel="noopener noreferrer">Call your county</a> if you don’t hear from them in the time period we’ve noted.<br><br>You may be able to receive more support. See “What benefits programs do I qualify for” at <a href="https://www.mnbenefits.org/faq#what-benefits-programs" target="_blank" rel="noopener noreferrer">mnbenefits.org/faq</a>.<p><strong>Verification Docs:</strong><br>If you need to submit verification documents for your case, you can <a href="https://www.mnbenefits.org/?utm_medium=confirmationemail#later-docs-upload" target="_blank" rel="noopener noreferrer">return to MNbenefits.org</a> to upload documents at any time.<br>You may need to share the following documents:<br><ul><li>Proof of Income: A document with employer and employee names and your total pre-tax income from the last 30 days (or total hours worked and rate of pay). Example: Paystubs</li><li>Proof of Housing Costs: A document showing total amount paid for housing. Examples: Rent receipts, lease, or mortgage statements</li><li>Proof of Job Loss: A document with your former employer’s name and signature, the last day you worked, and date and amount of your final paycheck. Example: Pink slip</li><li>Proof of Medical Expenses: Documents showing medical expenses that you paid for.</li></ul></p><br><br>**This is an automated message. Please do not reply to this message.**</body></html>""");

    String docRecEmail = emailContentCreator.createDocumentRecommendationEmail(applicationData,
        ENGLISH);
    assertThat(docRecEmail).contains(
        "<html><body>Remember to upload documents on <a href=\"https://www.mnbenefits.org/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.org</a> to support your MN Benefits application. You can use your phone to take or upload pictures, or use your computer to upload documents.<br>"
            + "If you have them, you should upload the following documents:<br>"
            + "<ul><li>Proof of Income: A document with employer and employee names and your total pre-tax income from the last 30 days (or total hours worked and rate of pay). Example: Paystubs</li><li>Proof of Housing Costs: A document showing total amount paid for housing. Examples: Rent receipts, lease, or mortgage statements</li><li>Proof of Job Loss: A document with your former employer’s name and signature, the last day you worked, and date and amount of your final paycheck. Example: Pink slip</li><li>Proof of Medical Expenses: Documents showing medical expenses that you paid for.</li></ul>"
            + "If you have already uploaded these documents, you can ignore this reminder.<br><br>"
            + "**This is an automated message. Please do not reply to this message.**</body></html>");
  }


}
