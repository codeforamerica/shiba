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

  public List<NextStepSection> getNextSteps(List<String> programs,
      SnapExpeditedEligibility snapExpeditedEligibility,
      CcapExpeditedEligibility ccapExpeditedEligibility,
      Locale locale) {
    boolean isSnapExpeditedEligible = snapExpeditedEligibility == SnapExpeditedEligibility.ELIGIBLE;
    boolean isCcapExpeditedEligible = ccapExpeditedEligibility == CcapExpeditedEligibility.ELIGIBLE;

    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    List<NextStepSection> messages = new ArrayList<>();

    // Expedited Snap timing
    if (isSnapExpeditedEligible) {
      messages.add(new NextStepSection(PHONE_ICON,
          lms.getMessage("success.expedited-snap-timing"),
          lms.getMessage("success.expedited-snap-timing-header"))
      );
    }

    // Expedited Ccap timing
    if (isCcapExpeditedEligible) {
      messages.add(new NextStepSection(LETTER_ICON,
          lms.getMessage("success.expedited-ccap-timing"),
          lms.getMessage("success.expedited-ccap-timing-header"))
      );
    }

    // Contact Promise for all programs they are not expedited for
    List<String> nonExpeditedPrograms =
        getNonExpeditedPrograms(programs, isSnapExpeditedEligible, isCcapExpeditedEligible, lms);
    if (!nonExpeditedPrograms.isEmpty()) {
      String humanReadableProgramList = listToString(nonExpeditedPrograms, lms);
      messages.add(new NextStepSection(LETTER_ICON,
          lms.getMessage("success.contact-promise", List.of(humanReadableProgramList)),
          lms.getMessage("success.contact-promise-header")));
    }

    // Suggested Action
    String suggestedAction = lms.getMessage("success.standard-suggested-action");
    if (isSnapExpeditedEligible && !programs.contains(CCAP)) {
      suggestedAction = lms.getMessage("success.expedited-snap-suggested-action");
    }
    messages.add(new NextStepSection(COMMUNICATE_ICON,
        suggestedAction,
        lms.getMessage("success.suggested-action-header")));

    return messages;
  }
  
  public List<NextStepSection> getNextStepsForDocumentUpload(boolean isDocumentUploaded,
	      Locale locale) {
	   
	    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
	    List<NextStepSection> messages = new ArrayList<NextStepSection>();

	    // No document uploaded
	    if (!isDocumentUploaded) {
		  messages.add(new NextStepSection("", lms.getMessage("next-steps.no-document-upload-message-1"), ""));
		  messages.add(new NextStepSection("", lms.getMessage("next-steps.no-document-upload-message-2"), ""));

	    }
	    // Document uploaded
	    if (isDocumentUploaded) {
		      messages.add(new NextStepSection("", lms.getMessage("next-steps.document-upload-message-1"), ""));
		      messages.add(new NextStepSection("", lms.getMessage("next-steps.document-upload-message-2"), ""));
		    }
	    
	    return messages;
	  }
  
	  /**
	   * This method determines the message that is displayed in the open "Allow time
	   * for review accordion" based on the parameters provided.
	   * 
	   * @param programs                 - the programs that have been applied for
	   * @param snapExpeditedEligibility - the object that determine whether or not
	   *                                 the applicant is eligible for expedited SNAP
	   * @param ccapExpeditedEligibility - the object that determine whether or not
	   *                                 the applicant is eligible for expedited CCAP
	   * @param locale                   - language locale
	   * @return - String, the string that is displayed when the "Allow time for
	   *         review accordion" is opened.
	   */
	  public List<NextStepSection> getNextStepsAllowTimeForReview(List<String> programs,
	  	  SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility,
	  	  Locale locale) {

	  	LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
	  	List<NextStepSection> messages = new ArrayList<>();

	  	if (snapExpeditedEligibility!=null && snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE)) { // case #1 has expedited SNAP
	  		messages.add(new NextStepSection(PHONE_ICON,
		  	          lms.getMessage("next-steps.allow-time-for-review-expedited-snap"),
		  	          lms.getMessage("next-steps.allow-time-for-review-header"))
		  	      );
	  	} else if (ccapExpeditedEligibility!=null && ccapExpeditedEligibility.equals(CcapExpeditedEligibility.ELIGIBLE)) { // case #2 has expedited CCAP
	  		messages.add(new NextStepSection(PHONE_ICON,
	  	          lms.getMessage("next-steps.allow-time-for-review-expedited-ccap"),
	  	          lms.getMessage("next-steps.allow-time-for-review-header"))
	  	      );
	  	} else { // case #3 has no expedited programs
	  		messages.add(new NextStepSection("",
		  	          lms.getMessage("next-steps.allow-time-for-review-not-expedited"),
		  	          lms.getMessage("next-steps.allow-time-for-review-header"))
		  	      );
	  	}

	  	return messages;
	  }
	  
	  /**
	   * This method determines the message that is displayed in the open "Complete an interview accordion"
	   *    based on the parameters provided.
	   * 
	   * @param programs                 - the programs that have been applied for
	   * @param snapExpeditedEligibility - the object that determine whether or not
	   *                                 the applicant is eligible for expedited SNAP
	   * @param ccapExpeditedEligibility - the object that determine whether or not
	   *                                 the applicant is eligible for expedited CCAP
	   * @param locale                   - language locale
	   * @return - String, the string that is displayed when the "Complete an interview
	   *         accordion" is opened.
	   */
	  public List<NextStepSection> getNextStepsCompleteAnInterview(List<String> programs,
	  	  SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility,
	  	  Locale locale) {
	
	  	LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
	  	List<NextStepSection> messages = new ArrayList<NextStepSection>();
	  	messages.add(new NextStepSection("", lms.getMessage("next-steps.complete-an-interview-1"), ""));
	  	messages.add(new NextStepSection("", lms.getMessage("next-steps.complete-an-interview-2"), ""));
	
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

  public record NextStepSection(String icon, String message, String title) {

  }
}
