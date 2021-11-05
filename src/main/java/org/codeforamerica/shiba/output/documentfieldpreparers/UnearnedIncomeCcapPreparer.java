package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME_CCAP;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UnearnedIncomeCcapPreparer extends OneToManyDocumentFieldPreparer {

  private static final List<String> UNEARNED_INCOME_CCAP_OPTIONS = List.of("BENEFITS",
      "INSURANCE_PAYMENTS", "CONTRACT_FOR_DEED", "TRUST_MONEY", "HEALTH_CARE_REIMBURSEMENT",
      "INTEREST_DIVIDENDS", "OTHER_SOURCES");

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams(
        "unearnedIncomeCcap",
        UNEARNED_INCOME_CCAP,
        UNEARNED_INCOME_CCAP_OPTIONS);
  }
}
