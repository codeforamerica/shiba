package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UNEARNED_INCOME;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RaceAndEthnicityMapper extends OneToManyApplicationInputsMapper {

  private static final List<String> UNEARNED_INCOME_OPTIONS = List.of("SOCIAL_SECURITY", "SSI",
      "VETERANS_BENEFITS",
      "UNEMPLOYMENT", "WORKERS_COMPENSATION", "RETIREMENT", "CHILD_OR_SPOUSAL_SUPPORT",
      "TRIBAL_PAYMENTS");

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams("unearnedIncome", UNEARNED_INCOME, UNEARNED_INCOME_OPTIONS);
  }
}
