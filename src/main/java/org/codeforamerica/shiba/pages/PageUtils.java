package org.codeforamerica.shiba.pages;

import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.codeforamerica.shiba.pages.data.DatasourcePages;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PageUtils {
    private PageUtils() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    private static final String WEB_INPUT_ARRAY_TOKEN = "[]";

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
        Stream<String> applicant = householdMembers.stream().filter(householdMember -> householdMember.endsWith("applicant"));
        Stream<String> nonApplicantHouseholdMembers = householdMembers.stream().filter(householdMember -> !householdMember.endsWith("applicant")).sorted();

        return Stream.concat(applicant, nonApplicantHouseholdMembers).collect(Collectors.toList());
    }

    public static Boolean isCCAPEligible(DatasourcePages datasourcePages) {
        List<String> applicantPrograms = datasourcePages.get("choosePrograms").get("programs").getValue();
        boolean applicantHasCCAP = applicantPrograms.contains("CCAP");
        boolean hasHousehold = !datasourcePages.get("householdMemberInfo").isEmpty();
        boolean householdHasCCAP = false;
        if (hasHousehold) {
            householdHasCCAP = datasourcePages.get("householdMemberInfo").get("programs").getValue().stream().anyMatch(iteration ->
                    iteration.contains("CCAP"));
        }
        return applicantHasCCAP || householdHasCCAP;
    }

    public static String householdMemberName(String householdMemberNameAndId) {
        String[] householdMemberInfo = householdMemberNameAndId.split(" ");
        String[] houseName = Arrays.copyOfRange(householdMemberInfo, 0, householdMemberInfo.length - 1);
        return StringUtils.join(houseName," ");
    }
}
