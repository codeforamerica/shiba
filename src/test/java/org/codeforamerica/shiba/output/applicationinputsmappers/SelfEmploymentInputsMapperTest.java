package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.Test;

public class SelfEmploymentInputsMapperTest {

  private final SelfEmploymentInputsMapper selfEmploymentInputsMapper = new SelfEmploymentInputsMapper();

  @Test
  void shouldMapTrueIfSelfEmployedJobExists() {
    PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();
    ApplicationData applicationData = new ApplicationData();
    applicationData.setSubworkflows(
        new Subworkflows(Map.of("jobs", new Subworkflow(List.of(
            pagesDataBuilder.build(List.of(
                new PageDataBuilder("selfEmployment", Map.of("selfEmployment", List.of("false")))
            )),
            pagesDataBuilder.build(List.of(
                new PageDataBuilder("selfEmployment", Map.of("selfEmployment", List.of("true")))
            ))
        )))));

    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentInputsMapper
        .map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker()))
        .containsExactlyInAnyOrder(
            new ApplicationInput(
                "employee",
                "selfEmployed",
                List.of("true"),
                ApplicationInputType.SINGLE_VALUE
            ),
            new ApplicationInput(
                "employee",
                "selfEmployedGrossMonthlyEarnings",
                List.of("see question 9"),
                ApplicationInputType.SINGLE_VALUE
            )
        );
  }

  @Test
  void shouldMapFalseIfSelfEmployedJobDoesntExists() {
    PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();
    ApplicationData applicationData = new ApplicationData();
    applicationData.setSubworkflows(
        new Subworkflows(Map.of("jobs", new Subworkflow(List.of(
            pagesDataBuilder.build(List.of(
                new PageDataBuilder("selfEmployment", Map.of("selfEmployment", List.of("false")))
            )),
            pagesDataBuilder.build(List.of(
                new PageDataBuilder("selfEmployment", Map.of("selfEmployment", List.of("false")))
            ))
        )))));

    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentInputsMapper
        .map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker()))
        .containsExactlyInAnyOrder(
            new ApplicationInput(
                "employee",
                "selfEmployed",
                List.of("false"),
                ApplicationInputType.SINGLE_VALUE
            ),
            new ApplicationInput(
                "employee",
                "selfEmployedGrossMonthlyEarnings",
                List.of(""),
                ApplicationInputType.SINGLE_VALUE
            )
        );
  }


  @Test
  void shouldMapEmptyIfNoJobs() {
    ApplicationData applicationData = new ApplicationData();
    Application application = Application.builder().applicationData(applicationData).build();

    assertThat(selfEmploymentInputsMapper
        .map(application, null, Recipient.CLIENT, new SubworkflowIterationScopeTracker()))
        .isEmpty();
  }

}
