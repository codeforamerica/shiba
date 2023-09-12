package org.codeforamerica.shiba.pages.emails;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.TribalNation.MilleLacsBandOfOjibwe;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.codeforamerica.shiba.RoutingDestinationMessageService;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService;
import org.codeforamerica.shiba.pages.NextStepsContentService;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class EmailContentCreatorTest {

  @Autowired
  private EmailContentCreator emailContentCreator;

  @Autowired
  private MessageSource messageSource;
  
  @Autowired
  private NextStepsContentService nextStepsContentService;

  @Autowired
  private DocRecommendationMessageService docRecommendationMessageService;
  
  @Mock
  private ApplicationRepository applicationRepository;
  
  @MockBean
  private RoutingDecisionService routingDecisionService;

  private Set<RoutingDestination> routingDestinations;
  
  @Mock
  private RoutingDestinationMessageService routingDestinationMessageService;

  private List<String> programs;
  private ApplicationData applicationData;
  private ZonedDateTime submissionTime;
  private String formattedTime;
  private static final ZoneId CENTRAL_TIMEZONE = ZoneId.of("America/Chicago");

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(ENGLISH);
    
    routingDestinations = new LinkedHashSet<>();
    routingDestinations.add(
    		new CountyRoutingDestination(Anoka, "DPI", "email", "555-5555"));
    routingDestinations.add(new TribalNationRoutingDestination(MilleLacsBandOfOjibwe,
            "someProviderId", "someEmail", "222-2222"));
    when(routingDecisionService.getRoutingDestinations(any(), any())).thenReturn(
    		new ArrayList<>(routingDestinations));
    when(routingDestinationMessageService.generatePhrase(any(),
            any(), anyBoolean(), any())).thenReturn(
            "Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222)");

    applicationData = new ApplicationData();
    applicationData.setId("9870000123");
    doReturn(Application.builder()
        .id("9870000123")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Anoka)
        .timeToComplete(null)
        .build()).when(applicationRepository).find(any());
    
    submissionTime = ZonedDateTime.now(CENTRAL_TIMEZONE);
    formattedTime = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            .format(submissionTime.withZoneSameInstant(ZoneId.of("America/Chicago")));
    
    emailContentCreator = new EmailContentCreator(messageSource, "test", nextStepsContentService,
            docRecommendationMessageService, applicationRepository, routingDecisionService, routingDestinationMessageService);
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

	
  @Test
  void createContentForExpeditedEligible() {
	String eligibilityContent = "<html><body>We received your Minnesota Benefits application.<br><br>Your application was submitted to Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) on " + formattedTime + ".<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br><strong>What's Next?</strong><br>Your next steps in the application process may include uploading verification documents and being available for calls and communications from your worker.<br><br><strong>Upload your documents.</strong><br>Do you want to send in documents or proof to your county or Tribal Nation directly? Go to <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> and click on ‘Upload Documents’. The most requested documents are identification (ID), paystubs from your job or proof of recent loss of job and proof of rent or shelter costs. <br><br><strong>Allow time for a worker to review.</strong><br>Your application and documents will be reviewed by Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222). Within the next 5 days expect a phone call from an eligibility worker.  <br>The time it takes to review your application can vary. If you haven’t heard back about your application within 7 days, contact Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) before you submit another application. <br><br><strong>Need help now?</strong><br>If you are in a mental health crisis, call or text 988. <br>To connect with food, housing, medical or other help, you can call 211, or text your zip code to 898-211, or visit <a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. <br>If you need additional assistance to access food, contact Minnesota Food HelpLine at 888-711-1151 or <a href=\"https://www.hungersolutions.org/\" target=\"_blank\" rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br><strong>Have other questions?</strong><br>Visit <a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. <br><br></body></html>";
    String emailContent = emailContentCreator.createFullClientConfirmationEmail(
    	applicationData,
        "someNumber",
        programs,
        SnapExpeditedEligibility.ELIGIBLE,
        CcapExpeditedEligibility.UNDETERMINED,
        ENGLISH);
    assertThat(emailContent).contains(eligibilityContent);
  }
  
  @Test
  void createContentForExpeditedNotEligible() {
    String eligibilityContent = "<html><body>We received your Minnesota Benefits application.<br><br>Your application was submitted to Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) on " + formattedTime + ".<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br><strong>What's Next?</strong><br>Your next steps in the application process may include uploading verification documents and being available for calls and communications from your worker.<br><br><strong>Upload your documents.</strong><br>Do you want to send in documents or proof to your county or Tribal Nation directly? Go to <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> and click on ‘Upload Documents’. The most requested documents are identification (ID), paystubs from your job or proof of recent loss of job and proof of rent or shelter costs. <br><br><strong>Allow time for a worker to review.</strong><br>Your application and documents will be reviewed by Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222).  The time it takes to review your application can vary. <br>Expect an eligibility worker to contact you by phone or mail with information about your next steps. If you haven’t heard back about your application, know that work is still in progress. <br>Before you submit another application, contact Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222).<br><br><strong>Need help now?</strong><br>If you are in a mental health crisis, call or text 988. <br>To connect with food, housing, medical or other help, you can call 211, or text your zip code to 898-211, or visit <a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. <br>If you need additional assistance to access food, contact Minnesota Food HelpLine at 888-711-1151 or <a href=\"https://www.hungersolutions.org/\" target=\"_blank\" rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br><strong>Have other questions?</strong><br>Visit <a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. <br><br></body></html>";
    String emailContent = emailContentCreator.createFullClientConfirmationEmail(
    	applicationData,
        "someNumber",
        programs,
        SnapExpeditedEligibility.NOT_ELIGIBLE,
        CcapExpeditedEligibility.UNDETERMINED,
        ENGLISH);
    assertThat(emailContent).contains(eligibilityContent);
  }
  
  @Test
  void createContentForExpeditedUndetermined() {
    String eligibilityContent = "<html><body>We received your Minnesota Benefits application.<br><br>Your application was submitted to Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) on " + formattedTime + ".<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: <strong>In review</strong><br><br><strong>What's Next?</strong><br>Your next steps in the application process may include uploading verification documents and being available for calls and communications from your worker.<br><br><strong>Upload your documents.</strong><br>Do you want to send in documents or proof to your county or Tribal Nation directly? Go to <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> and click on ‘Upload Documents’. The most requested documents are identification (ID), paystubs from your job or proof of recent loss of job and proof of rent or shelter costs. <br><br><strong>Allow time for a worker to review.</strong><br>Your application and documents will be reviewed by Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222).  The time it takes to review your application can vary. <br>Expect an eligibility worker to contact you by phone or mail with information about your next steps. If you haven’t heard back about your application, know that work is still in progress. <br>Before you submit another application, contact Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222).<br><br><strong>Need help now?</strong><br>If you are in a mental health crisis, call or text 988. <br>To connect with food, housing, medical or other help, you can call 211, or text your zip code to 898-211, or visit <a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. <br>If you need additional assistance to access food, contact Minnesota Food HelpLine at 888-711-1151 or <a href=\"https://www.hungersolutions.org/\" target=\"_blank\" rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br><strong>Have other questions?</strong><br>Visit <a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. <br><br></body></html>";
    String emailContent = emailContentCreator.createFullClientConfirmationEmail(
    	applicationData,
        "someNumber",
        programs,
        SnapExpeditedEligibility.UNDETERMINED,
        CcapExpeditedEligibility.UNDETERMINED,
        ENGLISH);
    assertThat(emailContent).contains(eligibilityContent);
  }


  @Test
  void shouldCreateLaterDocsConfirmationEmail() {
    String laterDocsConfirmationEmailSubject = emailContentCreator
        .createClientLaterDocsConfirmationEmailSubject(ENGLISH);
    String laterDocsConfirmationEmailBody = emailContentCreator
        .createClientLaterDocsConfirmationEmailBody(applicationData, "9870000123", ENGLISH);
    assertThat(laterDocsConfirmationEmailSubject).isEqualTo("We received documents for your MNbenefits Application");
    assertThat(laterDocsConfirmationEmailBody).isEqualTo("<html><body>" +
                                                         "<p>We received your documents for your Minnesota Benefits application.</p>"
                                                         +
                                                         "<p>Your documents were submitted to Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) on " + formattedTime + ".</p>"
                                                         +
                                                         "<p>Document confirmation number: #9870000123</p>"
                                                         +
                                                         "<p>Look out for mail containing more information about your case. You may need to complete additional steps.</p>"
                                                         +
                                                         "<p>If you would like an update on your case, please call the county or Tribal Nation listed above.</p>"
                                                         +
                                                         "</body></html>");
  }
  
  @Test
  void shouldCreateHealthCareRenewalConfirmationEmail() {
    String healthcareRenewalConfirmationEmailSubject = emailContentCreator
        .createClientHealthcareRenewalConfirmationEmailSubject(ENGLISH);
    String healthcareRenewalConfirmationEmailBody = emailContentCreator
        .createClientHealthcareRenewalConfirmationEmailBody(applicationData, "9870000123", ENGLISH);
    assertThat(healthcareRenewalConfirmationEmailSubject).isEqualTo("We received documents for your Health Care Renewal");
    assertThat(healthcareRenewalConfirmationEmailBody).isEqualTo("<html><body>" +
                                                         "<p>We received documents for your Minnesota Health Care Programs Renewal.</p>"
                                                         +
                                                         "<p>Your documents were submitted to Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) on " + formattedTime + ".</p>"
                                                         +
                                                         "<p>Document confirmation number: <b>#9870000123</b></p>"
                                                         +
                                                         "<p>Watch for mail containing more information about your case. You may need to complete additional steps.</p>"
                                                         +
                                                         "<p>If you would like an update on your case, please call the county or Tribal Nation listed above.</p>"
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
                                            "<p>Due to a technical issue, this MNbenefits file did not submit to the MNIT inbox. We are sharing it here instead. It is "
                                            + name + "</p>" +
                                            "</body></html>");
  }


  @Test
  void shouldCreateShortConfirmationEmail() {
    String emailContent = emailContentCreator.createShortClientConfirmationEmail(applicationData, "someNumber",
        ENGLISH);
    assertThat(emailContent).contains("<html><body>We received your Minnesota Benefits application.<br><br>Your "
    		+ "application was submitted to Anoka County (555-5555) and Mille Lacs Band of Ojibwe "
    		+ "(222-2222) on " + formattedTime + ".<br><br>Confirmation number: <strong>#someNumber</strong><br>Application status: "
    		+ "<strong>In review</strong><br><br>If you would like an update on your case, please call the county "
    		+ "or Tribal Nation listed above.</body></html>");
    assertThat(emailContent).doesNotContain(
        "if you donâ€™t hear from them in the time period weâ€™ve noted");
    assertThat(emailContent).doesNotContain("Verification Docs");
  }

  @Test
  void shouldCreateNextStepsEmail() {
    programs = List.of(CCAP, EA, SNAP);
    String applicationId = "applicationId";
    String emailContent = emailContentCreator.createNextStepsEmail(
        programs,
        SnapExpeditedEligibility.ELIGIBLE,
        CcapExpeditedEligibility.ELIGIBLE,
        ENGLISH,
        applicationId);
    assertThat(emailContent).contains(
    		"<html><body><br>You submitted your MNbenefits application and Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) received it."
    		+ "<br><br><strong>What's Next?</strong><br>Your next steps in the application process may include uploading verification documents and being "
    		+ "available for calls and communications from your worker.<br><br><strong>Upload your documents.</strong><br>Do you want to send in documents "
    		+ "or proof to your county or Tribal Nation directly? Go to "
    		+ "<a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> "
    		+ "and click on ‘Upload Documents’. The most requested documents are identification (ID), paystubs from your job or proof of recent loss of job and "
    		+ "proof of rent or shelter costs. <br><br><strong>Allow time for a worker to review.</strong><br>Your application and documents will be reviewed "
    		+ "by Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222). Within the next 5 days expect a phone call from an eligibility worker.  "
    		+ "<br>The time it takes to review your application can vary. If you haven’t heard back about your application within 7 days, contact Anoka County (555-5555) "
    		+ "and Mille Lacs Band of Ojibwe (222-2222) before you submit another application. <br><br><strong>Need help now?</strong><br>"
    		+ "If you are in a mental health crisis, call or text 988. <br>To connect with food, housing, medical or other help, you can call 211, or text your zip "
    		+ "code to 898-211, or visit <a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. <br>If you need additional "
    		+ "assistance to access food, contact Minnesota Food HelpLine at 888-711-1151 or "
    		+ "<a href=\"https://www.hungersolutions.org/\" target=\"_blank\" rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br>"
    		+ "<strong>Have other questions?</strong><br>Visit "
    		+ "<a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. </body></html>");
  }

  @Test
  void shouldCreateNextStepsEmailInSpanish() {
	programs = List.of(CCAP, EA, SNAP);
	String emailContent = emailContentCreator.createNextStepsEmail(programs, SnapExpeditedEligibility.ELIGIBLE,
		CcapExpeditedEligibility.ELIGIBLE, new Locale("es", "ES"), applicationData.getId());
	assertThat(emailContent).contains(
		"<html><body><br>You submitted your MNbenefits application and Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) received it.<br><br><strong>What's Next?</strong><br>Your next steps in the application process may include uploading verification documents and being available for calls and communications from your worker.<br><br><strong>Upload your documents.</strong><br>Do you want to send in documents or proof to your county or Tribal Nation directly? Go to <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> and click on ‘Upload Documents’. The most requested documents are identification (ID), paystubs from your job or proof of recent loss of job and proof of rent or shelter costs. <br><br><strong>Allow time for a worker to review.</strong><br>Your application and documents will be reviewed by Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222). Within the next 5 days expect a phone call from an eligibility worker.  <br>The time it takes to review your application can vary. If you haven’t heard back about your application within 7 days, contact Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) before you submit another application. <br><br><strong>Need help now?</strong><br>If you are in a mental health crisis, call or text 988. <br>To connect with food, housing, medical or other help, you can call 211, or text your zip code to 898-211, or visit <a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. <br>If you need additional assistance to access food, contact Minnesota Food HelpLine at 888-711-1151 or <a href=\"https://www.hungersolutions.org/\" target=\"_blank\" rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br><strong>Have other questions?</strong><br>Visit <a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. </body></html>");
  }

  @Test
  void shouldCreateNextStepsEmailForNoExpeditedPrograms() {
	programs = List.of(SNAP, CASH, CCAP, EA);
	String emailContent = emailContentCreator.createNextStepsEmail(programs, SnapExpeditedEligibility.NOT_ELIGIBLE,
		CcapExpeditedEligibility.NOT_ELIGIBLE, ENGLISH, applicationData.getId());
	assertThat(emailContent).contains(
		"<html><body><br>You submitted your MNbenefits application and Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) received it.<br><br><strong>What's Next?</strong><br>Your next steps in the application process may include uploading verification documents and being available for calls and communications from your worker.<br><br><strong>Upload your documents.</strong><br>Do you want to send in documents or proof to your county or Tribal Nation directly? Go to <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> and click on ‘Upload Documents’. The most requested documents are identification (ID), paystubs from your job or proof of recent loss of job and proof of rent or shelter costs. <br><br><strong>Allow time for a worker to review.</strong><br>Your application and documents will be reviewed by Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222).  The time it takes to review your application can vary. <br>Expect an eligibility worker to contact you by phone or mail with information about your next steps. If you haven’t heard back about your application, know that work is still in progress. <br>Before you submit another application, contact Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222).<br><br><strong>Need help now?</strong><br>If you are in a mental health crisis, call or text 988. <br>To connect with food, housing, medical or other help, you can call 211, or text your zip code to 898-211, or visit <a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. <br>If you need additional assistance to access food, contact Minnesota Food HelpLine at 888-711-1151 or <a href=\"https://www.hungersolutions.org/\" target=\"_blank\" rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br><strong>Have other questions?</strong><br>Visit <a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. </body></html>");
  }

  @Test
  void shouldCreateNextStepsEmailForExpeditedSnap() {
	programs = List.of(SNAP, CASH, CCAP, EA);
	String emailContent = emailContentCreator.createNextStepsEmail(programs, SnapExpeditedEligibility.ELIGIBLE,
		CcapExpeditedEligibility.NOT_ELIGIBLE, ENGLISH, applicationData.getId());
	assertThat(emailContent).contains(
		"<html><body><br>You submitted your MNbenefits application and Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) received it.<br><br><strong>What's Next?</strong><br>Your next steps in the application process may include uploading verification documents and being available for calls and communications from your worker.<br><br><strong>Upload your documents.</strong><br>Do you want to send in documents or proof to your county or Tribal Nation directly? Go to <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> and click on ‘Upload Documents’. The most requested documents are identification (ID), paystubs from your job or proof of recent loss of job and proof of rent or shelter costs. <br><br><strong>Allow time for a worker to review.</strong><br>Your application and documents will be reviewed by Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222). Within the next 5 days expect a phone call from an eligibility worker.  <br>The time it takes to review your application can vary. If you haven’t heard back about your application within 7 days, contact Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) before you submit another application. <br><br><strong>Need help now?</strong><br>If you are in a mental health crisis, call or text 988. <br>To connect with food, housing, medical or other help, you can call 211, or text your zip code to 898-211, or visit <a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. <br>If you need additional assistance to access food, contact Minnesota Food HelpLine at 888-711-1151 or <a href=\"https://www.hungersolutions.org/\" target=\"_blank\" rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br><strong>Have other questions?</strong><br>Visit <a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. </body></html>");
  }

  @Test
  void shouldCreateNextStepsEmailForExpeditedCcap() {
	programs = List.of(SNAP, CASH, CCAP, EA);
	String emailContent = emailContentCreator.createNextStepsEmail(programs, SnapExpeditedEligibility.NOT_ELIGIBLE,
		CcapExpeditedEligibility.ELIGIBLE, ENGLISH, applicationData.getId());
	assertThat(emailContent).contains(
		"<html><body><br>You submitted your MNbenefits application and Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) received it.<br><br><strong>What's Next?</strong><br>Your next steps in the application process may include uploading verification documents and being available for calls and communications from your worker.<br><br><strong>Upload your documents.</strong><br>Do you want to send in documents or proof to your county or Tribal Nation directly? Go to <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> and click on ‘Upload Documents’. The most requested documents are identification (ID), paystubs from your job or proof of recent loss of job and proof of rent or shelter costs. <br><br><strong>Allow time for a worker to review.</strong><br>Your application and documents will be reviewed by Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222). Your Child Care Assistance application does not need an interview. Within the next 5 days expect a phone call from an eligibility worker. <br>The time it takes to review your application can vary. If you haven’t heard back about your application within 7 days, contact Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) before you submit another application.<br><br><strong>Need help now?</strong><br>If you are in a mental health crisis, call or text 988. <br>To connect with food, housing, medical or other help, you can call 211, or text your zip code to 898-211, or visit <a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. <br>If you need additional assistance to access food, contact Minnesota Food HelpLine at 888-711-1151 or <a href=\"https://www.hungersolutions.org/\" target=\"_blank\" rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br><strong>Have other questions?</strong><br>Visit <a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. </body></html>");
  }

  @Test
  void shouldIncludeLaterDocsRecommendationsInFullConfirmationEmailAndDocUploadEmail() {
    programs = List.of(CCAP, SNAP, CASH, EA, GRH);

    ApplicationData applicationData = new ApplicationData();
    applicationData.setStartTimeOnce(Instant.now());
    applicationData.setId("someId");

    new TestApplicationDataBuilder(applicationData)
        .withPageData("choosePrograms", "programs", programs)
        // Show proof of income
        .withPageData("employmentStatus", "areYouWorking", "true")
        // Sow proof of housing cost
        .withPageData("homeExpenses", "homeExpenses", "RENT")
        // Show proof of job loss
        .withPageData("workSituation", "hasWorkSituation", "true")
        // Show proof of medical expenses
        .withPageData("medicalExpenses", "medicalExpenses", "MEDICAL_INSURANCE_PREMIUMS");

    String confirmationEmail = emailContentCreator.createFullClientConfirmationEmail(
        applicationData,
        "someNumber",
        programs,
        SnapExpeditedEligibility.UNDETERMINED,
        CcapExpeditedEligibility.UNDETERMINED,
        ENGLISH);
    assertThat(confirmationEmail).contains(
        "<html><body>We received your Minnesota Benefits application.<br><br>Your application "
        + "was submitted to Anoka County (555-5555) and Mille Lacs Band of Ojibwe (222-2222) on "
        + formattedTime +".<br><br>Confirmation number: <strong>#someNumber</strong><br>Application "
        + "status: <strong>In review</strong><br><br><strong>What's Next?</strong><br>Your next "
        + "steps in the application process may include uploading verification documents and being "
        + "available for calls and communications from your worker.<br><br><strong>Upload your documents."
        + "</strong><br>Do you want to send in documents or proof to your county or Tribal Nation "
        + "directly? Go to <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" "
        + "target=\"_blank\" rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> and click on ‘Upload Documents’. "
        + "The most requested documents are identification (ID), paystubs from your job or proof of "
        + "recent loss of job and proof of rent or shelter costs. <br><br><strong>Allow time for a worker "
        + "to review.</strong><br>Your application and documents will be reviewed by Anoka County (555-5555) "
        + "and Mille Lacs Band of Ojibwe (222-2222).  The time it takes to review your application "
        + "can vary. <br>Expect an eligibility worker to contact you by phone or mail with information "
        + "about your next steps. If you haven’t heard back about your application, know that work "
        + "is still in progress. <br>Before you submit another application, contact Anoka County "
        + "(555-5555) and Mille Lacs Band of Ojibwe (222-2222).<br><br><strong>Need help now?</strong><br>If "
        + "you are in a mental health crisis, call or text 988. <br>To connect with food, housing, "
        + "medical or other help, you can call 211, or text your zip code to 898-211, or visit "
        + "<a href=\"https://www.211.org/\" target=\"_blank\" rel=\"noopener noreferrer\"> 211.org</a>. "
        + "<br>If you need additional assistance to access food, contact Minnesota Food HelpLine "
        + "at 888-711-1151 or <a href=\"https://www.hungersolutions.org/\" target=\"_blank\" "
        + "rel=\"noopener noreferrer\">hungersolutions.org</a>.<br><br><strong>Have other "
        + "questions?</strong><br>Visit <a href=\"https://mnbenefits.mn.gov/faq\" target=\"_blank\" "
        + "rel=\"noopener noreferrer\">MNbenefits frequently asked questions</a>. "
        + "<br><br><p><strong>Verification Docs:</strong><br>If you need to submit verification "
        + "documents for your case, you can <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" "
        + "rel=\"noopener noreferrer\">return to MNbenefits.mn.gov</a> to upload documents "
        + "at any time.<br>You may need to share the following documents:<br><ul><li><strong>Proof "
        + "of Income:</strong> A document with employer and employee names and your total pre-tax income "
        + "from the last 30 days (or total hours worked and rate of pay). Example: Paystubs</li><li><strong>Proof "
        + "of Housing Costs:</strong> A document showing total amount paid for housing. "
        + "Examples: Rent receipts, lease, or mortgage statements</li><li><strong>Proof of Job "
        + "Loss:</strong> A document with your former employer’s name and signature, the last "
        + "day you worked, and date and amount of your final paycheck. Example: Pink slip</li><li><strong>Proof "
        + "of Medical Expenses:</strong> Documents showing medical expenses that you paid for.</li></ul></p></body></html>");

    String docRecEmail = emailContentCreator.createDocRecommendationEmail(applicationData);
    assertThat(docRecEmail).contains(
        "<html><body>Remember to upload documents on <a href=\"https://mnbenefits.mn.gov/?utm_medium=confirmationemail#later-docs-upload\" target=\"_blank\" "
        + "rel=\"noopener noreferrer\">MNbenefits.mn.gov</a> to support your MN Benefits application. You can use your phone to "
        + "take or upload pictures, or use your computer to upload documents.<br>If you have them, you should upload the following "
        + "documents:<br><ul><li><strong>Proof of Income:</strong> A document with employer and employee names and your total pre-tax "
        + "income from the last 30 days (or total hours worked and rate of pay). Example: Paystubs</li><li><strong>Proof of "
        + "Housing Costs:</strong> A document showing total amount paid for housing. Examples: Rent receipts, lease, or mortgage "
        + "statements</li><li><strong>Proof of Job Loss:</strong> A document with your former employer’s name and signature, the last day "
        + "you worked, and date and amount of your final paycheck. Example: Pink slip</li><li><strong>Proof of Medical Expenses:</strong> "
        + "Documents showing medical expenses that you paid for.</li></ul>If you have already uploaded these documents, you can ignore this reminder.</body></html>");
  }
}
