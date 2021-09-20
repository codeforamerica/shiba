package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.internationalization.InternationalizationUtils.listToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class NextStepsContentService {

  private final String PHONE_ICON = "fragments/icons/icon-phone :: icon-phone";
  private final String LETTER_ICON = "fragments/icons/icon-letter :: icon-letter";
  private final String COMMUNICATE_ICON = "fragments/icons/icon-communicate :: icon-communicate";

  private final MessageSource messageSource;

  public NextStepsContentService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public List<SuccessMessage> getNextSteps(List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      Locale locale) {
    boolean isSnapExpeditedEligible = snapExpeditedEligibility == SnapExpeditedEligibility.ELIGIBLE;
    boolean isCcapExpeditedEligible = ccapExpeditedEligibility == CcapExpeditedEligibility.ELIGIBLE;

    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    List<SuccessMessage> messages = new ArrayList<>();

    // Expedited Snap timing
    if (isSnapExpeditedEligible) {
      messages.add(new SuccessMessage(PHONE_ICON,
          lms.getMessage("success.expedited-snap-timing"),
          lms.getMessage("success.expedited-snap-timing-header"))
      );
    }

    // Expedited Ccap timing
    if (isCcapExpeditedEligible) {
      messages.add(new SuccessMessage(LETTER_ICON,
          lms.getMessage("success.expedited-ccap-timing"),
          lms.getMessage("success.expedited-ccap-timing-header"))
      );
    }

    // Contact Promise for all programs they are not expedited for
    List<String> nonExpeditedPrograms =
        getNonExpeditedPrograms(programs, isSnapExpeditedEligible, isCcapExpeditedEligible, lms);
    if (!nonExpeditedPrograms.isEmpty()) {
      String humanReadableProgramList = listToString(nonExpeditedPrograms, lms);
      messages.add(new SuccessMessage(LETTER_ICON,
          lms.getMessage("success.contact-promise", List.of(humanReadableProgramList)),
          lms.getMessage("success.contact-promise-header")));
    }

    // Suggested Action
    String suggestedAction = lms.getMessage("success.standard-suggested-action");
    if (isSnapExpeditedEligible && !programs.contains(CCAP)) {
      suggestedAction = lms.getMessage("success.expedited-snap-suggested-action");
    }
    messages.add(new SuccessMessage(COMMUNICATE_ICON,
        suggestedAction,
        lms.getMessage("success.suggested-action-header")));

    return messages;
  }

  private List<String> getNonExpeditedPrograms(
      List<String> programAcronyms,
      boolean isSnapExpeditedEligible,
      boolean isCcapExpeditedEligible,
      LocaleSpecificMessageSource ms) {
    List<String> nextStepLetterPrograms = new ArrayList<>();

    boolean hasNonExpeditedCcap = programAcronyms.contains(CCAP) && !isCcapExpeditedEligible;
    if (hasNonExpeditedCcap) {
      nextStepLetterPrograms.add(ms.getMessage("success.childcare"));
    }

    if (programAcronyms.contains(GRH)) {
      nextStepLetterPrograms.add(ms.getMessage("success.housing"));
    }

    if (programAcronyms.contains(EA)) {
      nextStepLetterPrograms.add(ms.getMessage("success.emergency-assistance"));
    }

    if (programAcronyms.contains(CASH)) {
      nextStepLetterPrograms.add(ms.getMessage("success.cash-support"));
    }

    boolean hasNonExpeditedSnap = programAcronyms.contains(SNAP) && !isSnapExpeditedEligible;
    if (hasNonExpeditedSnap) {
      nextStepLetterPrograms.add(ms.getMessage("success.food-support"));
    }

    return nextStepLetterPrograms;
  }

  public record SuccessMessage(String icon, String message, String title) {

  }
}
