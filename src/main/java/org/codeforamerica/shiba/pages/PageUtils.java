package org.codeforamerica.shiba.pages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.shiba.pages.data.DatasourcePages;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

public class PageUtils {

  private static final String WEB_INPUT_ARRAY_TOKEN = "[]";

  private PageUtils() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  public static String getFormInputName(String name) {
    return name + WEB_INPUT_ARRAY_TOKEN;
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

  public static Boolean isProgramEligible(DatasourcePages datasourcePages, String program) {
    List<String> applicantPrograms = datasourcePages.get("choosePrograms").get("programs")
        .getValue();
    boolean applicantHasProgram = applicantPrograms.contains(program);
    boolean hasHousehold = !datasourcePages.get("householdMemberInfo").isEmpty();
    boolean householdHasProgram = false;
    if (hasHousehold) {
      householdHasProgram = datasourcePages.get("householdMemberInfo").get("programs").getValue()
          .stream().anyMatch(iteration ->
              iteration.contains(program));
    }
    return applicantHasProgram || householdHasProgram;
  }

  /**
   * @param householdMemberNameAndId a string in the form "firstname lastname id".
   * @param translatedYou                       the string "you" in whatever language the client is using
   * @return the full name without an id, or "you" if the id is the string "applicant"
   */
  public static String householdMemberName(String householdMemberNameAndId, String translatedYou) {
    String[] householdMemberInfo = householdMemberNameAndId.split(" ");
    String childId = householdMemberInfo[householdMemberInfo.length - 1];
    String[] fullNameParts = Arrays
    		.copyOfRange(householdMemberInfo, 0, householdMemberInfo.length - 1);
    
    if ("applicant".equals(childId)) {
      return StringUtils.join(fullNameParts, " ") + " " + translatedYou;
    }

    return StringUtils.join(fullNameParts, " ");
  }
  
	/**
	 * Tests if String name is in a list of names, of which each name contains the name plus id.
	 * This method is different than Arraylist.contains() which simply matches each string.
	 * @param listOfNames
	 * @param name
	 * @return
	 */
	public static boolean listOfNamesContainsName(Collection<String> listOfNames, String name) {
		return listOfNames.stream().filter(k -> k.contains(name)).collect(Collectors.toList()).size() > 0;
	}
	
	public static int findNumberOfHouseholdMembers(Subworkflow datasourcePages) {
		return datasourcePages.size();
	}
  
}
