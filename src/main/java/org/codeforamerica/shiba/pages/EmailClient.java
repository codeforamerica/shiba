package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;

import java.time.ZonedDateTime;

public interface EmailClient {
    void sendConfirmationEmail(String recipientEmail,
                               String confirmationId,
                               ExpeditedEligibility expeditedEligibility,
                               ApplicationFile applicationFile);

    void sendCaseWorkerEmail(String recipientEmail,
                             String recipientName,
                             String confirmationId,
                             ApplicationFile applicationFile);

    void sendDownloadCafAlertEmail(String confirmationId, String ip);

    void sendNonPartnerCountyAlert(String applicationId, ZonedDateTime submissionTime);
}
