package org.codeforamerica.shiba.output;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.codeforamerica.shiba.testutilities.TestUtils.ADMIN_EMAIL;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.exception.controller.ControllerAdvisor;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.pages.emails.EmailContentCreator;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = MOCK)
class ResendFailedEmailControllerTest {

  @Autowired
  MockMvc mockMvc;
  @MockBean
  private EmailContentCreator emailContentCreator;
  @MockBean
  private EmailClient emailClient;
  @MockBean
  private ApplicationRepository applicationRepository;
  @MockBean
  private SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
  @MockBean
  private CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;
  @MockBean
  private MonitoringService monitoringService;
  @MockBean
  private PdfGenerator pdfGenerator;
  @MockBean
  private ControllerAdvisor controllerAdvisor;

  @Test
  void shouldResendConfirmationEmail() throws Exception {

    var programs = List.of(Program.SNAP);
    String recipientEmail = "test@example.com";
    String emailContent = "content";
    SnapExpeditedEligibility snapExpeditedEligibility = SnapExpeditedEligibility.ELIGIBLE;
    CcapExpeditedEligibility ccapExpeditedEligibility = CcapExpeditedEligibility.ELIGIBLE;
    String confirmationId = "6730000290";

    var applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(programs)
        .withPageData("matchInfo", "email", recipientEmail)
        .build();

    when(emailContentCreator.createFullClientConfirmationEmail(applicationData,
        confirmationId,
        programs,
        snapExpeditedEligibility,
        ccapExpeditedEligibility,
        Locale.ENGLISH)).thenReturn(emailContent);

    Application application = Application.builder()
        .id(confirmationId)
        .county(County.Hennepin)
        .timeToComplete(Duration.ofSeconds(12415))
        .completedAt(ZonedDateTime.now(ZoneOffset.UTC))
        .applicationData(applicationData)
        .build();

    when(applicationRepository.find(confirmationId)).thenReturn(application);
    when(snapExpeditedEligibilityDecider.decide(applicationData))
        .thenReturn(SnapExpeditedEligibility.ELIGIBLE);
    when(ccapExpeditedEligibilityDecider.decide(applicationData))
        .thenReturn(CcapExpeditedEligibility.NOT_ELIGIBLE);
    String fileContent = "someContent";
    String fileName = "someFileName";
    when(pdfGenerator.generate(eq(confirmationId), eq(CAF), eq(CLIENT)))
        .thenReturn(new ApplicationFile(fileContent.getBytes(), fileName));

    mockMvc.perform(get("/resend-confirmation-email/" + confirmationId)
            .with(oauth2Login().attributes(attrs -> attrs.put("email", ADMIN_EMAIL))))
        .andExpect(content()
            .string("Successfully resent confirmation email for application: " + confirmationId));
    verify(emailClient)
        .sendConfirmationEmail(applicationData, recipientEmail, confirmationId, programs,
            SnapExpeditedEligibility.ELIGIBLE, CcapExpeditedEligibility.NOT_ELIGIBLE,
            List.of(new ApplicationFile(fileContent.getBytes(), fileName)), Locale.ENGLISH);
  }
}
