package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLICANT_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.LAST_THIRTY_DAYS_JOB_INCOME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MIGRANT_WORKER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.PREPARING_MEALS_TOGETHER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UTILITY_EXPENSES_SELECTIONS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.HOUSEHOLD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group.JOBS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.ELIGIBLE;
import static org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility.NOT_ELIGIBLE;

import java.util.List;
import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class SnapExpeditedEligibilityDecider {

  public static final Money ASSET_THRESHOLD = Money.parse("100");
  public static final Money INCOME_THRESHOLD = Money.parse("150");
  private final UtilityDeductionCalculator utilityDeductionCalculator;
  private final TotalIncomeCalculator totalIncomeCalculator;
  private final GrossMonthlyIncomeParser grossMonthlyIncomeParser;


  public SnapExpeditedEligibilityDecider(UtilityDeductionCalculator utilityDeductionCalculator,
      TotalIncomeCalculator totalIncomeCalculator,
      GrossMonthlyIncomeParser snapExpeditedEligibilityParser) {
    this.utilityDeductionCalculator = utilityDeductionCalculator;
    this.totalIncomeCalculator = totalIncomeCalculator;
    this.grossMonthlyIncomeParser = snapExpeditedEligibilityParser;
  }

  /**
   * Users qualify for expedited service if they meet one of the following criteria:
   * <ul>
   *   <li>Households with less than $150 in monthly gross income and $100 or less in liquid assets.</li>
   *   <li>Destitute migrant or seasonal farm worker units who have $100 or less in liquid assets.</li>
   *   <li>Households whose combined monthly gross income and liquid assets are less than their monthly housing cost(s) and the applicable standard utility deduction if the unit is entitled to it.</li>
   * </ul>
   * If it is unclear if someone is expedited (they have not given the needed info) return “Undetermined”
   *
   * @param applicationData Applicant data to check
   * @return SNAP eligibility given the applicant data
   */
  public SnapExpeditedEligibility decide(ApplicationData applicationData) {
    if (applicationData.getPagesData().safeGetPageInputValue("expeditedSnap", "expeditedSnap").equals(List.of("true"))) {
      return ELIGIBLE;
    } else {
      return NOT_ELIGIBLE;
    }
  }

  private boolean isApplyingForSnap(ApplicationData applicationData) {
    boolean applicantApplyingForSnap = getValues(applicationData.getPagesData(), APPLICANT_PROGRAMS)
        .contains("SNAP");
    List<String> householdPrograms = getValues(HOUSEHOLD, HOUSEHOLD_PROGRAMS, applicationData);
    boolean householdMemberApplyingForSnap =
        householdPrograms != null && householdPrograms.contains("SNAP");
    boolean isPreparingMealsTogether = Boolean
        .parseBoolean(getFirstValue(applicationData.getPagesData(), PREPARING_MEALS_TOGETHER));

    return applicantApplyingForSnap || (householdMemberApplyingForSnap && isPreparingMealsTogether);
  }

  private boolean canDetermineEligibility(ApplicationData applicationData) {
    // required information
    String isMigrantWorker = getFirstValue(applicationData.getPagesData(), MIGRANT_WORKER);
    boolean missingUtilities =
        getFirstValue(applicationData.getPagesData(), UTILITY_EXPENSES_SELECTIONS) == null;
    if (isMigrantWorker == null || missingUtilities) {
      return false;
    }

    List<String> thirtyDayEstimates = getValues(JOBS, LAST_THIRTY_DAYS_JOB_INCOME, applicationData);
    return thirtyDayEstimates == null || !thirtyDayEstimates.stream().allMatch(String::isBlank);
  }


  private Money parseMoney(PagesData pagesData, Field field) {
    return Money.parse(getFirstValue(pagesData, field), field.getDefaultValue());
  }
}
