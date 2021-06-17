package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.*;
import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.util.List;
import java.util.Locale;

public interface EmailClient {
    void sendConfirmationEmail(ApplicationData applicationData,
                               String recipientEmail,
                               String confirmationId,
                               List<String> programs,
                               SnapExpeditedEligibility snapExpeditedEligibility,
                               CcapExpeditedEligibility ccapExpeditedEligibility,
                               List<ApplicationFile> applicationFiles,
                               Locale locale);

    void sendCaseWorkerEmail(String recipientEmail,
                             String recipientName,
                             String confirmationId,
                             ApplicationFile applicationFile);

    void sendDownloadCafAlertEmail(String confirmationId, String ip, Locale locale);
    
    void sendHennepinDocUploadsEmails(Application application);

    void sendLaterDocsConfirmationEmail(String recipientEmail, Locale locale);
}
