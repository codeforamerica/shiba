package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.ENUMERATED_SINGLE_VALUE;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class UtilityPaymentsInputsMapperTest {

  private final UtilityPaymentsInputsMapper mapper = new UtilityPaymentsInputsMapper();

  @Test
  public void testNonExpeditedPayments() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("utilityPayments", "payForUtilities",
            List.of("SEWER"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput("utilityPayments", "noExpeditedUtilitiesSelected", "true",
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("utilityPayments", "NO_EXPEDITED_UTILITIES_SELECTED",
            "NO_EXPEDITED_UTILITIES_SELECTED", ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("utilityPayments", "heatingOrCoolingSelection", "NEITHER_SELECTED",
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("utilityPayments", "waterOrSewer", "WATER_OR_SEWER",
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("utilityPayments", "waterOrSewerSelection", "ONE_SELECTED",
            ENUMERATED_SINGLE_VALUE
        )
    );
  }

  @Test
  public void testExpeditedPayments() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("utilityPayments", "payForUtilities",
            List.of("HEATING"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput("utilityPayments", "heatingOrCoolingSelection", "ONE_SELECTED",
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("utilityPayments", "waterOrSewerSelection", "NEITHER_SELECTED",
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("utilityPayments", "heatingOrCooling", "HEATING_OR_COOLING",
            ENUMERATED_SINGLE_VALUE),
        new ApplicationInput("utilityPayments", "HEATING", "true", ENUMERATED_SINGLE_VALUE));
  }

  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }
}
