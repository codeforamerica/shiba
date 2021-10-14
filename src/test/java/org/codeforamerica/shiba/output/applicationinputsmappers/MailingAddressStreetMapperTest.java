package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_COUNTY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class MailingAddressStreetMapperTest {

  private final MailingAddressStreetMapper mapper = new MailingAddressStreetMapper();

  @Test
  void shouldMapSameAsHomeAddressEnriched() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withEnrichedHomeAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", "true")
        .withPageData("mailingAddress", "sameMailingAddress", "true")
        .build();
    PagesData pagesData = applicationData.getPagesData();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput(pagesData, "selectedStreetAddress", ENRICHED_HOME_STREET),
        createApplicationInput(pagesData, "selectedApartmentNumber",
            ENRICHED_HOME_APARTMENT_NUMBER),
        createApplicationInput(pagesData, "selectedCity", ENRICHED_HOME_CITY),
        createApplicationInput(pagesData, "selectedState", ENRICHED_HOME_STATE),
        createApplicationInput(pagesData, "selectedZipCode", ENRICHED_HOME_ZIPCODE),
        createApplicationInput(pagesData, "selectedCounty", ENRICHED_HOME_COUNTY));
  }

  @Test
  void shouldMapSameAsHomeAddressNotEnriched() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withEnrichedHomeAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", "false")
        .withPageData("mailingAddress", "sameMailingAddress", "true")
        .build();
    PagesData pagesData = applicationData.getPagesData();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput(pagesData, "selectedStreetAddress", HOME_STREET),
        createApplicationInput(pagesData, "selectedApartmentNumber", HOME_APARTMENT_NUMBER),
        createApplicationInput(pagesData, "selectedCity", HOME_CITY),
        createApplicationInput(pagesData, "selectedState", HOME_STATE),
        createApplicationInput(pagesData, "selectedZipCode", HOME_ZIPCODE),
        createApplicationInput(pagesData, "selectedCounty", HOME_COUNTY));
  }

  @Test
  void shouldMapEnrichedMailingAddressIfDifferentThanHomeAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withMailingAddress()
        .withEnrichedMailingAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", "true")
        .withPageData("mailingAddressValidation", "useEnrichedAddress", "true")
        .withPageData("mailingAddress", "sameMailingAddress", "")
        .build();
    PagesData pagesData = applicationData.getPagesData();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput(pagesData, "selectedStreetAddress", ENRICHED_MAILING_STREET),
        createApplicationInput(pagesData, "selectedApartmentNumber",
            ENRICHED_MAILING_APARTMENT_NUMBER),
        createApplicationInput(pagesData, "selectedCity", ENRICHED_MAILING_CITY),
        createApplicationInput(pagesData, "selectedState", ENRICHED_MAILING_STATE),
        createApplicationInput(pagesData, "selectedZipCode", ENRICHED_MAILING_ZIPCODE),
        createApplicationInput(pagesData, "selectedCounty", ENRICHED_MAILING_COUNTY));
  }

  @Test
  void shouldMapMailingAddressIfDifferentThanHomeAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withMailingAddress()
        .withEnrichedMailingAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", "true")
        .withPageData("mailingAddressValidation", "useEnrichedAddress", "false")
        .withPageData("mailingAddress", "sameMailingAddress", "")
        .build();
    PagesData pagesData = applicationData.getPagesData();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput(pagesData, "selectedStreetAddress", MAILING_STREET),
        createApplicationInput(pagesData, "selectedApartmentNumber", MAILING_APARTMENT_NUMBER),
        createApplicationInput(pagesData, "selectedCity", MAILING_CITY),
        createApplicationInput(pagesData, "selectedState", MAILING_STATE),
        createApplicationInput(pagesData, "selectedZipCode", MAILING_ZIPCODE),
        createApplicationInput(pagesData, "selectedCounty", MAILING_COUNTY));
  }

  @Test
  void shouldMapForGeneralDelivery() {
    String expectedCityInput = "Ada";
    String expectedZipcodeInput = "12345";
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .noPermamentAddress()
        .withPageData("cityForGeneralDelivery", "whatIsTheCity", expectedCityInput)
        .withPageData("cityForGeneralDelivery", "enrichedZipcode", expectedZipcodeInput)
        .build();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput("selectedStreetAddress", "General Delivery"),
        createApplicationInput("selectedCity", expectedCityInput),
        createApplicationInput("selectedState", "MN"),
        createApplicationInput("selectedZipCode", expectedZipcodeInput));
  }

  @NotNull
  private ApplicationInput createApplicationInput(PagesData pagesData, String name, Field field) {
    return createApplicationInput(name, getFirstValue(pagesData, field));
  }

  @NotNull
  private ApplicationInput createApplicationInput(String name, String value) {
    return new ApplicationInput("mailingAddress", name, value, SINGLE_VALUE);
  }
}