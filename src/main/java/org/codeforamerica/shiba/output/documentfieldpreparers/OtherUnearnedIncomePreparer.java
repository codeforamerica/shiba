package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME_OTHER;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OtherUnearnedIncomePreparer extends OneToManyDocumentFieldPreparer {

  private static final List<String> UNEARNED_INCOME_OTHER_OPTIONS = List.of("BENEFITS",
      "INSURANCE_PAYMENTS", "CONTRACT_FOR_DEED", "TRUST_MONEY", "HEALTH_CARE_REIMBURSEMENT",
      "INTEREST_DIVIDENDS", "OTHER_PAYMENTS", "RENTAL_INCOME");

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams(
        "otherUnearnedIncome",
        UNEARNED_INCOME_OTHER,
        UNEARNED_INCOME_OTHER_OPTIONS);
  }
}
