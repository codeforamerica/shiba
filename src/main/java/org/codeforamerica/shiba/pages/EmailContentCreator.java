package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.springframework.stereotype.Component;

import static org.codeforamerica.shiba.output.caf.ExpeditedEligibility.ELIGIBLE;

@Component
public class EmailContentCreator {
    String createClientHTML(String confirmationId, ExpeditedEligibility expeditedEligibility) {
        String eligibilitySpecificVerbiage;
        if (ELIGIBLE == expeditedEligibility) {
            eligibilitySpecificVerbiage = "Your county will call you in the next 3 days for your phone interview.";
        } else {
            eligibilitySpecificVerbiage = "Your county will mail you a notice that will arrive in the next week.";
        }
        return String.format("We received your Minnesota Benefits application. %s <br><br> Confirmation number: <strong>#%s</strong><br>Application status: <strong>in review</strong><br><br>**This is an automated message. Please do not reply to this message.**", eligibilitySpecificVerbiage, confirmationId);
    }

    String createCaseworkerHTML() {
        return "<html><body><p>This application was submitted on behalf of a client.</p><p>Please keep the file pages in the order they appear in the file; intake workers will be looking for the cover page in front of the CAF.</p></body></html>";
    }
}
