package org.codeforamerica.shiba.pages.emails;

import java.util.List;
import java.util.Locale;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.ExpeditedCcap;
import org.codeforamerica.shiba.output.caf.ExpeditedSnap;
import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface EmailClient {

  void sendConfirmationEmail(ApplicationData applicationData,
      String recipientEmail,
      String confirmationId,
      List<String> programs,
      ExpeditedSnap expeditedSnap,
      ExpeditedCcap expeditedCcap,
      List<ApplicationFile> applicationFiles,
      Locale locale);

  void sendShortConfirmationEmail(ApplicationData applicationData,
      String recipientEmail,
      String confirmationId,
      List<String> programs,
      ExpeditedSnap expeditedSnap,
      ExpeditedCcap expeditedCcap,
      List<ApplicationFile> applicationFiles,
      Locale locale);

  void sendNextStepsEmail(ApplicationData applicationData,
      String recipientEmail,
      String confirmationId,
      List<String> programs,
      ExpeditedSnap expeditedSnap,
      ExpeditedCcap expeditedCcap,
      List<ApplicationFile> applicationFiles,
      Locale locale);

  void sendCaseWorkerEmail(String recipientEmail,
      String recipientName,
      String confirmationId,
      ApplicationFile applicationFile);

  void sendDownloadCafAlertEmail(String confirmationId, String ip, Locale locale);

  void sendHennepinDocUploadsEmails(Application application);

  void sendLaterDocsConfirmationEmail(String recipientEmail, Locale locale);

  void resubmitFailedEmail(String recipientEmail, Document document,
      ApplicationFile applicationFile, Application application);
}
