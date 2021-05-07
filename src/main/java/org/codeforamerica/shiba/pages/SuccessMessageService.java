package org.codeforamerica.shiba.pages;

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
    // TODO where should these constants live
    private static final String PROGRAM_CCAP = "CCAP";
    private static final String PROGRAM_SNAP = "SNAP";
    private static final String PROGRAM_GRH = "GRH";
    private static final String PROGRAM_CASH = "CASH";
    private static final String PROGRAM_EA = "EA";
    private final MessageSource messageSource;

    public SuccessMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getSuccessMessage(List<String> applicantPrograms, SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility, Locale locale) {
        boolean hasCcap = applicantPrograms.stream().anyMatch(p -> p.equals(PROGRAM_CCAP));
        boolean hasSnap = applicantPrograms.stream().anyMatch(p -> p.equals(PROGRAM_SNAP));
        boolean hasGrh = applicantPrograms.stream().anyMatch(p -> p.equals(PROGRAM_GRH));
        boolean hasCash = applicantPrograms.stream().anyMatch(p -> p.equals(PROGRAM_CASH));
        boolean hasEa = applicantPrograms.stream().anyMatch(p -> p.equals(PROGRAM_EA));
        boolean onlyCcap = applicantPrograms.stream().allMatch(p -> p.equals(PROGRAM_CCAP));
//        boolean isSnapAndCcapOnly = applicantPrograms.stream().allMatch(p -> p.equals(PROGRAM_CCAP) || p.equals(PROGRAM_SNAP)) && !onlyCcap && !onlySnap;
        boolean isSnapExpeditedEligible = snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE);
        boolean isCcapExpeditedEligible = ccapExpeditedEligibility.equals(CcapExpeditedEligibility.ELIGIBLE);
        boolean notCcap = applicantPrograms.stream().noneMatch(p -> p.equals(PROGRAM_CCAP));
        boolean hasNonExpeditedCcap = hasCcap && !isCcapExpeditedEligible;
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
        List<String> nextStepLetterProgramNames = new ArrayList<>();
        if (hasNonExpeditedCcap) { //If they have housing, cash support, or emergency assistance, or non-expedited snap, or non-expedited ccap
            nextStepLetterProgramNames.add(getMessage("success.childcare", locale));
        }
        if(hasGrh) {
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
        if (nextStepLetterProgramNames.size() > 0) {
            String programNamesForLetter = String.join("/", nextStepLetterProgramNames);
            String[] arg = new String[]{programNamesForLetter};
            messageKeys.add(messageSource.getMessage("success.contact-promise", arg, locale));
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

    @NotNull
    private String getMessage(String messageKey, Locale locale) {
        return messageSource.getMessage(messageKey, null, locale);
    }
}
