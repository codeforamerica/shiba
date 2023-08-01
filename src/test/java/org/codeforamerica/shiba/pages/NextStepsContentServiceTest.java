package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.testutilities.AbstractPageControllerTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.ResultActions;

public class NextStepsContentServiceTest extends AbstractPageControllerTest {
  
  @SuppressWarnings("unused")
  private static Stream<Arguments> successMessageTestCases() {
    return Stream.of(
        Arguments.of(
            "Only Expedited SNAP",
            List.of("SNAP"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.UNDETERMINED,
            List.of(
                "Within 24 hours, expect a call from your county or Tribal Nation about your food assistance application.",
                "If you don't hear from your county or Tribal Nation within 7 days or want an update on your case, please call your county or Tribal Nation.")
        ),
        Arguments.of(
            "Only Non-expedited SNAP",
            List.of("SNAP"),
            SnapExpeditedEligibility.NOT_ELIGIBLE,
            CcapExpeditedEligibility.UNDETERMINED,
            List.of(
                "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your food support application. The letter will explain your next steps.",
                "Call your county or Tribal Nation if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Expedited SNAP + Expedited CCAP",
            List.of("SNAP", "CCAP"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of(
                "Within 24 hours, expect a call from your county or Tribal Nation about your food assistance application.",
                "Within 5 days, your county or Tribal Nation will determine your childcare assistance case and send you a letter in the mail.",
                "Call your county or Tribal Nation if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Expedited SNAP + non-expedited CCAP",
            List.of("SNAP", "CCAP"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.NOT_ELIGIBLE,
            List.of(
                "Within 24 hours, expect a call from your county or Tribal Nation about your food assistance application.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your childcare application. The letter will explain your next steps.",
                "Call your county or Tribal Nation if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Expedited CCAP + non-expedited SNAP",
            List.of("SNAP", "CCAP"),
            SnapExpeditedEligibility.NOT_ELIGIBLE,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of(
                "Within 5 days, your county or Tribal Nation will determine your childcare assistance case and send you a letter in the mail.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your food support application. The letter will explain your next steps.",
                "Call your county or Tribal Nation if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Only Expedited CCAP",
            List.of("CCAP"),
            SnapExpeditedEligibility.UNDETERMINED,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of(
                "Within 5 days, your county or Tribal Nation will determine your childcare assistance case and send you a letter in the mail.",
                "Call your county or Tribal Nation if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Only Non-expedited CCAP",
            List.of("CCAP"),
            SnapExpeditedEligibility.UNDETERMINED,
            CcapExpeditedEligibility.NOT_ELIGIBLE,
            List.of(
                "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your childcare application. The letter will explain your next steps.",
                "Call your county or Tribal Nation if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Expedited SNAP + any other program",
            List.of("SNAP", "GRH"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.UNDETERMINED,
            List.of(
                "Within 24 hours, expect a call from your county or Tribal Nation about your food assistance application.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your housing application. The letter will explain your next steps.",
                "If you don't hear from your county or Tribal Nation within 7 days or want an update on your case, please call your county or Tribal Nation.")
        ),
        Arguments.of(
            "Expedited SNAP + multiple other programs",
            List.of("SNAP", "GRH", "EA"),
            SnapExpeditedEligibility.ELIGIBLE,
            CcapExpeditedEligibility.UNDETERMINED,
            List.of(
                "Within 24 hours, expect a call from your county or Tribal Nation about your food assistance application.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your housing and emergency assistance application. The letter will explain your next steps.",
                "If you don't hear from your county or Tribal Nation within 7 days or want an update on your case, please call your county or Tribal Nation.")
        ),
        Arguments.of(
            "Expedited CCAP + any other program besides SNAP",
            List.of("CCAP", "GRH"),
            SnapExpeditedEligibility.UNDETERMINED,
            CcapExpeditedEligibility.ELIGIBLE,
            List.of(
                "Within 5 days, your county or Tribal Nation will determine your childcare assistance case and send you a letter in the mail.",
                "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your housing application. The letter will explain your next steps.",
                "Call your county or Tribal Nation if you don’t hear from them in the time period we’ve noted.")
        ),
        Arguments.of(
            "Non-expedited CCAP + any other program besides SNAP",
            List.of("CCAP", "GRH"),
            SnapExpeditedEligibility.UNDETERMINED,
            CcapExpeditedEligibility.NOT_ELIGIBLE,
            List.of(
                "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your childcare and housing application. The letter will explain your next steps.",
                "Call your county or Tribal Nation if you don’t hear from them in the time period we’ve noted.")
        )
    );
  }
  
  @BeforeEach
  void setUp() {
    applicationData.setStartTimeOnce(Instant.now());
    var id = "some-id";
    applicationData.setId(id);
    new TestApplicationDataBuilder(applicationData)
        .withPageData("contactInfo", "email", "test@example.com");

    Application application = Application.builder()
        .id(id)
        .county(County.Hennepin)
        .timeToComplete(Duration.ofSeconds(12415))
        .completedAt(ZonedDateTime.now(ZoneOffset.UTC))
        .applicationData(applicationData)
        .build();
    when(applicationRepository.find(any())).thenReturn(application);
  }


  @SuppressWarnings("unused")
  @ParameterizedTest(name = "{0}")
  @MethodSource("org.codeforamerica.shiba.pages.NextStepsContentServiceTest#successMessageTestCases")
  @Disabled("This test is disabled because it requires feature-flag.enhanced-next-steps: off. Remove test in final implementation.")
  void displaysCorrectSuccessMessageForApplicantPrograms(String testName, List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility, List<String> expectedMessages)
      throws Exception {
    setPrograms(programs);

    setSubworkflows(new Subworkflows());

    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }

  @Test
  @Disabled("This test is disabled because it requires feature-flag.enhanced-next-steps: off. Remove test in final implementation.")
  void displaysCorrectSuccessMessageForHouseholdMemberPrograms() throws Exception {
    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(List.of("SNAP"))
        .withHouseholdMemberPrograms(List.of("GRH", "EA"));

    var snapExpeditedEligibility = SnapExpeditedEligibility.ELIGIBLE;
    var ccapExpeditedEligibility = CcapExpeditedEligibility.UNDETERMINED;
    List<String> expectedMessages = List.of(
        "Within 24 hours, expect a call from your county or Tribal Nation about your food assistance application.",
        "In the next 7-10 days, expect to get a letter in the mail from your county or Tribal Nation about your housing and emergency assistance application. The letter will explain your next steps.",
        "If you don't hear from your county or Tribal Nation within 7 days or want an update on your case, please call your county or Tribal Nation.");
    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }

  @Test
  /**
   * Tests the message generated for the "Upload documents" accordion when no documents have been uploaded.
   * @throws Exception
   */
  void displaysCorrectUploadDocumentsAccordionMessageWhenNoDocumentsUploaded() throws Exception {
	new TestApplicationDataBuilder(applicationData).withApplicantPrograms(List.of("SNAP"));

	applicationData.setUploadedDocs(Collections.emptyList());
    List<String> expectedMessages = List.of(
    		"You did not upload documents with your application today. To upload documents later, you can return to our homepage and click on ‘Upload documents’ to get started.",
    		"Expect an eligibility worker to contact you by phone or mail with information about your next steps. The time it takes to review applications can vary.");
    var snapExpeditedEligibility = SnapExpeditedEligibility.NOT_ELIGIBLE;
    var ccapExpeditedEligibility = CcapExpeditedEligibility.UNDETERMINED;
    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }

  @Test
  /**
   * Tests the message generated for the "Upload documents" accordion when one or more documents have been uploaded.
   * @throws Exception
   */
  void displaysCorrectUploadDocumentsAccordionMessageWhenADocumentIsUploaded() throws Exception {
	new TestApplicationDataBuilder(applicationData).withApplicantPrograms(List.of("SNAP"));

	UploadedDocument uploadedDocument = new UploadedDocument("paystub.pdf", "1000000001/aaaaaaaa-1111-2222-bbbb-cccccccccccc",
			                            "1000000001/thumbnail-aaaaaaaa-1111-2222-bbbb-cccccccccccc", "application/pdf",	25000);
	applicationData.setUploadedDocs(List.of(uploadedDocument));
    List<String> expectedMessages = List.of(
    		"We received the documents you uploaded with your application. If you need to upload more documents later, you can return to our homepage and click on ‘Upload documents’ to get started.",
    		"Expect an eligibility worker to contact you by phone or mail with information about your next steps. The time it takes to review applications can vary.");
    var snapExpeditedEligibility = SnapExpeditedEligibility.NOT_ELIGIBLE;
    var ccapExpeditedEligibility = CcapExpeditedEligibility.UNDETERMINED;
    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }
  
  @Test
  /**
   * Tests the message generated for the "Expedited SNAP" accordion without documents uploaded.
   * @throws Exception
   */
  void displaysCorrectReviewAccordionMessageWhenSNAPExpedited() throws Exception {
	new TestApplicationDataBuilder(applicationData).withApplicantPrograms(List.of("SNAP"));
	
    List<String> expectedMessages = List.of(
    		"You did not upload documents with your application today. To upload documents later, you can return to our homepage and click on ‘Upload documents’ to get started.",
    		"Within the next 5 days, expect a phone call from an eligibility worker with information about your next steps.");
    var snapExpeditedEligibility = SnapExpeditedEligibility.ELIGIBLE;
    var ccapExpeditedEligibility = CcapExpeditedEligibility.UNDETERMINED;
    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }
  
  @Test
  /**
   * Tests the message generated for the "Expedited CCAP" accordion without documents uploaded.
   * @throws Exception
   */
  void displaysCorrectReviewAccordionMessageWhenCCAPExpedited() throws Exception {
	new TestApplicationDataBuilder(applicationData).withApplicantPrograms(List.of("SNAP"));
	
    List<String> expectedMessages = List.of(
    		"You did not upload documents with your application today. To upload documents later, you can return to our homepage and click on ‘Upload documents’ to get started.",
    		"Within the next 5 days, expect a phone call or letter in the mail from an eligibility worker with information about your next steps.");
    var snapExpeditedEligibility = SnapExpeditedEligibility.UNDETERMINED;
    var ccapExpeditedEligibility = CcapExpeditedEligibility.ELIGIBLE;
    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }
  
  @Test
  /**
   * Tests the message generated for the "Other programs not expedited" accordion without documents uploaded.
   * @throws Exception
   */
  void displaysCorrectReviewAccordionMessageWhenForOtherPrograms() throws Exception {
	new TestApplicationDataBuilder(applicationData).withApplicantPrograms(List.of("SNAP"));
	
    List<String> expectedMessages = List.of(
    		"You did not upload documents with your application today. To upload documents later, you can return to our homepage and click on ‘Upload documents’ to get started.",
    		"Expect an eligibility worker to contact you by phone or mail with information about your next steps. The time it takes to review applications can vary.");
    var snapExpeditedEligibility = SnapExpeditedEligibility.UNDETERMINED;
    var ccapExpeditedEligibility = CcapExpeditedEligibility.UNDETERMINED;
    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }
  
  @Test
  /**
   * Tests the message generated for the "Allow time for review" accordion when snapExpeditedEligibility and ccapExpeditedEligibility are both true.  When SNAP is expedited it doesn't matter whether or not CCAP is expedited. 
   * @throws Exception
   */
  void displaysCorrectAllowTimeForReviewAccordionMessageWhenSnapAndCcapExpedited() throws Exception {
	new TestApplicationDataBuilder(applicationData).withApplicantPrograms(List.of("SNAP", "CCAP", "CASH"));
    List<UploadedDocument> uploadDocumuments = new ArrayList<UploadedDocument>();
	applicationData.setUploadedDocs(uploadDocumuments);
    List<String> expectedMessages = List.of(
    		"You did not upload documents with your application today. To upload documents later, you can return to our homepage and click on ‘Upload documents’ to get started.",
    		"Within the next 5 days, expect a phone call from an eligibility worker with information about your next steps.");
    var snapExpeditedEligibility = SnapExpeditedEligibility.ELIGIBLE;
    var ccapExpeditedEligibility = CcapExpeditedEligibility.ELIGIBLE;
    assertCorrectMessage(snapExpeditedEligibility, ccapExpeditedEligibility, expectedMessages);
  }
  

  private void setSubworkflows(Subworkflows subworkflows) {
    applicationData.setSubworkflows(subworkflows);
  }

  private void setPrograms(List<String> programs) {
    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(programs);
  }

  private void assertCorrectMessage(SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility, List<String> expectedMessages)
      throws Exception {
    when(snapExpeditedEligibilityDecider.decide(any())).thenReturn(snapExpeditedEligibility);
    when(ccapExpeditedEligibilityDecider.decide(any())).thenReturn(ccapExpeditedEligibility);
    
    ResultActions resultActions = mockMvc.perform(
            get("/pages/nextSteps").session(new MockHttpSession()))
        .andExpect(status().isOk());
    FormPage formPage = new FormPage(resultActions);
    List<String> nextStepSections = formPage.getElementsByClassName("next-step-section").stream()
        .map(Element::text).collect(Collectors.toList());
    assertThat(nextStepSections).containsExactly(expectedMessages.toArray(new String[0]));
  }
}
