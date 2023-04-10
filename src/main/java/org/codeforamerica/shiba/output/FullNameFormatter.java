package org.codeforamerica.shiba.output;

import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.FlowType.HEALTHCARE_RENEWAL;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.jetbrains.annotations.NotNull;

public class FullNameFormatter {

  public static String format(String unformattedName) {
    int lastIndex = unformattedName.trim().lastIndexOf(' ');
    return lastIndex == -1 ? "" : unformattedName.trim().substring(0, lastIndex);
  }

  public static String getId(String unformattedName) {
    int lastIndex = unformattedName.trim().lastIndexOf(' ');
    return unformattedName.trim().substring(lastIndex + 1);
  }

  @NotNull
  public static List<String> getListOfSelectedFullNames(Application application, String pageName,
      String inputName) {
    return getListOfSelectedNameStrings(application, pageName, inputName)
        .stream().map(FullNameFormatter::format).collect(Collectors.toList());
  }

  @NotNull
  public static List<String> getListOfSelectedFullNamesExceptFor(Application application,
      String pageName, String inputName, String exceptPageName, String exceptInputName) {
    List<String> exceptNameStrings = getListOfSelectedNameStrings(application, exceptPageName,
        exceptInputName);
    return getListOfSelectedNameStrings(application, pageName, inputName)
        .stream().filter(nameString -> !exceptNameStrings.contains(nameString))
        .map(FullNameFormatter::format).collect(Collectors.toList());
  }

  public static List<String> getListOfSelectedNameStrings(Application application, String pageName,
      String inputName) {
    return Optional.ofNullable(application.getApplicationData().getPageData(pageName))
        .map(pageData -> pageData.get(inputName))
        .map(InputData::getValue)
        .orElse(List.of(""));
  }


  public static String getFullName(Application application) {
    var pageName = (application.getFlow() == LATER_DOCS) ? "matchInfo" : "personalInfo";
    
    if (application.getFlow() == HEALTHCARE_RENEWAL) {
      pageName = "healthcareRenewalMatchInfo";
    }
    return ofNullable(application.getApplicationData().getPagesData().getPage(pageName))
        .map(FullNameFormatter::getFullNameString)
        .orElse(null);
  }

  @NotNull
  private static String getFullNameString(PageData pageData) {
    var firstName = getValueOrEmptyString(pageData, "firstName");
    var lastName = getValueOrEmptyString(pageData, "lastName");
    return firstName + " " + lastName;
  }

  @NotNull
  private static String getValueOrEmptyString(PageData pageData, String firstName) {
    return ofNullable(pageData.get(firstName))
        .map(InputData::getValue)
        .map(val -> String.join("", val))
        .orElse("");
  }

}
