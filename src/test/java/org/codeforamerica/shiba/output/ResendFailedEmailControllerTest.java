package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.*;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.pages.emails.EmailContentCreator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("test")
@WebMvcTest(ResendFailedEmailController.class)
class ResendFailedEmailControllerTest {
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

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void shouldResendConfirmationEmail() throws Exception {

        var programs = List.of(Program.SNAP);
        String recipientEmail = "test@email.com";
        String emailContent = "content";
        SnapExpeditedEligibility snapExpeditedEligibility = SnapExpeditedEligibility.ELIGIBLE;
        CcapExpeditedEligibility ccapExpeditedEligibility = CcapExpeditedEligibility.ELIGIBLE;
        String confirmationId = "6730000290";

        var applicationData = new ApplicationData();
        when(emailContentCreator.createClientHTML(applicationData,
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
        when(snapExpeditedEligibilityDecider.decide(applicationData)).thenReturn(SnapExpeditedEligibility.ELIGIBLE);
        when(ccapExpeditedEligibilityDecider.decide(applicationData)).thenReturn(CcapExpeditedEligibility.NOT_ELIGIBLE);
        String fileContent = "someContent";
        String fileName = "someFileName";
        when(pdfGenerator.generate(eq(confirmationId), eq(CAF), eq(CLIENT))).thenReturn(new ApplicationFile(fileContent.getBytes(), fileName));

        PagesData pagesData = new PagesDataBuilder().build(List.of(
                new PageDataBuilder("matchInfo", Map.of("email", List.of(recipientEmail))),
                new PageDataBuilder("choosePrograms", Map.of("programs", programs))

        ));
        applicationData.setPagesData(pagesData);

        mockMvc.perform(get("/resend-confirmation-email/" + confirmationId)).andExpect(content().string("Successfully resent confirmation email for application: " + confirmationId));
        verify(emailClient).sendConfirmationEmail(applicationData, recipientEmail, confirmationId, programs, SnapExpeditedEligibility.ELIGIBLE, CcapExpeditedEligibility.NOT_ELIGIBLE, List.of(new ApplicationFile(fileContent.getBytes(), fileName)), Locale.ENGLISH);
    }
}