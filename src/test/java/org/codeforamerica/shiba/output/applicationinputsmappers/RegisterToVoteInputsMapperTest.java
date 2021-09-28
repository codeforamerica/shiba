package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

public class RegisterToVoteInputsMapperTest {

  private final RegisterToVoteInputsMapper mapper = new RegisterToVoteInputsMapper();

  @Test
  public void shouldMapNoToFalse() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("registerToVote", "registerToVote",
            List.of("NO"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "registerToVote",
            "registerToVoteSelection",
            List.of("false"),
            ApplicationInputType.ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapNoAlreadyRegisteredToFalse() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("registerToVote", "registerToVote",
            List.of("NO_ALREADY_REGISTERED"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "registerToVote",
            "registerToVoteSelection",
            List.of("false"),
            ApplicationInputType.ENUMERATED_SINGLE_VALUE
        ));
  }

  @Test
  public void shouldMapYesToTrue() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("registerToVote", "registerToVote",
            List.of("YES"))
        .build();

    List<ApplicationInput> result = mapper.map(Application.builder()
        .applicationData(applicationData)
        .build(), null, null, null);

    assertThat(result).containsOnly(
        new ApplicationInput(
            "registerToVote",
            "registerToVoteSelection",
            List.of("true"),
            ApplicationInputType.ENUMERATED_SINGLE_VALUE
        ));
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
