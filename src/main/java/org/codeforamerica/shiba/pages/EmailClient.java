package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;

public interface EmailClient {
    void sendConfirmationEmail(String recipientEmail,
                               String confirmationId,
                               ExpeditedEligibility expeditedEligibility,
                               ApplicationFile applicationFile);

    void sendCaseWorkerEmail(String recipientEmail,
                             String recipientName,
                             ApplicationFile applicationFile);
}
