package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HAS_HOUSE_HOLD;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SOCIAL_SECURITY_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SSI_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_SOCIAL_SECURITY_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_SSI_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_VETERANS_BENEFITS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.VETERANS_BENEFITS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEMPLOYMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_UNEMPLOYMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WORKERS_COMPENSATION_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_WORKERS_COMPENSATION_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RETIREMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_RETIREMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.CHILD_OR_SPOUSAL_SUPPORT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_CHILD_OR_SPOUSAL_SUPPORT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.TRIBAL_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_TRIBAL_PAYMENTS_AMOUNT;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.BENEFITS_PROGRAMS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_BENEFITS_PROGRAMS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.INSURANCE_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INSURANCE_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.CONTRACT_FOR_DEED_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_CONTRACT_FOR_DEED_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.TRUST_MONEY_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_TRUST_MONEY_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HEALTHCARE_REIMBURSEMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_HEALTHCARE_REIMBURSEMENT_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.INTEREST_DIVIDENDS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INTEREST_DIVIDENDS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_RENTAL_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.RENTAL_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.OTHER_PAYMENTS_AMOUNT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_OTHER_PAYMENTS_AMOUNT;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    List<DocumentField> results = new ArrayList<>();
    boolean hasHouseHold =
        getValues(application.getApplicationData().getPagesData(), HAS_HOUSE_HOLD).contains("true");
    //Unearned Income
    DocumentField socialSecurityAmount= getUnearnedIncomeSection(application, "socialSecurityAmount", 
        SOCIAL_SECURITY_AMOUNT, UNEARNED_SOCIAL_SECURITY_AMOUNT, hasHouseHold);
    results.add(socialSecurityAmount);
    DocumentField supplementalSecurityIncomeAmount= getUnearnedIncomeSection(application, "supplementalSecurityIncomeAmount", 
        SSI_AMOUNT, UNEARNED_SSI_AMOUNT, hasHouseHold);
    results.add(supplementalSecurityIncomeAmount);
    DocumentField veteransBenefitsAmount= getUnearnedIncomeSection(application, "veteransBenefitsAmount", 
        VETERANS_BENEFITS_AMOUNT, UNEARNED_VETERANS_BENEFITS_AMOUNT, hasHouseHold);
    results.add(veteransBenefitsAmount);
    DocumentField unemploymentAmount= getUnearnedIncomeSection(application, "unemploymentAmount", 
        UNEMPLOYMENT_AMOUNT, UNEARNED_UNEMPLOYMENT_AMOUNT, hasHouseHold);
    results.add(unemploymentAmount);
    DocumentField workersCompensationAmount= getUnearnedIncomeSection(application, "workersCompensationAmount", 
        WORKERS_COMPENSATION_AMOUNT, UNEARNED_WORKERS_COMPENSATION_AMOUNT, hasHouseHold);
    results.add(workersCompensationAmount);
    DocumentField retirementAmount= getUnearnedIncomeSection(application, "retirementAmount", 
        RETIREMENT_AMOUNT, UNEARNED_RETIREMENT_AMOUNT, hasHouseHold);
    results.add(retirementAmount);
    DocumentField childOrSpousalSupportAmount= getUnearnedIncomeSection(application, "childOrSpousalSupportAmount", 
        CHILD_OR_SPOUSAL_SUPPORT_AMOUNT, UNEARNED_CHILD_OR_SPOUSAL_SUPPORT_AMOUNT, hasHouseHold);
    results.add(childOrSpousalSupportAmount);
    DocumentField tribalPaymentsAmount= getUnearnedIncomeSection(application, "tribalPaymentsAmount", 
        TRIBAL_PAYMENTS_AMOUNT, UNEARNED_TRIBAL_PAYMENTS_AMOUNT, hasHouseHold);
    results.add(tribalPaymentsAmount);
    //Others Unearned Income CCAP AND CERTAIN POPS
    DocumentField benefitsAmount= getUnearnedIncomeSection(application, "benefitsAmount", 
        BENEFITS_PROGRAMS_AMOUNT, UNEARNED_BENEFITS_PROGRAMS_AMOUNT, hasHouseHold);
    results.add(benefitsAmount);
    DocumentField insurancePaymentsAmount= getUnearnedIncomeSection(application, "insurancePaymentsAmount", 
        INSURANCE_PAYMENTS_AMOUNT, UNEARNED_INSURANCE_PAYMENTS_AMOUNT, hasHouseHold);
    results.add(insurancePaymentsAmount);
    DocumentField contractForDeedAmount= getUnearnedIncomeSection(application, "contractForDeedAmount", 
        CONTRACT_FOR_DEED_AMOUNT, UNEARNED_CONTRACT_FOR_DEED_AMOUNT, hasHouseHold);
    results.add(contractForDeedAmount);
    DocumentField trustMoneyAmount= getUnearnedIncomeSection(application, "trustMoneyAmount", 
        TRUST_MONEY_AMOUNT, UNEARNED_TRUST_MONEY_AMOUNT, hasHouseHold);
    results.add(trustMoneyAmount);
    DocumentField healthcareReimbursementAmount= getUnearnedIncomeSection(application, "healthcareReimbursementAmount", 
        HEALTHCARE_REIMBURSEMENT_AMOUNT, UNEARNED_HEALTHCARE_REIMBURSEMENT_AMOUNT, hasHouseHold);
    results.add(healthcareReimbursementAmount);
    DocumentField interestDividendsAmount= getUnearnedIncomeSection(application, "interestDividendsAmount", 
        INTEREST_DIVIDENDS_AMOUNT, UNEARNED_INTEREST_DIVIDENDS_AMOUNT, hasHouseHold);
    results.add(interestDividendsAmount);
    DocumentField rentalIncomeAmount= getUnearnedIncomeSection(application, "rentalIncomeAmount", 
        RENTAL_AMOUNT, UNEARNED_RENTAL_AMOUNT, hasHouseHold);
    results.add(rentalIncomeAmount);
    DocumentField otherPaymentsAmount= getUnearnedIncomeSection(application, "otherPaymentsAmount", 
        OTHER_PAYMENTS_AMOUNT, UNEARNED_OTHER_PAYMENTS_AMOUNT, hasHouseHold);
    results.add(otherPaymentsAmount);
    
    return results;
  }
  
  @NotNull
  private static DocumentField getUnearnedIncomeSection(Application application, String inputName,
      Field individualInput, Field hhInput, boolean hasHouseHold) {
    var individualAmt = getValues(application.getApplicationData().getPagesData(), individualInput);
    var hhAmt = getValues(application.getApplicationData().getPagesData(), hhInput);
    var totAmt = hhAmt.stream().filter(amt -> !amt.isEmpty()).map(BigDecimal::new)
        .reduce(BigDecimal.ZERO,BigDecimal::add).toString();
    return new DocumentField("unearnedIncomeSource", inputName,
        hasHouseHold ? (totAmt.equals("0")?"":totAmt) : individualAmt.toString(),
        DocumentFieldType.SINGLE_VALUE);

  }
}
