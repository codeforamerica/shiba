package org.codeforamerica.shiba.output.applicationinputsmappers;

import static java.lang.Boolean.parseBoolean;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.NO_PERMANENT_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.USE_ENRICHED_HOME_ADDRESS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
public class HomeAddressStreetMapper implements ApplicationInputsMapper {

  private final static String NO_PERMANENT_ADDRESS_STREET = "No permanent address";

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
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
          "MN");
    }

    if (parseBoolean(getFirstValue(pagesData, USE_ENRICHED_HOME_ADDRESS))) {
      return createAddressInputs(
          getFirstValue(pagesData, ENRICHED_HOME_STREET),
          getFirstValue(pagesData, ENRICHED_HOME_APARTMENT_NUMBER),
          getFirstValue(pagesData, ENRICHED_HOME_ZIPCODE),
          getFirstValue(pagesData, ENRICHED_HOME_CITY),
          getFirstValue(pagesData, ENRICHED_HOME_STATE));
    } else {
      return createAddressInputs(
          getFirstValue(pagesData, HOME_STREET),
          getFirstValue(pagesData, HOME_APARTMENT_NUMBER),
          getFirstValue(pagesData, HOME_ZIPCODE),
          getFirstValue(pagesData, HOME_CITY),
          getFirstValue(pagesData, HOME_STATE));
    }
  }

  private List<ApplicationInput> createAddressInputs(String street, String apartment,
      String zipcode, String city, String state) {
    return List.of(new ApplicationInput(
        "homeAddress",
        "streetAddressWithPermanentAddress",
        street,
        SINGLE_VALUE
    ), new ApplicationInput(
        "homeAddress",
        "selectedApartmentNumber",
        apartment,
        SINGLE_VALUE
    ), new ApplicationInput(
        "homeAddress",
        "selectedZipCode",
        zipcode,
        SINGLE_VALUE
    ), new ApplicationInput(
        "homeAddress",
        "selectedCity",
        city,
        SINGLE_VALUE
    ), new ApplicationInput(
        "homeAddress",
        "selectedState",
        state,
        SINGLE_VALUE
    ));
  }
}
