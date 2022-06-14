package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.BENEFITS_PROGRAMS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.CHILD_OR_SPOUSAL_SUPPORT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.CONTRACT_FOR_DEED_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HEALTHCARE_REIMBURSEMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.INSURANCE_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.INTEREST_DIVIDENDS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.OTHER_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RENTAL_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RETIREMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SOCIAL_SECURITY_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SSI_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.TRIBAL_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.TRUST_MONEY_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_BENEFITS_PROGRAMS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_CHILD_OR_SPOUSAL_SUPPORT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_CONTRACT_FOR_DEED_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_HEALTHCARE_REIMBURSEMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INSURANCE_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INTEREST_DIVIDENDS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_OTHER_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_RENTAL_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_RETIREMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_SOCIAL_SECURITY_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_SSI_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_TRIBAL_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_TRUST_MONEY_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_UNEMPLOYMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_VETERANS_BENEFITS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_WORKERS_COMPENSATION_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEMPLOYMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.VETERANS_BENEFITS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WORKERS_COMPENSATION_AMOUNT;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class UnearnedIncomeCalculationPreparer implements DocumentFieldPreparer {
  @SuppressWarnings("serial")
  public static final Map<String, List<Field>> UNEARNED_INCOME_FIELDS = new HashMap<String, List<Field>>(){
  {
    put("socialSecurityAmount", List.of(SOCIAL_SECURITY_AMOUNT, UNEARNED_SOCIAL_SECURITY_AMOUNT));
    put("supplementalSecurityIncomeAmount", List.of(SSI_AMOUNT, UNEARNED_SSI_AMOUNT));
    put("veteransBenefitsAmount",
        List.of(VETERANS_BENEFITS_AMOUNT, UNEARNED_VETERANS_BENEFITS_AMOUNT));
    put("unemploymentAmount", List.of(UNEMPLOYMENT_AMOUNT, UNEARNED_UNEMPLOYMENT_AMOUNT));
    put("workersCompensationAmount",
        List.of(WORKERS_COMPENSATION_AMOUNT, UNEARNED_WORKERS_COMPENSATION_AMOUNT));
    put("retirementAmount", List.of(RETIREMENT_AMOUNT, UNEARNED_RETIREMENT_AMOUNT));
    put("childOrSpousalSupportAmount",
        List.of(CHILD_OR_SPOUSAL_SUPPORT_AMOUNT, UNEARNED_CHILD_OR_SPOUSAL_SUPPORT_AMOUNT));
    put("tribalPaymentsAmount", List.of(TRIBAL_PAYMENTS_AMOUNT, UNEARNED_TRIBAL_PAYMENTS_AMOUNT));
    // Individual Amounts below only used in CCAP and CERTAIN_POPS
    put("benefitsAmount", List.of(BENEFITS_PROGRAMS_AMOUNT, UNEARNED_BENEFITS_PROGRAMS_AMOUNT));
    put("insurancePaymentsAmount",
        List.of(INSURANCE_PAYMENTS_AMOUNT, UNEARNED_INSURANCE_PAYMENTS_AMOUNT));
    put("contractForDeedAmount",
        List.of(CONTRACT_FOR_DEED_AMOUNT, UNEARNED_CONTRACT_FOR_DEED_AMOUNT));
    put("trustMoneyAmount", List.of(TRUST_MONEY_AMOUNT, UNEARNED_TRUST_MONEY_AMOUNT));
    put("healthCareReimbursementAmount",
        List.of(HEALTHCARE_REIMBURSEMENT_AMOUNT, UNEARNED_HEALTHCARE_REIMBURSEMENT_AMOUNT));
    put("interestDividendsAmount",
        List.of(INTEREST_DIVIDENDS_AMOUNT, UNEARNED_INTEREST_DIVIDENDS_AMOUNT));
    put("rentalIncomeAmount", List.of(RENTAL_AMOUNT, UNEARNED_RENTAL_AMOUNT));
    put("otherPaymentsAmount", List.of(OTHER_PAYMENTS_AMOUNT, UNEARNED_OTHER_PAYMENTS_AMOUNT));
}};
 
  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> results = new ArrayList<>();
    for(String inputName : UNEARNED_INCOME_FIELDS.keySet())
    {
      List<DocumentField> docFieldList = getUnearnedIncomeSection(application,inputName, UNEARNED_INCOME_FIELDS.get(inputName));
      results.addAll(docFieldList);
    }
    return results;
  }
  
  @NotNull
  private static List<DocumentField> getUnearnedIncomeSection(Application application, String inputName,
      List<Field> unearnedIncomeField) {
    List<DocumentField> availableUnearnedList = new ArrayList<>();
    var individualAmt =
        getFirstValue(application.getApplicationData().getPagesData(), unearnedIncomeField.get(0));
    var hhAmt =
        getValues(application.getApplicationData().getPagesData(), unearnedIncomeField.get(1));
    BigDecimal totHHAmt = hhAmt.stream().filter(amt -> !amt.isEmpty()).map(BigDecimal::new)
        .reduce(BigDecimal.ZERO,BigDecimal::add);
    BigDecimal totAmt = totHHAmt.add(new BigDecimal(individualAmt==null?"0":individualAmt));
    availableUnearnedList.add( new DocumentField("unearnedIncomeSource", inputName,
        totAmt.toString().equals("0")?"":totAmt.toString() ,
        DocumentFieldType.SINGLE_VALUE));
    availableUnearnedList.add( new DocumentField("unearnedIncomeSource", inputName.replace("Amount", "Frequency"),
        totAmt.toString().equals("0")?"":"Monthly" ,
        DocumentFieldType.SINGLE_VALUE));
    return availableUnearnedList;

  }
}
