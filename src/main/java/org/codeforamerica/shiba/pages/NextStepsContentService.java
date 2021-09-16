package org.codeforamerica.shiba.pages;

import static java.util.Collections.emptyList;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.internationalization.InternationalizationUtils.listToString;

import java.util.ArrayList;
import java.util.Collections;
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
    return Collections.emptyList();
  }

  public record SuccessMessage(String icon, String message) {}
}
