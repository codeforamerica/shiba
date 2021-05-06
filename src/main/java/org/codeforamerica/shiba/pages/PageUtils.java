package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.DatasourcePages;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class PageUtils {
    private static final String WEB_INPUT_ARRAY_TOKEN = "[]";
    private static final String PROGRAM_CCAP = "CCAP";
    private static final String PROGRAM_SNAP = "SNAP";

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

    @SuppressWarnings("ConstantConditions")
    public static String programSpecificSuccessMessage(DatasourcePages datasourcePages, SnapExpeditedEligibility snapExpeditedEligibility, CcapExpeditedEligibility ccapExpeditedEligibility) {
        List<String> applicantPrograms = datasourcePages.get("choosePrograms").get("programs").getValue();
        boolean onlyCcap = applicantPrograms.stream().allMatch(p -> p.equals(PROGRAM_CCAP));
        boolean onlySnap = applicantPrograms.stream().allMatch(p -> p.equals(PROGRAM_SNAP));
        boolean isSnapAndCcapOnly = applicantPrograms.stream().allMatch(p -> p.equals(PROGRAM_CCAP) || p.equals(PROGRAM_SNAP)) && !onlyCcap && !onlySnap;
        boolean isSnapExpeditedEligible = snapExpeditedEligibility.equals(SnapExpeditedEligibility.ELIGIBLE);
        boolean isCcapExpeditedEligible = ccapExpeditedEligibility.equals(CcapExpeditedEligibility.ELIGIBLE);
        if (onlySnap && isSnapExpeditedEligible) {
            return "success.expedited-snap-only";
        } else if (onlySnap && !isSnapExpeditedEligible) {
            return "success.non-expedited-snap-only";
        } else if (isSnapAndCcapOnly && isSnapExpeditedEligible && isCcapExpeditedEligible) { // todo is it isSnapAndCcapOnly? or can there be other programs
            return "success.expedited-snap-expedited-ccap";
        } else if (isSnapAndCcapOnly && isSnapExpeditedEligible && !isCcapExpeditedEligible) {
            return "success.expedited-snap-nonexpedited-ccap";
        } else if (isSnapAndCcapOnly && !isSnapExpeditedEligible && isCcapExpeditedEligible) {
            return "success.expedited-ccap-nonexpedited-snap";
        } else if(onlyCcap && isCcapExpeditedEligible && !isSnapExpeditedEligible) {
            return "success.expedited-ccap-only";
        }
        return "success.your-county-will-contact-within-one-week";
    }
}
