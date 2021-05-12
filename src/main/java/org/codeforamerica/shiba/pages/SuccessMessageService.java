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
        boolean onlyCcap = programs.stream().allMatch(p -> p.equals(CCAP));
        boolean notCcap = programs.stream().noneMatch(p -> p.equals(CCAP));
        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);

        List<String> messages = new ArrayList<>();

        // Snap timing
        if (isSnapExpeditedEligible) {
            messages.add(lms.getMessage("success.expedited-snap-timing"));
        }

        // Ccap timing
        if (isCcapExpeditedEligible) {
            messages.add(lms.getMessage("success.expedited-ccap-timing"));
        }

        // Contact Promise
        List<String> programNamesForNextStepLetter = getNextStepLetterPrograms(programs, isSnapExpeditedEligible, isCcapExpeditedEligible, lms);
        if (!programNamesForNextStepLetter.isEmpty()) {
            String programsInNextStepLetter = listToString(programNamesForNextStepLetter, lms);
            messages.add(lms.getMessage("success.contact-promise", new String[]{programsInNextStepLetter}));
        }

        // Interview expectation
        if (!onlyCcap) {
            messages.add(lms.getMessage("success.you-will-need-to-complete-an-interview"));
        }

        // Suggested Action
        if (isSnapExpeditedEligible && notCcap) {
            messages.add(lms.getMessage("success.expedited-snap-suggested-action"));
        } else {
            messages.add(lms.getMessage("success.standard-suggested-action"));
        }

        return String.join("<br><br>", messages);
    }

    private List<String> getNextStepLetterPrograms(List<String> allPrograms,
                                                   boolean isSnapExpeditedEligible,
                                                   boolean isCcapExpeditedEligible,
                                                   LocaleSpecificMessageSource ms) {
        boolean hasNonExpeditedSnap = hasProgram(SNAP, allPrograms) && !isSnapExpeditedEligible;
        boolean hasNonExpeditedCcap = hasProgram(CCAP, allPrograms) && !isCcapExpeditedEligible;
        List<String> nextStepLetterPrograms = new ArrayList<>();
        if (hasNonExpeditedCcap) {
            nextStepLetterPrograms.add(ms.getMessage("success.childcare"));
        }
        if (hasProgram(GRH, allPrograms)) {
            nextStepLetterPrograms.add(ms.getMessage("success.housing"));
        }
        if (hasProgram(EA, allPrograms)) {
            nextStepLetterPrograms.add((ms.getMessage("success.emergency-assistance")));
        }
        if (hasProgram(CASH, allPrograms)) {
            nextStepLetterPrograms.add((ms.getMessage("success.cash-support")));
        }
        if (hasNonExpeditedSnap) {
            nextStepLetterPrograms.add(ms.getMessage("success.food-support"));
        }

        return nextStepLetterPrograms;
    }

    private boolean hasProgram(String program, List<String> programs) {
        return programs.stream().anyMatch(p -> p.equals(program));
    }
}
