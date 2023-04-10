package org.codeforamerica.shiba.application.parsers;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Optional;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;

public class EmailParser {

  public static Optional<String> parse(ApplicationData applicationData) {
    PagesData pagesData = applicationData.getPagesData();
    String laterDocsEmail = pagesData.getPageInputFirstValue("matchInfo", "email");
    if (isNotBlank(laterDocsEmail)) {
      return Optional.of(laterDocsEmail);
    }

    String healthcareRenewalEmail = pagesData.getPageInputFirstValue("healthcareRenewalMatchInfo", "email");
    if (isNotBlank(healthcareRenewalEmail)) {
      return Optional.of(healthcareRenewalEmail);
    }

    String regularFlowEmail = pagesData.getPageInputFirstValue("contactInfo", "email");
    if (isNotBlank(regularFlowEmail)) {
      return Optional.of(regularFlowEmail);
    }

    return Optional.empty();
  }
}
