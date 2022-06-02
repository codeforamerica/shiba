package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_DOB;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ASSETS_TYPE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.HOUSEHOLD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.ELIGIBLE;
import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.NOT_ELIGIBLE;
import static org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility.UNDETERMINED;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.enrichment.DateOfBirthEnrichment;
import org.codeforamerica.shiba.pages.enrichment.HouseholdMemberDateOfBirthEnrichment;
import org.springframework.stereotype.Component;

@Component
public class CcapExpeditedEligibilityDecider {

  private final DateOfBirthEnrichment dateOfBirthEnrichment = new HouseholdMemberDateOfBirthEnrichment();

  private static final Set<String> EXPEDITED_LIVING_SITUATIONS
      = Set.of("HOTEL_OR_MOTEL", "TEMPORARILY_WITH_FRIENDS_OR_FAMILY_DUE_TO_ECONOMIC_HARDSHIP",
      "EMERGENCY_SHELTER", "LIVING_IN_A_PLACE_NOT_MEANT_FOR_HOUSING");
  

  public CcapExpeditedEligibility decide(ApplicationData applicationData) {
    boolean hasMillionDollarAsset = getMillionDollarAsset(applicationData);
    String livingSituation = getLivingSituation(applicationData);
    if (null == livingSituation || !applicationData.isCCAPApplication()
        || !applicationData.getSubworkflows().containsKey("household")
        || hasNotEnteredHouseholdMemberBirthDates(applicationData)) {
      return UNDETERMINED;
    }

    if (EXPEDITED_LIVING_SITUATIONS.contains(livingSituation) 
        && hasHouseholdMemberUnder12(applicationData) 
        && !hasMillionDollarAsset) {
      return ELIGIBLE;
    } else {
      return NOT_ELIGIBLE;
    }
  }

  private boolean hasNotEnteredHouseholdMemberBirthDates(ApplicationData applicationData) {
    return getValues(applicationData, HOUSEHOLD,
        HOUSEHOLD_INFO_DOB) == (null) || getValues(applicationData, HOUSEHOLD,
        HOUSEHOLD_INFO_DOB).isEmpty();
  }

  private boolean hasHouseholdMemberUnder12(ApplicationData applicationData) {
    List<PagesData> householdMemberIterations = applicationData.getSubworkflows().get("household")
        .stream().map(Iteration::getPagesData).toList();
    List<PageData> householdMemberIterationEnrichedDobPagesDatas = householdMemberIterations.stream()
        .map(dateOfBirthEnrichment::process).toList();
    List<String> householdMemberBirthDatesAsStrings = householdMemberIterationEnrichedDobPagesDatas
        .stream().map(pagesData -> pagesData.get("dobAsDate").getValue().get(0)).toList();
    List<LocalDate> householdMemberBirthDatesAsLocalDates =
        getHouseHoldMemberBirthdatesAsDates(householdMemberBirthDatesAsStrings);
    return householdMemberBirthDatesAsLocalDates.stream()
        .anyMatch(date -> Period.between(date, LocalDate.now()).getYears() <= 12);
  }

  private String getLivingSituation(ApplicationData applicationData) {
    return applicationData.getPagesData()
        .getPageInputFirstValue("livingSituation", "livingSituation");
  }
  
  private boolean getMillionDollarAsset(ApplicationData applicationData) {
    return getValues(applicationData.getPagesData(),ASSETS_TYPE).contains("ONE_MILLION_ASSETS");
  }

  private List<LocalDate> getHouseHoldMemberBirthdatesAsDates(List<String> birthDatesAsStrings) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    return birthDatesAsStrings.stream().map(stringDob -> {
      return LocalDate.parse(stringDob, formatter);
    }).collect(Collectors.toList());
  }
}
