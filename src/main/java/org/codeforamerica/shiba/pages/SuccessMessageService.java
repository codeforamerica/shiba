package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.internationalization.InternationalizationUtils;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class SuccessMessageService {
    private final MessageSource messageSource;

    public SuccessMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getSuccessMessage(List<String> applicantPrograms, SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility, Locale locale) {
        boolean hasSnap = applicantPrograms.stream().anyMatch(p -> p.equals(Program.SNAP));
        boolean onlyCcap = applicantPrograms.stream().allMatch(p -> p.equals(Program.CCAP));
        boolean isSnapExpeditedEligible = snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE);
        boolean isCcapExpeditedEligible = ccapExpeditedEligibility.equals(CcapExpeditedEligibility.ELIGIBLE);
        boolean notCcap = applicantPrograms.stream().noneMatch(p -> p.equals(Program.CCAP));
        boolean hasNonExpeditedSnap = hasSnap && !isSnapExpeditedEligible;

        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
        List<String> messageKeys = new ArrayList<>();

        // Snap timing
        if (isSnapExpeditedEligible) {
            messageKeys.add(lms.getMessage("success.expedited-snap-timing"));
        }

        // Ccap timing
        if (isCcapExpeditedEligible) {
            messageKeys.add(lms.getMessage("success.expedited-ccap-timing"));
        }

        // Contact Promise
        List<String> programNamesForNextStepLetter = getProgramNames(applicantPrograms, hasNonExpeditedSnap, isCcapExpeditedEligible, lms);
        if (!programNamesForNextStepLetter.isEmpty()) {
            String programsInNextStepLetter = InternationalizationUtils.listToString(programNamesForNextStepLetter, lms);
            messageKeys.add(lms.getMessage("success.contact-promise", new String[]{programsInNextStepLetter}));
        }

        // Interview expectation
        if (!onlyCcap) {
            messageKeys.add(lms.getMessage("success.you-will-need-to-complete-an-interview"));
        }

        // Suggested Action
        if (isSnapExpeditedEligible && notCcap) {
            messageKeys.add(lms.getMessage("success.expedited-snap-suggested-action"));
        } else {
            messageKeys.add(lms.getMessage("success.standard-suggested-action"));
        }

        return String.join("<br><br>", messageKeys);
    }

    private List<String> getProgramNames(List<String> applicantPrograms, boolean hasNonExpeditedSnap, boolean isCcapExpeditedEligible, LocaleSpecificMessageSource ms) {
        boolean hasCcap = applicantPrograms.stream().anyMatch(p -> p.equals(Program.CCAP));
        boolean hasNonExpeditedCcap = hasCcap && !isCcapExpeditedEligible;
        boolean hasGrh = applicantPrograms.stream().anyMatch(p -> p.equals(Program.GRH));
        boolean hasCash = applicantPrograms.stream().anyMatch(p -> p.equals(Program.CASH));
        boolean hasEa = applicantPrograms.stream().anyMatch(p -> p.equals(Program.EA));
        List<String> nextStepLetterProgramNames = new ArrayList<>();
        if (hasNonExpeditedCcap) {
            nextStepLetterProgramNames.add(ms.getMessage("success.childcare"));
        }
        if (hasGrh) {
            nextStepLetterProgramNames.add(ms.getMessage("success.housing"));
        }
        if (hasEa) {
            nextStepLetterProgramNames.add((ms.getMessage("success.emergency-assistance")));
        }
        if (hasCash) {
            nextStepLetterProgramNames.add((ms.getMessage("success.cash-support")));
        }
        if (hasNonExpeditedSnap) {
            nextStepLetterProgramNames.add(ms.getMessage("success.food-support"));
        }

        return nextStepLetterProgramNames;
    }

}
