package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class HomeAddressStreetMapperTest {

  private final HomeAddressStreetMapper mapper = new HomeAddressStreetMapper();
  private final Application application = Application.builder().build();

  @Test
  void shouldSayNotPermanentWhenClientDoesNotHaveAPermanentAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .noPermanentAddress()
        .withPageData("homeAddress", "streetAddress", List.of(""))
        .build();
    application.setApplicationData(applicationData);

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).containsOnly(
        createHomeAddressApplicationInput("streetAddressWithPermanentAddress",
            "No permanent address"),
        createHomeAddressApplicationInput("selectedZipCode", ""),
        createHomeAddressApplicationInput("selectedCity", ""),
        createHomeAddressApplicationInput("selectedState", "MN"),
        createHomeAddressApplicationInput("selectedApartmentNumber", ""),
        createHomeAddressApplicationInput("selectedCounty", "")
    );
  }

  @Test
  void shouldUseClientInputAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withEnrichedHomeAddress()
        .withPageData("homeAddress", "isHomeless", List.of())
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("false"))
        .build();
    application.setApplicationData(applicationData);

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).containsOnly(
        createHomeAddressApplicationInput("streetAddressWithPermanentAddress",
            "street"),
        createHomeAddressApplicationInput("selectedZipCode", "02103"),
        createHomeAddressApplicationInput("selectedCity", "city"),
        createHomeAddressApplicationInput("selectedState", "CA"),
        createHomeAddressApplicationInput("selectedApartmentNumber", "ste 123"),
        createHomeAddressApplicationInput("selectedCounty", "")
    );
  }

  @Test
  void shouldUseEnrichedAddress() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHomeAddress()
        .withEnrichedHomeAddress()
        .withPageData("homeAddress", "isHomeless", List.of())
        .withPageData("homeAddressValidation", "useEnrichedAddress", List.of("true"))
        .build();
    application.setApplicationData(applicationData);

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).containsOnly(
        createHomeAddressApplicationInput("streetAddressWithPermanentAddress",
            "smarty street"),
        createHomeAddressApplicationInput("selectedZipCode", "02103-9999"),
        createHomeAddressApplicationInput("selectedCity", "smarty city"),
        createHomeAddressApplicationInput("selectedState", "CA"),
        createHomeAddressApplicationInput("selectedApartmentNumber", "apt 123"),
        createHomeAddressApplicationInput("selectedCounty", Collections.emptyList())
    );
  }

  @Test
  void shouldNotIncludeApplicationInputs_whenThereIsNoHomeAddress() {
    application.setApplicationData(new ApplicationData());

    List<ApplicationInput> map = mapper.map(application, null, null, null);

    assertThat(map).isEmpty();
  }

  private ApplicationInput createHomeAddressApplicationInput(String name, List<String> value) {
    return new ApplicationInput("homeAddress", name, value, ApplicationInputType.SINGLE_VALUE);
  }

  private ApplicationInput createHomeAddressApplicationInput(String name, String value) {
    return new ApplicationInput("homeAddress", name, value, ApplicationInputType.SINGLE_VALUE);
  }
}