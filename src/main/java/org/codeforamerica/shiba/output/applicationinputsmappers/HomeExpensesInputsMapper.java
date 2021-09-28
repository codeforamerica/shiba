package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_EXPENSES;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HomeExpensesInputsMapper extends OneToManyApplicationInputsMapper {

  private static final List<String> HOME_EXPENSES_OPTIONS = List.of("RENT", "MORTGAGE",
      "HOMEOWNERS_INSURANCE", "REAL_ESTATE_TAXES", "ASSOCIATION_FEES", "ROOM_AND_BOARD");

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams("homeExpenses", HOME_EXPENSES, HOME_EXPENSES_OPTIONS);
  }
}
