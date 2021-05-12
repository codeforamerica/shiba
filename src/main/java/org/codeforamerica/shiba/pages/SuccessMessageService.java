package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.jetbrains.annotations.NotNull;
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

        List<String> messageKeys = new ArrayList<>();

        // Snap timing
        if (isSnapExpeditedEligible) {
            messageKeys.add(getMessage("success.expedited-snap-timing", locale));
        }

        // Ccap timing
        if (isCcapExpeditedEligible) {
            messageKeys.add(getMessage("success.expedited-ccap-timing", locale));
        }

        // Contact Promise
        List<String> nextStepLetterProgramNames = getProgramNames(applicantPrograms, hasNonExpeditedSnap, isCcapExpeditedEligible, locale);
        if (!nextStepLetterProgramNames.isEmpty()) {
            String programNamesForLetter = listToString(nextStepLetterProgramNames, locale);
            messageKeys.add(messageSource.getMessage("success.contact-promise", new String[]{programNamesForLetter}, locale));
        }

        // Interview expectation
        if (!onlyCcap) {
            messageKeys.add(getMessage("success.you-will-need-to-complete-an-interview", locale));
        }

        // Suggested Action
        if (isSnapExpeditedEligible && notCcap) {
            messageKeys.add(getMessage("success.expedited-snap-suggested-action", locale));
        } else {
            messageKeys.add(getMessage("success.standard-suggested-action", locale));
        }

        return String.join("<br><br>", messageKeys);
    }

    private List<String> getProgramNames(List<String> applicantPrograms, boolean hasNonExpeditedSnap, boolean isCcapExpeditedEligible, Locale locale) {
        boolean hasCcap = applicantPrograms.stream().anyMatch(p -> p.equals(Program.CCAP));
        boolean hasNonExpeditedCcap = hasCcap && !isCcapExpeditedEligible;
        boolean hasGrh = applicantPrograms.stream().anyMatch(p -> p.equals(Program.GRH));
        boolean hasCash = applicantPrograms.stream().anyMatch(p -> p.equals(Program.CASH));
        boolean hasEa = applicantPrograms.stream().anyMatch(p -> p.equals(Program.EA));
        List<String> nextStepLetterProgramNames = new ArrayList<>();
        if (hasNonExpeditedCcap) {
            nextStepLetterProgramNames.add(getMessage("success.childcare", locale));
        }
        if (hasGrh) {
            nextStepLetterProgramNames.add(getMessage("success.housing", locale));
        }
        if (hasEa) {
            nextStepLetterProgramNames.add((getMessage("success.emergency-assistance", locale)));
        }
        if (hasCash) {
            nextStepLetterProgramNames.add((getMessage("success.cash-support", locale)));
        }
        if (hasNonExpeditedSnap) {
            nextStepLetterProgramNames.add(getMessage("success.food-support", locale));
        }

        return nextStepLetterProgramNames;
    }

    /**
     * Takes a list of strings and returns an English- or Spanish-language string representation of that list
     * e.g. ["a", "b", "c"] -> "a, b and c"
     *
     * The Oxford comma is omitted for Spanish-language compatibility. Spanish does not use the Oxford comma.
     *
     * If we ever add a language which represents lists differently, this method will need to be updated.
     */
    private String listToString(List<String> list, Locale locale) {
        if (list.isEmpty()) return "";

        int lastIdx = list.size() - 1;
        String lastElement = list.get(lastIdx);
        if (list.size() == 1) return lastElement;

        String firstPart = String.join(", ", list.subList(0, lastIdx));
        String and = getMessage("general.and", locale);

        return String.join(" %s ".formatted(and), firstPart, lastElement);
    }

    @NotNull
    private String getMessage(String messageKey, Locale locale) {
        return messageSource.getMessage(messageKey, null, locale);
    }
}
