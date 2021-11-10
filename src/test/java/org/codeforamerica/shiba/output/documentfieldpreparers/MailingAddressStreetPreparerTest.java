package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.OtterTail;
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
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.MAILING_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MailingAddressStreetPreparerTest {

  private final CountyMap<CountyRoutingDestination> countyMap = new CountyMap<>();
  private final CityInfoConfiguration cityInfo = new CityInfoConfiguration();

  private final MailingAddressStreetPreparer preparer =
      new MailingAddressStreetPreparer(cityInfo, countyMap);

  @BeforeEach
  void setup() {
    Address hennepinPostOfficeAddress = new Address("123 hennepin st", "Minneapolis", "MN", "55555",
        null, "Hennepin");
    countyMap.getCounties().putAll(Map.of(
        Hennepin, CountyRoutingDestination.builder()
            .county(Hennepin).phoneNumber("765-4321")
            .postOfficeAddress(hennepinPostOfficeAddress).build(),
        OtterTail, CountyRoutingDestination.builder()
            .county(OtterTail).phoneNumber("123-4567").build()));

    cityInfo.getCityToZipAndCountyMapping().putAll(Map.of(
        "Ada",
        Map.of("displayName", "Ada", "zipcode", "56515", "county", "OtterTail"),
        "Plymouth",
        Map.of("displayName", "Plymouth", "zipcode", "55555", "county", "Hennepin")));
  }

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

    List<DocumentField> map = preparer.prepareDocumentFields(application, null, null, null);

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

    List<DocumentField> map = preparer.prepareDocumentFields(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput(pagesData, "selectedStreetAddress", HOME_STREET),
        createApplicationInput(pagesData, "selectedApartmentNumber", HOME_APARTMENT_NUMBER),
        createApplicationInput(pagesData, "selectedCity", HOME_CITY),
        createApplicationInput(pagesData, "selectedState", HOME_STATE),
        createApplicationInput(pagesData, "selectedZipCode", HOME_ZIPCODE),
        createApplicationInput("selectedCounty", ""));
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

    List<DocumentField> map = preparer.prepareDocumentFields(application, null, null, null);

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

    List<DocumentField> map = preparer.prepareDocumentFields(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput(pagesData, "selectedStreetAddress", MAILING_STREET),
        createApplicationInput(pagesData, "selectedApartmentNumber", MAILING_APARTMENT_NUMBER),
        createApplicationInput(pagesData, "selectedCity", MAILING_CITY),
        createApplicationInput(pagesData, "selectedState", MAILING_STATE),
        createApplicationInput(pagesData, "selectedZipCode", MAILING_ZIPCODE),
        createApplicationInput("selectedCounty", ""));
  }

  @Test
  void shouldMapForGeneralDeliveryNoPostOfficeShouldShowBlankStreet() {
    String expectedCityInput = "Ada";
    String expectedZipcodeInput = "12345";
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .noPermanentAddress()
        .withPageData("cityForGeneralDelivery", "whatIsTheCity", expectedCityInput)
        .withPageData("cityForGeneralDelivery", "enrichedZipcode", expectedZipcodeInput)
        .build();
    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> map = preparer.prepareDocumentFields(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput("selectedStreetAddress", "General Delivery"),
        createApplicationInput("selectedCity", expectedCityInput),
        createApplicationInput("selectedState", "MN"),
        createApplicationInput("selectedZipCode", expectedZipcodeInput));
  }

  @Test
  void shouldMapForGeneralDeliveryWithPostOfficeShouldShowPostOfficeForMailing() {
    String expectedCityInput = "Minneapolis";
    String expectedZipcodeInput = "55555";
    String expectedStreetAddress = "123 hennepin st";
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .noPermanentAddress()
        .withPageData("cityForGeneralDelivery", "whatIsTheCity", "Plymouth")
        .withPageData("cityForGeneralDelivery", "enrichedZipcode", "54321")
        .withPageData("cityForGeneralDelivery", "enrichedStreetAddress", expectedStreetAddress)
        .build();
    Application application = Application.builder().applicationData(applicationData).build();

    List<DocumentField> map = preparer.prepareDocumentFields(application, null, null, null);

    assertThat(map).containsOnly(
        createApplicationInput("selectedStreetAddress", expectedStreetAddress),
        createApplicationInput("selectedCity", expectedCityInput),
        createApplicationInput("selectedState", "MN"),
        createApplicationInput("selectedZipCode", expectedZipcodeInput));
  }

  @NotNull
  private DocumentField createApplicationInput(PagesData pagesData, String name, Field field) {
    return createApplicationInput(name, getFirstValue(pagesData, field));
  }

  @NotNull
  private DocumentField createApplicationInput(String name, String value) {
    return new DocumentField("mailingAddress", name, value, SINGLE_VALUE);
  }
}