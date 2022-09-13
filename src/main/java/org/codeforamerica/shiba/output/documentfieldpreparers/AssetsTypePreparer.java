package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ASSETS_TYPE;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AssetsTypePreparer extends OneToManyDocumentFieldPreparer {

  private static final List<String> ASSETS_TYPE_OPTIONS = List.of("VEHICLE", "STOCK_BOND",
      "LIFE_INSURANCE",
      "BURIAL_ACCOUNT", "OWNERSHIP_BUSINESS", "REAL_ESTATE", "ONE_MILLION_ASSETS",
      "CONTRACTS_NOTES_AGREEMENTS", "TRUST_OR_ANNUITY", "OTHER_ASSETS");

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams("assets", ASSETS_TYPE, ASSETS_TYPE_OPTIONS);
  }
}
