package org.codeforamerica.shiba.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class NextStepsContentService {

  private final String PHONE_ICON = "fragments/icons/icon-phone :: icon-phone";
  private final String LETTER_ICON = "fragments/icons/icon-letter :: icon-letter";
  private final String SILHOUETTE_ICON = "fragments/icons/icon-communicate :: icon-communicate";

  private final String CALL_FOR_FOOD = "Within 24 hours, expect a call from your county about your food assistance application.";

  private final String CCAP_EXPEDITED_5_DAYS = "Within 5 days, your county will determine your childcare assistance case and send you a letter in the mail.";
  private final String CALL_YOUR_COUNTY_3_DAYS = "If you don't hear from your county within 3 days or want an update on your case, please call your county.";
  private final String CALL_YOUR_COUNTY_END_MESSAGE = "Call your county if you don’t hear from them in the time period we’ve noted.";

  private final String THIRD_SECTION_PART_1 = "In the next 7-10 days, expect to get a letter in the mail from your county about your ";
  private final String THIRD_SECTION_PART_2 = " application. The letter will explain your next steps.";

  private final Map<String, String> HUMAN_READABLE_PROGRAM_NAMES = Map.of(
      "SNAP", "food support",
      "CCAP", "childcare",
      "EA", "emergency assistance",
      "CASH", "cash support",
      "GRH", "housing"
  );

  private final MessageSource messageSource;

  public NextStepsContentService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public List<NextStepSection> getNextSteps(
      List<String> programs, // ["CCAP", "SNAP"]
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      Locale locale) {

    List<NextStepSection> output = new ArrayList<>();

    // First Section
    if (snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE)) {
      output.add(new NextStepSection(PHONE_ICON, CALL_FOR_FOOD));
    }

    // Second Section
    if (ccapExpeditedEligibility.equals(CcapExpeditedEligibility.ELIGIBLE)) {
      output.add(new NextStepSection(LETTER_ICON, CCAP_EXPEDITED_5_DAYS));
    }

    // Third Section
    if (programs.contains("GRH") || programs.contains("EA") || programs.contains("CASH") ||
        (snapExpeditedEligibility.equals(SnapExpeditedEligibility.NOT_ELIGIBLE) && programs.contains("SNAP")) ||
        (ccapExpeditedEligibility.equals(CcapExpeditedEligibility.NOT_ELIGIBLE) && programs.contains("CCAP"))
    ) {
      List<String> programNames = new ArrayList<>();
      if (programs.contains("EA")) {
        programNames.add(HUMAN_READABLE_PROGRAM_NAMES.get("EA"));
      }
      if (programs.contains("CASH")) {
        programNames.add(HUMAN_READABLE_PROGRAM_NAMES.get("CASH"));
      }
      if (programs.contains("SNAP") && snapExpeditedEligibility.equals(SnapExpeditedEligibility.NOT_ELIGIBLE)) {
        programNames.add(HUMAN_READABLE_PROGRAM_NAMES.get("SNAP"));
      }
      if (programs.contains("GRH")) {
        programNames.add(HUMAN_READABLE_PROGRAM_NAMES.get("GRH"));
      }
      if (programs.contains("CCAP") && ccapExpeditedEligibility.equals(CcapExpeditedEligibility.NOT_ELIGIBLE)) {
        programNames.add(HUMAN_READABLE_PROGRAM_NAMES.get("CCAP"));
      }

      String commaSeparated = String.join(", ", programNames);

      output.add(new NextStepSection(LETTER_ICON, THIRD_SECTION_PART_1 + commaSeparated + THIRD_SECTION_PART_2));
    }

    // Fourth section here
    if (snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE) && ccapExpeditedEligibility.equals(CcapExpeditedEligibility.NOT_ELIGIBLE)) {
      output.add(new NextStepSection(SILHOUETTE_ICON, CALL_YOUR_COUNTY_3_DAYS));
    } else {
      output.add(new NextStepSection(SILHOUETTE_ICON, CALL_YOUR_COUNTY_END_MESSAGE));
    }

    return output;
  }

  public record NextStepSection(String icon, String message) {

  }

//  public static class NextStepSectionOld {
//    String icon;
//    String message;
//
//    public NextStepSectionOld(String icon, String message) {
//      this.icon = icon;
//      this.message = message;
//    }
//
//    public String getIcon() {
//      return icon;
//    }
//
//    public String getMessage() {
//      return message;
//    }
//  }
}
