package org.codeforamerica.shiba.output.caf;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME_CCAP;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;

public class UnearnedIncomeCalculator {

  public static final Map<String, String> UNEARNED_INCOME_FIELD_NAMES = new HashMap<>();

  static {
    UNEARNED_INCOME_FIELD_NAMES.put("SOCIAL_SECURITY", "socialSecurityAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("SSI", "supplementalSecurityIncomeAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("VETERANS_BENEFITS", "veteransBenefitsAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("UNEMPLOYMENT", "unemploymentAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("WORKERS_COMPENSATION", "workersCompensationAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("RETIREMENT", "retirementAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("CHILD_OR_SPOUSAL_SUPPORT", "childOrSpousalSupportAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("TRIBAL_PAYMENTS", "tribalPaymentsAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("BENEFITS", "benefitsAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("INSURANCE_PAYMENTS", "insurancePaymentsAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("CONTRACT_FOR_DEED", "contractForDeedAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("TRUST_MONEY", "trustMoneyAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("HEALTH_CARE_REIMBURSEMENT", "healthCareReimbursementAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("INTEREST_DIVIDENDS", "interestDividendsAmount");
    UNEARNED_INCOME_FIELD_NAMES.put("OTHER_SOURCES", "otherSourcesAmount");
  }

  public BigDecimal unearnedAmount(ApplicationData applicationData) {
    return unearnedAmount(applicationData, "unearnedIncomeSources", UNEARNED_INCOME)
        .add(unearnedAmount(applicationData, "unearnedIncomeSourcesCcap", UNEARNED_INCOME_CCAP));
  }

  private BigDecimal unearnedAmount(ApplicationData applicationData, String pageName, Field field) {
    return getValues(applicationData.getPagesData(), field)
        .stream()
        .map(amount -> getUnearnedAmount(amount, pageName, applicationData.getPagesData()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal getUnearnedAmount(String fieldName, String pageName, PagesData pagesData) {
    String result = pagesData.getPageInputFirstValue(pageName,
        UNEARNED_INCOME_FIELD_NAMES.get(fieldName));
    String formattedResult =
        result == null || result.isBlank() ? "0" : result.replaceAll("[^\\d.]", "");
    return new BigDecimal(formattedResult);
  }
}
