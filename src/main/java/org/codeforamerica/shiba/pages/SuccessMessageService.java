package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SuccessMessageService {
    private static final String PROGRAM_CCAP = "CCAP";
    private static final String PROGRAM_SNAP = "SNAP";
    private final MessageSource messageSource;

    public SuccessMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @SuppressWarnings("ConstantConditions")
    public String getSuccessMessage(List<String> applicantPrograms, SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility, Locale locale) {
        boolean onlyCcap = applicantPrograms.stream().allMatch(p -> p.equals(PROGRAM_CCAP));
        boolean onlySnap = applicantPrograms.stream().allMatch(p -> p.equals(PROGRAM_SNAP));
        boolean isSnapAndCcapOnly = applicantPrograms.stream().allMatch(p -> p.equals(PROGRAM_CCAP) || p.equals(PROGRAM_SNAP)) && !onlyCcap && !onlySnap;
        boolean isSnapExpeditedEligible = snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE);
        boolean isCcapExpeditedEligible = ccapExpeditedEligibility.equals(CcapExpeditedEligibility.ELIGIBLE);

        String messageKey = "success.your-county-will-contact-within-one-week";
        if (onlySnap && isSnapExpeditedEligible) {
            messageKey = "success.expedited-snap-only";
        } else if (onlySnap && !isSnapExpeditedEligible) {
            messageKey = "success.non-expedited-snap-only";
        } else if (isSnapAndCcapOnly && isSnapExpeditedEligible && isCcapExpeditedEligible) {
            messageKey = "success.expedited-snap-expedited-ccap";
        } else if (isSnapAndCcapOnly && isSnapExpeditedEligible && !isCcapExpeditedEligible) {
            messageKey = "success.expedited-snap-nonexpedited-ccap";
        } else if (isSnapAndCcapOnly && !isSnapExpeditedEligible && isCcapExpeditedEligible) {
            messageKey = "success.expedited-ccap-nonexpedited-snap";
        } else if(onlyCcap && isCcapExpeditedEligible && !isSnapExpeditedEligible) {
            messageKey = "success.expedited-ccap-only";
        }
        return messageSource.getMessage(messageKey, null, locale);
    }
}
