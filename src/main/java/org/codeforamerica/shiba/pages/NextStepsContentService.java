package org.codeforamerica.shiba.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.stereotype.Service;

@Service
public class NextStepsContentService {

  private final String PHONE_ICON = "fragments/icons/icon-phone :: icon-phone";
  private final String LETTER_ICON = "fragments/icons/icon-letter :: icon-letter";
  private final String SILHOUETTE_ICON = "fragments/icons/icon-communicate :: icon-communicate";

  private Map<String, String> humanReadableProgramNames = Map.of(
      "SNAP", "food support",
      "CCAP", "childcare",
      "EA", "emergency assistance",
      "CASH", "cash support",
      "GRH", "housing"
  );

  public List<NextStepSection> getNextSteps(
      List<String> programs, // ["SNAP", "CCAP"]
      SnapExpeditedEligibility snapExpeditedEligibility, // ELIGIBLE
      CcapExpeditedEligibility ccapExpeditedEligibility) { // NOT ELIGIBLE

    List<NextStepSection> nextStepSections = new ArrayList<>();

    // First section
    if (isEligibleForExpeditedSnap(programs, snapExpeditedEligibility)) {
      nextStepSections.add(new NextStepSection(PHONE_ICON,
          "Within 24 hours, expect a call from your county about your food assistance application."));
    }

    // Second Section
    if (isEligibleForExpeditedCcap(programs, ccapExpeditedEligibility)) {
      nextStepSections.add(new NextStepSection(LETTER_ICON,
          "Within 5 days, your county will determine your childcare assistance case and send you a letter in the mail."));
    }

    // Third section
    if (programs.contains("GRH") || programs.contains("EA") || programs.contains("CASH") ||
        (programs.contains("SNAP") && !isEligibleForExpeditedSnap(programs, snapExpeditedEligibility)) ||
        (programs.contains("CCAP") && !isEligibleForExpeditedCcap(programs, ccapExpeditedEligibility))
    ) {
      List<String> humanReadableProgramNames = programs.stream()
          .map(program -> this.humanReadableProgramNames.get(program))
          .collect(Collectors.toCollection(ArrayList::new));

      if (isEligibleForExpeditedSnap(programs, snapExpeditedEligibility)) {
        humanReadableProgramNames.remove("food support");
      }

      if (isEligibleForExpeditedCcap(programs, ccapExpeditedEligibility)) {
        humanReadableProgramNames.remove("childcare");
      }

      String humanReadableProgramList;
      if (humanReadableProgramNames.size() > 1) {
        List<String> everyElementExceptTheLastOne = humanReadableProgramNames.subList(0, humanReadableProgramNames.size() - 1);
        String firstPart = String.join(", ", everyElementExceptTheLastOne);
        humanReadableProgramList =  firstPart + " and " + humanReadableProgramNames.get(humanReadableProgramNames.size() - 1);
      } else {
        humanReadableProgramList = humanReadableProgramNames.get(humanReadableProgramNames.size() - 1);
      }

      String msg = "In the next 7-10 days, expect to get a letter in the mail from your county about your %s application. The letter will explain your next steps.".formatted(humanReadableProgramList);
      nextStepSections.add(new NextStepSection(LETTER_ICON, msg));
    }

    // Fourth section
    if (isEligibleForExpeditedSnap(programs, snapExpeditedEligibility)
        && !isEligibleForExpeditedCcap(programs, ccapExpeditedEligibility)) {
      nextStepSections.add(new NextStepSection(SILHOUETTE_ICON,
          "If you don't hear from your county within 3 days or want an update on your case, please call your county."));
    } else {
      nextStepSections.add(new NextStepSection(SILHOUETTE_ICON,
          "Call your county if you don’t hear from them in the time period we’ve noted."));
    }

    return nextStepSections;
  }

  private boolean isEligibleForExpeditedCcap(List<String> programs, CcapExpeditedEligibility ccapExpeditedEligibility) {
    return programs.contains("CCAP") && CcapExpeditedEligibility.ELIGIBLE.equals(
        ccapExpeditedEligibility);
  }

  private boolean isEligibleForExpeditedSnap(List<String> programs, SnapExpeditedEligibility snapExpeditedEligibility) {
    return programs.contains("SNAP") &&
        SnapExpeditedEligibility.ELIGIBLE.equals(snapExpeditedEligibility);
  }

  // a pair of an icon identifier and a string that is the actual message
  public record NextStepSection(String icon, String message) {

  }
}
