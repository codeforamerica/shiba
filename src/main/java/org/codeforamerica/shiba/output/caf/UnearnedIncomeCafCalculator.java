package org.codeforamerica.shiba.output.caf;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeMap;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class UnearnedIncomeCafCalculator {


  public BigDecimal unearnedAmount(ApplicationData applicationData) {
    List<String> unearnedIncomeList = applicationData.getPagesData()
        .safeGetPageInputValue("unearnedIncome", "unearnedIncome");
    List<BigDecimal> unearnedAmountList = unearnedIncomeList.stream()
        .map(amount -> getUnearnedAmount(amount, applicationData)).toList();

    return unearnedAmountList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal getUnearnedAmount(String fieldName, ApplicationData applicationData) {
    String result = applicationData.getPagesData()
        .getPageInputFirstValue("unearnedIncomeSources", getFieldName().get(fieldName));
    String formattedResult =
        result == null || result.isBlank() ? "0" : result.replaceAll("[^\\d.]", "");
    return new BigDecimal(formattedResult);
  }

  private TreeMap<String, String> getFieldName() {
    TreeMap<String, String> tmap = new TreeMap<String, String>();
    tmap.put("SOCIAL_SECURITY", "socialSecurityAmount");
    tmap.put("SSI", "supplementalSecurityIncomeAmount");
    tmap.put("VETERANS_BENEFITS", "veteransBenefitsAmount");
    tmap.put("UNEMPLOYMENT", "unemploymentAmount");
    tmap.put("WORKERS_COMPENSATION", "workersCompensationAmount");
    tmap.put("RETIREMENT", "retirementAmount");
    tmap.put("CHILD_OR_SPOUSAL_SUPPORT", "childOrSpousalSupportAmount");
    tmap.put("TRIBAL_PAYMENTS", "tribalPaymentsAmount");
    return tmap;
  }

}
