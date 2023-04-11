package org.codeforamerica.shiba.pages.emails;

import java.util.List;
import java.util.Locale;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface EmailClient {

  void sendConfirmationEmail(ApplicationData applicationData,
      String recipientEmail,
      String confirmationId,
      List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      List<ApplicationFile> applicationFiles,
      Locale locale);

  void sendShortConfirmationEmail(ApplicationData applicationData,
      String recipientEmail,
      String confirmationId,
      List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      List<ApplicationFile> applicationFiles,
      Locale locale);

  void sendNextStepsEmail(ApplicationData applicationData,
      String recipientEmail,
      String confirmationId,
      List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      List<ApplicationFile> applicationFiles,
      Locale locale);

  void sendDownloadCafAlertEmail(String confirmationId, String ip, Locale locale);

  void sendLaterDocsConfirmationEmail(Application application, 
		  String confirmationId,
		  String recipientEmail, 
		  Locale locale);
  
  void sendHealthcareRenewalConfirmationEmail(Application application, 
		  String confirmationId,
		  String recipientEmail, 
		  Locale locale);

  void resubmitFailedEmail(String recipientEmail, Document document,
      ApplicationFile applicationFile, Application application);

  void sendEmail(String subject, String senderEmail, String recipientEmail, String emailBody);

  void sendEmail(String subject, String senderEmail, String recipientEmail, String emailBody,
      List<ApplicationFile> attachments);

  void sendEmail(
      String subject,
      String senderEmail,
      String recipientEmail,
      List<String> emailsToCC,
      String emailBody,
      List<ApplicationFile> attachments,
      boolean requireTls);
}
