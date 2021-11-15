package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.UTILITY_PAYMENTS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * PDF generator interprets ENUMERATED_MULTI_VALUE as "Yes" if the field is there, so it will do the
 * "COOKING_FUEL" -> "Yes" on the acroform. XML doesn't do that but it replaces the values with the
 * enums it has listed in xml-mappings, "COOKING_FUEL" -> "Cooking Fuel". Having multiple
 * application inputs with different values is needed because the XML doesn't have the radio buttons
 * to write "Yes".
 */
@Component
public class UtilityPaymentsPreparer extends OneToManyDocumentFieldPreparer {

  private static final List<String> UTILITY_PAYMENTS_OPTIONS = List.of("ELECTRICITY", "PHONE",
      "GARBAGE_REMOVAL", "COOKING_FUEL");

  private static final List<String> EXPEDITED_UTILITY_PAYMENTS = List.of("HEATING", "COOLING",
      "ELECTRICITY", "PHONE");
  private static final List<String> HEATING_OR_COOLING = List.of("HEATING", "COOLING");
  private static final List<String> WATER_OR_SEWER = List.of("WATER", "SEWER");

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document _document,
      Recipient _recipient) {
    return map(application.getApplicationData().getPagesData());
  }

  @Override
  protected OneToManyParams getParams() {
    return new OneToManyParams("utilityPayments", UTILITY_PAYMENTS, UTILITY_PAYMENTS_OPTIONS);
  }

  protected List<DocumentField> map(PagesData pagesData) {
    // Question was unanswered
    if (pagesData.get("utilityPayments") == null) {
      return Collections.emptyList();
    }

    List<DocumentField> results = super.map(pagesData);

    List<String> utilityPayments = getValues(pagesData, UTILITY_PAYMENTS);

    // No expedited utilities selected - need multiple inputs because they are written differently
    // to xml and pdfs
    if (utilityPayments.stream().noneMatch(EXPEDITED_UTILITY_PAYMENTS::contains)) {
      results.add(new DocumentField("utilityPayments", "noExpeditedUtilitiesSelected",
          "true", ENUMERATED_SINGLE_VALUE));
      results.add(new DocumentField("utilityPayments", "NO_EXPEDITED_UTILITIES_SELECTED",
          "NO_EXPEDITED_UTILITIES_SELECTED", ENUMERATED_SINGLE_VALUE));
    }

    boolean heatingOrCooling = utilityPayments.stream().anyMatch(HEATING_OR_COOLING::contains);
    boolean waterOrSewer = utilityPayments.stream().anyMatch(WATER_OR_SEWER::contains);

    // Mark these radio buttons on the PDF
    results.add(new DocumentField("utilityPayments", "heatingOrCoolingSelection",
        heatingOrCooling ? "ONE_SELECTED" : "NEITHER_SELECTED",
        ENUMERATED_SINGLE_VALUE));
    results.add(new DocumentField("utilityPayments", "waterOrSewerSelection",
        waterOrSewer ? "ONE_SELECTED" : "NEITHER_SELECTED",
        ENUMERATED_SINGLE_VALUE));

    // Write these values explicitly on the XML
    if (heatingOrCooling) {
      results.add(new DocumentField("utilityPayments", "heatingOrCooling",
          "HEATING_OR_COOLING",
          ENUMERATED_SINGLE_VALUE));
    }
    if (waterOrSewer) {
      results.add(new DocumentField("utilityPayments", "waterOrSewer",
          "WATER_OR_SEWER",
          ENUMERATED_SINGLE_VALUE));
    }
    if (utilityPayments.contains("PHONE")) {
      results.add(new DocumentField("utilityPayments", "phoneCellPhone",
          "PHONE_CELL_PHONE",
          ENUMERATED_SINGLE_VALUE));
    }

    HEATING_OR_COOLING.stream()
        .filter(utilityPayments::contains)
        .map(this::createApplicationInput)
        .forEach(results::add);

    return results;
  }

  @NotNull
  private DocumentField createApplicationInput(String name) {
    return new DocumentField("utilityPayments",
        name,
        "true",
        ENUMERATED_SINGLE_VALUE);
  }
}
