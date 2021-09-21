package org.codeforamerica.shiba.pages;

import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class NextStepsContentService {

  private final String PHONE_ICON = "fragments/icons/icon-phone :: icon-phone";
  private final String LETTER_ICON = "fragments/icons/icon-letter :: icon-letter";
  private final String SILHOUETTE_ICON = "fragments/icons/icon-communicate :: icon-communicate";
  private final String expeditedSnapText = "Within 24 hours, expect a call from your county about your food assistance application.";
  private final String expeditedCCapText = "Within 5 days, your county will determine your childcare assistance case and send you a letter in the mail.";
  private final String reachOutToCounty1="If you don't hear from your county within 3 days or want an update on your case, please call your county.";
  private final String reachOutToCounty2="Call your county if you don’t hear from them in the time period we’ve noted.";
  private final String nonExpeditedPrograms = "In the next 7-10 days, expect to get a letter in the mail from your county about your %s application. The letter will explain your next steps.";

  private final MessageSource messageSource;

  public NextStepsContentService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public List<NextStepSection> getNextSteps(List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      Locale locale) {
    ArrayList<NextStepSection> nextStepSections = new ArrayList<>();
    boolean snapEligibile = snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE);
    boolean ccapEligible = ccapExpeditedEligibility.equals(CcapExpeditedEligibility.ELIGIBLE);

    if(snapEligibile) {
      nextStepSections.add(new NextStepSection(PHONE_ICON,expeditedSnapText ));
    }

    if(ccapEligible){
      nextStepSections.add(new NextStepSection(LETTER_ICON, expeditedCCapText));
    }

    addTimingForNonExpeditedPrograms(programs, nextStepSections, snapEligibile, ccapEligible);

    if(snapEligibile && !ccapEligible ){
      nextStepSections.add(new NextStepSection(SILHOUETTE_ICON, reachOutToCounty1 ));
    } else {
      nextStepSections.add(new NextStepSection(SILHOUETTE_ICON, reachOutToCounty2 ));
    }

    return nextStepSections;
  }

  private void addTimingForNonExpeditedPrograms(List<String> programs, ArrayList<NextStepSection> nextStepSections,
      boolean snapEligibile, boolean ccapEligible) {
    List<String> programNames = new ArrayList<>();
    programs.forEach( p -> {
      if (p.equals(CCAP) && !ccapEligible) {
        programNames.add("childcare");
      }
      if(p.equals(SNAP) && !snapEligibile) {
        programNames.add("food support");
      }
      if(p.equals(EA)){
        programNames.add("emergency assistance");
      }
      if(p.equals(CASH)){
        programNames.add("cash support");
      }
      if(p.equals(GRH)){
        programNames.add("housing");
      }
    });

    if(programNames.size() > 0) {
      String addAnd ="";
      if(programNames.size() > 1) {
        String lastProgram = programNames.get(programNames.size() -1);
        addAnd = " and " + lastProgram;
        programNames.remove(programNames.size()-1);
      }
      String programText = String.join(", ", programNames);
      programText += addAnd;
      nextStepSections.add(new NextStepSection(LETTER_ICON, nonExpeditedPrograms.formatted(programText)));
    }
  }

  public record NextStepSection(String icon, String message) {

  }
}
