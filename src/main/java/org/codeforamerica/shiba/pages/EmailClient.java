package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;

public interface EmailClient {
    void sendConfirmationEmail(String recipient, String confirmationId, ExpeditedEligibility expeditedEligibility);
}
