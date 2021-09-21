package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;

public class ContactInfoParser {

  public static boolean optedIntoEmailCommunications(ApplicationData applicationData) {
    return applicationData.getPagesData()
        .safeGetPageInputValue("contactInfo", "phoneOrEmail")
        .contains("EMAIL");
  }
}
