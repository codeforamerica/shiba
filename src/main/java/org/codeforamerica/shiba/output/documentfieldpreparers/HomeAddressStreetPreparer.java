package org.codeforamerica.shiba.output.documentfieldpreparers;

import static java.lang.Boolean.parseBoolean;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.NO_PERMANENT_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.USE_ENRICHED_HOME_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class HomeAddressStreetPreparer implements DocumentFieldPreparer {

  private final static String NO_PERMANENT_ADDRESS_STREET = "No permanent address";

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    PagesData pagesData = application.getApplicationData().getPagesData();
    PageData homeAddressPageData = pagesData.getPage("homeAddress");
    if (homeAddressPageData == null) {
      return List.of();
    }

    // Empty fields for no-permanent-address
    if (parseBoolean(getFirstValue(pagesData, NO_PERMANENT_ADDRESS))) {
      return createAddressInputs(NO_PERMANENT_ADDRESS_STREET,
          "",
          "",
          "",
          "MN",
          "");
    }

    if (parseBoolean(getFirstValue(pagesData, USE_ENRICHED_HOME_ADDRESS))) {
      return createAddressInputs(
          getFirstValue(pagesData, ENRICHED_HOME_STREET),
          getFirstValue(pagesData, ENRICHED_HOME_APARTMENT_NUMBER),
          getFirstValue(pagesData, ENRICHED_HOME_ZIPCODE),
          getFirstValue(pagesData, ENRICHED_HOME_CITY),
          getFirstValue(pagesData, ENRICHED_HOME_STATE),
          getFirstValue(pagesData, ENRICHED_HOME_COUNTY));
    } else {
      return createAddressInputs(
          getFirstValue(pagesData, HOME_STREET),
          getFirstValue(pagesData, HOME_APARTMENT_NUMBER),
          getFirstValue(pagesData, HOME_ZIPCODE),
          getFirstValue(pagesData, HOME_CITY),
          getFirstValue(pagesData, HOME_STATE),
          getFirstValue(pagesData, HOME_COUNTY));
    }
  }

  private List<DocumentField> createAddressInputs(String street, String apartment,
      String zipcode, String city, String state, String county) {
    // county Fields default to "Other" but we don't want to write that to the PDF
    if (County.Other.toString().equals(county)) {
      county = "";
    }
    return List.of(
        createSingleHomeAddressInput("streetAddressWithPermanentAddress", street),
        createSingleHomeAddressInput("selectedApartmentNumber", apartment),
        createSingleHomeAddressInput("selectedZipCode", zipcode),
        createSingleHomeAddressInput("selectedCity", city),
        createSingleHomeAddressInput("selectedState", state),
        createSingleHomeAddressInput("selectedCounty", county));
  }

  private DocumentField createSingleHomeAddressInput(String name, String value) {
    return new DocumentField("homeAddress", name, value, SINGLE_VALUE);
  }
}
