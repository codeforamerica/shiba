package org.codeforamerica.shiba.pages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.shiba.pages.data.DatasourcePages;

public class PageUtils {

  private PageUtils() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  public static String getFormInputName(String name) {
    return name;
  }

  public static String getTitleString(List<String> strings) {
    if (strings.size() == 1) {
      return strings.iterator().next();
    } else {
      Iterator<String> iterator = strings.iterator();
      StringBuilder stringBuilder = new StringBuilder(iterator.next());
      while (iterator.hasNext()) {
        String string = iterator.next();
        if (iterator.hasNext()) {
          stringBuilder.append(", ");
        } else {
          stringBuilder.append(" and ");
        }
        stringBuilder.append(string);
      }
      return stringBuilder.toString();
    }
  }

  public static List<String> householdMemberSort(Collection<String> householdMembers) {
    Stream<String> applicant = householdMembers.stream()
        .filter(householdMember -> householdMember.endsWith("applicant"));
    Stream<String> nonApplicantHouseholdMembers = householdMembers.stream()
        .filter(householdMember -> !householdMember.endsWith("applicant")).sorted();

    return Stream.concat(applicant, nonApplicantHouseholdMembers).collect(Collectors.toList());
  }

  public static Boolean isCCAPEligible(DatasourcePages datasourcePages) {
    List<String> applicantPrograms = datasourcePages.get("choosePrograms").get("programs")
        .getValue();
    boolean applicantHasCCAP = applicantPrograms.contains("CCAP");
    boolean hasHousehold = !datasourcePages.get("householdMemberInfo").isEmpty();
    boolean householdHasCCAP = false;
    if (hasHousehold) {
      householdHasCCAP = datasourcePages.get("householdMemberInfo").get("programs").getValue()
          .stream().anyMatch(iteration ->
              iteration.contains("CCAP"));
    }
    return applicantHasCCAP || householdHasCCAP;
  }

  /**
   * @param householdMemberNameAndId a string in the form "firstname lastname id".
   * @param me                       the string "me" in whatever language the client is using
   * @return the full name without an id, or "Me" if the id is the string "applicant"
   */
  public static String householdMemberName(String householdMemberNameAndId, String me) {
    String[] householdMemberInfo = householdMemberNameAndId.split(" ");
    String childId = householdMemberInfo[householdMemberInfo.length - 1];
    if ("applicant".equals(childId)) {
      return me;
    }

    String[] fullNameParts = Arrays
        .copyOfRange(householdMemberInfo, 0, householdMemberInfo.length - 1);
    return StringUtils.join(fullNameParts, " ");
  }
}
