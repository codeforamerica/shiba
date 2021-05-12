package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.codeforamerica.shiba.Program.*;
import static org.codeforamerica.shiba.internationalization.InternationalizationUtils.listToString;

@Service
public class SuccessMessageService {
    private final MessageSource messageSource;

    public SuccessMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getSuccessMessage(List<String> programs,
                                    SnapExpeditedEligibility snapExpeditedEligibility,
                                    CcapExpeditedEligibility ccapExpeditedEligibility,
                                    Locale locale) {
        boolean isSnapExpeditedEligible = snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE);
        boolean isCcapExpeditedEligible = ccapExpeditedEligibility.equals(CcapExpeditedEligibility.ELIGIBLE);

        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);

        List<String> paragraphs = new ArrayList<>();

        // Snap timing
        if (isSnapExpeditedEligible) {
            paragraphs.add(lms.getMessage("success.expedited-snap-timing"));
        }

        // Ccap timing
        if (isCcapExpeditedEligible) {
            paragraphs.add(lms.getMessage("success.expedited-ccap-timing"));
        }

        // Contact Promise
        List<String> nextStepLetterPrograms = getNextStepLetterPrograms(programs, isSnapExpeditedEligible, isCcapExpeditedEligible, lms);
        if (!nextStepLetterPrograms.isEmpty()) {
            String programsInNextStepLetter = listToString(nextStepLetterPrograms, lms);
            paragraphs.add(lms.getMessage("success.contact-promise", new String[]{programsInNextStepLetter}));
        }

        // Interview expectation
        boolean onlyCcap = programs.stream().allMatch(p -> p.equals(CCAP));
        if (!onlyCcap) {
            paragraphs.add(lms.getMessage("success.you-will-need-to-complete-an-interview"));
        }

        // Suggested Action
        if (isSnapExpeditedEligible && !programs.contains(CCAP)) {
            paragraphs.add(lms.getMessage("success.expedited-snap-suggested-action"));
        } else {
            paragraphs.add(lms.getMessage("success.standard-suggested-action"));
        }

        return String.join("<br><br>", paragraphs);
    }

    private List<String> getNextStepLetterPrograms(List<String> allPrograms,
                                                   boolean isSnapExpeditedEligible,
                                                   boolean isCcapExpeditedEligible,
                                                   LocaleSpecificMessageSource ms) {
        List<String> nextStepLetterPrograms = new ArrayList<>();

        boolean hasNonExpeditedCcap = allPrograms.contains(CCAP) && !isCcapExpeditedEligible;
        if (hasNonExpeditedCcap) {
            nextStepLetterPrograms.add(ms.getMessage("success.childcare"));
        }

        if (allPrograms.contains(GRH)) {
            nextStepLetterPrograms.add(ms.getMessage("success.housing"));
        }

        if (allPrograms.contains(EA)) {
            nextStepLetterPrograms.add((ms.getMessage("success.emergency-assistance")));
        }

        if (allPrograms.contains(CASH)) {
            nextStepLetterPrograms.add((ms.getMessage("success.cash-support")));
        }

        boolean hasNonExpeditedSnap = allPrograms.contains(SNAP) && !isSnapExpeditedEligible;
        if (hasNonExpeditedSnap) {
            nextStepLetterPrograms.add(ms.getMessage("success.food-support"));
        }

        return nextStepLetterPrograms;
    }
}
