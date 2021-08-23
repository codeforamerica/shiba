package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_CITY;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STATE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_STREET;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_HOME_ZIPCODE;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_APARTMENT_NUMBER;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.ENRICHED_MAILING_CITY;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.caf.MailingAddressStreetMapper;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MailingAddressStreetMapperTest {

  private final FeatureFlagConfiguration featureFlagConfiguration = mock(
      FeatureFlagConfiguration.class);
  private final MailingAddressStreetMapper mapper = new MailingAddressStreetMapper(
      featureFlagConfiguration);

  @BeforeEach
  void setUp() {
    when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.ON);
  }

  @Test
  void shouldMapSameAsHomeAddressEnriched() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withEnrichedHomeAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("true"))
        .withPageData("mailingAddress", "sameMailingAddress", List.of("true"))
        .build();
    PagesData pagesData = applicationData.getPagesData();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_STREET)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_APARTMENT_NUMBER)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_CITY)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_STATE)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE));

    // Flag off
    when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
    applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withEnrichedHomeAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("true"))
        .withPageData("homeAddress", "sameMailingAddress", List.of("true"))
        .build();
    pagesData = applicationData.getPagesData();
    application = Application.builder().applicationData(applicationData).build();
    map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_STREET)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_APARTMENT_NUMBER)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_CITY)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_STATE)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, ENRICHED_HOME_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE));
  }

  @Test
  void shouldMapSameAsHomeAddressNotEnriched() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withEnrichedHomeAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("false"))
        .withPageData("mailingAddress", "sameMailingAddress", List.of("true"))
        .build();
    PagesData pagesData = applicationData.getPagesData();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, HOME_STREET)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, HOME_APARTMENT_NUMBER)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, HOME_CITY)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, HOME_STATE)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, HOME_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE));

    // Flag off
    when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
    applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withEnrichedHomeAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("false"))
        .withPageData("homeAddress", "sameMailingAddress", List.of("true"))
        .build();
    pagesData = applicationData.getPagesData();
    application = Application.builder().applicationData(applicationData).build();
    map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, HOME_STREET)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, HOME_APARTMENT_NUMBER)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, HOME_CITY)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, HOME_STATE)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, HOME_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE));
  }

  @Test
  void shouldMapEnrichedMailingAddressIfDifferentThanHomeAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withMailingAddress()
        .withEnrichedMailingAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("true"))
        .withPageData("mailingAddressValidation", "useEnrichedAddress", List.of("true"))
        .withPageData("mailingAddress", "sameMailingAddress", List.of(""))
        .build();
    PagesData pagesData = applicationData.getPagesData();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_STREET)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_APARTMENT_NUMBER)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_CITY)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_STATE)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE));

    // Flag off
    when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
    applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withMailingAddress()
        .withEnrichedMailingAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("true"))
        .withPageData("mailingAddressValidation", "useEnrichedAddress", List.of("true"))
        .withPageData("homeAddress", "sameMailingAddress", List.of(""))
        .build();
    pagesData = applicationData.getPagesData();
    application = Application.builder().applicationData(applicationData).build();
    map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_STREET)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_APARTMENT_NUMBER)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_CITY)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_STATE)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, ENRICHED_MAILING_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE));
  }

  @Test
  void shouldMapMailingAddressIfDifferentThanHomeAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withMailingAddress()
        .withEnrichedMailingAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("true"))
        .withPageData("mailingAddressValidation", "useEnrichedAddress", List.of("false"))
        .withPageData("mailingAddress", "sameMailingAddress", List.of(""))
        .build();
    PagesData pagesData = applicationData.getPagesData();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, MAILING_STREET)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, MAILING_APARTMENT_NUMBER)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, MAILING_CITY)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, MAILING_STATE)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, MAILING_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE));

    // Flag off
    when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
    applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withMailingAddress()
        .withEnrichedMailingAddress()
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("true"))
        .withPageData("mailingAddressValidation", "useEnrichedAddress", List.of("false"))
        .withPageData("homeAddress", "sameMailingAddress", List.of(""))
        .build();
    pagesData = applicationData.getPagesData();
    application = Application.builder().applicationData(applicationData).build();

    map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of(getFirstValue(pagesData, MAILING_STREET)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedApartmentNumber",
        List.of(getFirstValue(pagesData, MAILING_APARTMENT_NUMBER)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        List.of(getFirstValue(pagesData, MAILING_CITY)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of(getFirstValue(pagesData, MAILING_STATE)),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        List.of(getFirstValue(pagesData, MAILING_ZIPCODE)),
        ApplicationInputType.SINGLE_VALUE));
  }

  @Test
  void shouldMapForGeneralDelivery() {
    List<String> expectedCityInput = List.of("Ada");
    List<String> expectedZipcodeInput = List.of("12345");
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .isHomeless()
        .withPageData("cityForGeneralDelivery", "whatIsTheCity", expectedCityInput)
        .withPageData("cityForGeneralDelivery", "enrichedZipcode", expectedZipcodeInput)
        .build();
    Application application = Application.builder().applicationData(applicationData).build();

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedStreetAddress",
        List.of("General Delivery"),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedCity",
        expectedCityInput,
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedState",
        List.of("MN"),
        ApplicationInputType.SINGLE_VALUE));
    assertThat(map).contains(new ApplicationInput("mailingAddress",
        "selectedZipCode",
        expectedZipcodeInput,
        ApplicationInputType.SINGLE_VALUE));
  }
}
