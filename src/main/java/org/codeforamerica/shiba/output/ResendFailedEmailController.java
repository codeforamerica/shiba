package org.codeforamerica.shiba.output;


import static org.codeforamerica.shiba.output.Recipient.CLIENT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ResendFailedEmailController {

  private final ApplicationRepository applicationRepository;
  private final EmailClient emailClient;
  private final SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
  private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;
  private final PdfGenerator pdfGenerator;

  public ResendFailedEmailController(
      ApplicationRepository applicationRepository,
      EmailClient emailClient,
      SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider,
      CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider,
      PdfGenerator pdfGenerator) {
    this.applicationRepository = applicationRepository;
    this.emailClient = emailClient;
    this.snapExpeditedEligibilityDecider = snapExpeditedEligibilityDecider;
    this.ccapExpeditedEligibilityDecider = ccapExpeditedEligibilityDecider;
    this.pdfGenerator = pdfGenerator;
  }

  @GetMapping("/resend-confirmation-email/{applicationId}")
  @ResponseBody
  public String resendConfirmationEmail(@PathVariable String applicationId) {
    Application application = applicationRepository.find(applicationId);
    ApplicationData applicationData = application.getApplicationData();

    EmailParser.parse(applicationData)
        .ifPresent(email -> {
          if (application.getFlow() == FlowType.LATER_DOCS) {
            emailClient.sendLaterDocsConfirmationEmail(application, applicationId, email, LocaleContextHolder.getLocale());
          } else {
            SnapExpeditedEligibility snapExpeditedEligibility = snapExpeditedEligibilityDecider
                .decide(application.getApplicationData());
            CcapExpeditedEligibility ccapExpeditedEligibility = ccapExpeditedEligibilityDecider
                .decide(application.getApplicationData());
            List<Document> docs = DocumentListParser.parse(applicationData);
            List<ApplicationFile> pdfs = docs.stream()
                .map(doc -> pdfGenerator.generate(applicationId, doc, CLIENT))
                .collect(Collectors.toList());
            emailClient.sendConfirmationEmail(applicationData, email, applicationId,
                new ArrayList<>(applicationData.getApplicantAndHouseholdMemberPrograms()),
                snapExpeditedEligibility, ccapExpeditedEligibility, pdfs,
                applicationData.getLocale());
          }
        });

    return "Successfully resent confirmation email for application: " + applicationId;
  }
}
