package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.TestUtils.createApplicationInput;

import java.util.List;
import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class BasicCriteriaInputsMapperTest {

  private final BasicCriteriaInputsMapper mapper = new BasicCriteriaInputsMapper();

  @Test
  public void testBlindOrHasDisability() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of(Program.CERTAIN_POPS))
        .withPageData("basicCriteria", "basicCriteria", List.of("SIXTY_FIVE_OR_OLDER", "BLIND"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("basicCriteria", "SIXTY_FIVE_OR_OLDER", "true"),
        createApplicationInput("basicCriteria", "BLIND", "true"),
        createApplicationInput("basicCriteria", "SSI_OR_RSDI", "false"),
        createApplicationInput("basicCriteria", "HAVE_DISABILITY_SSA", "false"),
        createApplicationInput("basicCriteria", "HAVE_DISABILITY_SMRT", "false"),
        createApplicationInput("basicCriteria", "MEDICAL_ASSISTANCE", "false"),
        createApplicationInput("basicCriteria", "HELP_WITH_MEDICARE", "false"),

        createApplicationInput("basicCriteria", "blindOrHasDisability", "true"),
        createApplicationInput("basicCriteria", "disabilityDetermination", "false")
    );
  }

  @Test
  public void testNotBlindAndDoesntHaveDisability() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of(Program.CERTAIN_POPS))
        .withPageData("basicCriteria", "basicCriteria", List.of("SIXTY_FIVE_OR_OLDER"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("basicCriteria", "SIXTY_FIVE_OR_OLDER", "true"),
        createApplicationInput("basicCriteria", "BLIND", "false"),
        createApplicationInput("basicCriteria", "SSI_OR_RSDI", "false"),
        createApplicationInput("basicCriteria", "HAVE_DISABILITY_SSA", "false"),
        createApplicationInput("basicCriteria", "HAVE_DISABILITY_SMRT", "false"),
        createApplicationInput("basicCriteria", "MEDICAL_ASSISTANCE", "false"),
        createApplicationInput("basicCriteria", "HELP_WITH_MEDICARE", "false"),

        createApplicationInput("basicCriteria", "blindOrHasDisability", "false"),
        createApplicationInput("basicCriteria", "disabilityDetermination", "false")
    );
  }

  @Test
  public void testDeterminedDisability() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of(Program.CERTAIN_POPS))
        .withPageData("basicCriteria", "basicCriteria", List.of("SSI_OR_RSDI"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("basicCriteria", "SIXTY_FIVE_OR_OLDER", "false"),
        createApplicationInput("basicCriteria", "BLIND", "false"),
        createApplicationInput("basicCriteria", "SSI_OR_RSDI", "true"),
        createApplicationInput("basicCriteria", "HAVE_DISABILITY_SSA", "false"),
        createApplicationInput("basicCriteria", "HAVE_DISABILITY_SMRT", "false"),
        createApplicationInput("basicCriteria", "MEDICAL_ASSISTANCE", "false"),
        createApplicationInput("basicCriteria", "HELP_WITH_MEDICARE", "false"),

        createApplicationInput("basicCriteria", "blindOrHasDisability", "true"),
        createApplicationInput("basicCriteria", "disabilityDetermination", "true")
    );
  }

  @Test
  public void testDeterminedNoDisability() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of(Program.CERTAIN_POPS))
        .withPageData("basicCriteria", "basicCriteria", List.of("MEDICAL_ASSISTANCE"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        createApplicationInput("basicCriteria", "SIXTY_FIVE_OR_OLDER", "false"),
        createApplicationInput("basicCriteria", "BLIND", "false"),
        createApplicationInput("basicCriteria", "SSI_OR_RSDI", "false"),
        createApplicationInput("basicCriteria", "HAVE_DISABILITY_SSA", "false"),
        createApplicationInput("basicCriteria", "HAVE_DISABILITY_SMRT", "false"),
        createApplicationInput("basicCriteria", "MEDICAL_ASSISTANCE", "true"),
        createApplicationInput("basicCriteria", "HELP_WITH_MEDICARE", "false"),

        createApplicationInput("basicCriteria", "blindOrHasDisability", "false"),
        createApplicationInput("basicCriteria", "disabilityDetermination", "false")
    );
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
