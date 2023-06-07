package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;

public class ContactInfoParser {

  public static boolean optedIntoEmailCommunications(ApplicationData applicationData) {
    return applicationData.getPagesData()
        .safeGetPageInputValue("contactInfo", "phoneOrEmail")
        .contains("EMAIL");
  }
  
  public static boolean optedIntoTEXT(ApplicationData applicationData) {
    return applicationData.getPagesData()
        .safeGetPageInputValue("contactInfo", "phoneOrEmail")
        .contains("TEXT");
  }
  
  public static String phoneNumber(ApplicationData applicationData) {
    return applicationData.getPagesData()
        .getPageInputFirstValue("contactInfo", "phoneNumber");
  }
  
  public static String email(ApplicationData applicationData) {
    return applicationData.getPagesData()
        .getPageInputFirstValue("contactInfo", "email");
  }
  
  public static String firstName(ApplicationData applicationData) {
        return applicationData.getPagesData()
            .getPageInputFirstValue("personalInfo", "firstName");
      }
}
