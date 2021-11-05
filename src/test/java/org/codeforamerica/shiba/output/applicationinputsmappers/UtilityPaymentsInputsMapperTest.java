package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInput;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
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

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("utilityPayments", "PHONE", "false"),
        createApplicationInput("utilityPayments", "GARBAGE_REMOVAL", "false"),
        createApplicationInput("utilityPayments", "ELECTRICITY", "false"),
        createApplicationInput("utilityPayments", "COOKING_FUEL", "false"),

        createApplicationInput("utilityPayments", "noExpeditedUtilitiesSelected", "true"),
        createApplicationInput("utilityPayments", "NO_EXPEDITED_UTILITIES_SELECTED",
            "NO_EXPEDITED_UTILITIES_SELECTED"),
        createApplicationInput("utilityPayments", "heatingOrCoolingSelection", "NEITHER_SELECTED"),
        createApplicationInput("utilityPayments", "waterOrSewer", "WATER_OR_SEWER"),
        createApplicationInput("utilityPayments", "waterOrSewerSelection", "ONE_SELECTED"
        )
    );
  }

  @Test
  public void testExpeditedPayments() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("utilityPayments", "payForUtilities",
            List.of("HEATING"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("utilityPayments", "PHONE", "false"),
        createApplicationInput("utilityPayments", "GARBAGE_REMOVAL", "false"),
        createApplicationInput("utilityPayments", "ELECTRICITY", "false"),
        createApplicationInput("utilityPayments", "COOKING_FUEL", "false"),

        createApplicationInput("utilityPayments", "heatingOrCoolingSelection", "ONE_SELECTED"),
        createApplicationInput("utilityPayments", "waterOrSewerSelection", "NEITHER_SELECTED"),
        createApplicationInput("utilityPayments", "heatingOrCooling", "HEATING_OR_COOLING"),
        createApplicationInput("utilityPayments", "HEATING", "true")
    );
  }

  @Test
  public void shouldMapNoneOfTheAboveToNoForYesNoOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("utilityPayments", "payUtilities", List.of("NONE_OF_THE_ABOVE"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("utilityPayments", "PHONE", "false"),
        createApplicationInput("utilityPayments", "GARBAGE_REMOVAL", "false"),
        createApplicationInput("utilityPayments", "ELECTRICITY", "false"),
        createApplicationInput("utilityPayments", "COOKING_FUEL", "false"),

        createApplicationInput("utilityPayments", "noExpeditedUtilitiesSelected", "true"),
        createApplicationInput("utilityPayments", "NO_EXPEDITED_UTILITIES_SELECTED",
            "NO_EXPEDITED_UTILITIES_SELECTED"),
        createApplicationInput("utilityPayments", "heatingOrCoolingSelection", "NEITHER_SELECTED"),
        createApplicationInput("utilityPayments", "waterOrSewerSelection", "NEITHER_SELECTED")
    );
  }

  @Test
  public void shouldMapYesToSelectedOptions() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("utilityPayments", "payForUtilities", List.of("HEATING", "PHONE"))
        .build();

    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("utilityPayments", "PHONE", "true"),
        createApplicationInput("utilityPayments", "GARBAGE_REMOVAL", "false"),
        createApplicationInput("utilityPayments", "ELECTRICITY", "false"),
        createApplicationInput("utilityPayments", "COOKING_FUEL", "false"),

        createApplicationInput("utilityPayments", "HEATING", "true"),
        createApplicationInput("utilityPayments", "heatingOrCoolingSelection", "ONE_SELECTED"),
        createApplicationInput("utilityPayments", "heatingOrCooling", "HEATING_OR_COOLING"),
        createApplicationInput("utilityPayments", "waterOrSewerSelection", "NEITHER_SELECTED"),
        createApplicationInput("utilityPayments", "phoneCellPhone", "PHONE_CELL_PHONE")
    );
  }

  @Test
  public void shouldReturnEmptyForMissingData() {
    ApplicationData applicationData = new ApplicationData();
    List<DocumentField> result = mapper.prepareDocumentFields(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).isEmpty();
  }
}
