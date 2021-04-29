package org.codeforamerica.shiba.pages.emails;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;

import java.util.List;
import java.util.Locale;

public interface EmailClient {
    void sendConfirmationEmail(String recipientEmail,
                               String confirmationId,
                               SnapExpeditedEligibility snapExpeditedEligibility,
                               List<ApplicationFile> applicationFiles,
                               Locale locale);

    void sendCaseWorkerEmail(String recipientEmail,
                             String recipientName,
                             String confirmationId,
                             ApplicationFile applicationFile);

    void sendDownloadCafAlertEmail(String confirmationId, String ip, Locale locale);
    
    void sendHennepinDocUploadsEmail(Application application);  
}
