package org.codeforamerica.shiba.output.caf;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeMap;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

@Component
public class UnearnedIncomeCcapCalculator {
 
  public BigDecimal unearnedAmount(ApplicationData applicationData) {
    List<String> unearnedIncomeList = applicationData.getPagesData().safeGetPageInputValue("unearnedIncomeCcap", "unearnedIncomeCcap");
    List<BigDecimal> unearnedAmountList = unearnedIncomeList.stream()
        .map(amount -> {
          return getUnearnedAmount(amount, applicationData);
        }).toList();
        
    BigDecimal totalUnearnedAmount = unearnedAmountList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return totalUnearnedAmount;
  }
  
  private BigDecimal getUnearnedAmount(String fieldName, ApplicationData applicationData) {
    String result = applicationData.getPagesData().getPageInputFirstValue("unearnedIncomeSourcesCcap", getFieldName().get(fieldName));
    return new BigDecimal((null == result || result.isBlank()) ?"0":result);
  }
 
 private TreeMap<String, String> getFieldName() {
   TreeMap<String, String> tmap = new TreeMap<String, String>();
   tmap.put("BENEFITS", "benefitsAmount");
   tmap.put("INSURANCE_PAYMENTS", "insurancePaymentsAmount");
   tmap.put("CONTRACT_FOR_DEED", "contractForDeedAmount");
   tmap.put("TRUST_MONEY", "trustMoneyAmount");
   tmap.put("HEALTH_CARE_REIMBURSEMENT", "healthCareReimbursementAmount");
   tmap.put("INTEREST_DIVIDENDS", "interestDividendsAmount");
   tmap.put("OTHER_SOURCES", "otherSourcesAmount");
   return tmap;
 }
}
