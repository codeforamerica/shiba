package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.codeforamerica.shiba.output.caf.ExpeditedCcap;
import org.codeforamerica.shiba.output.caf.ExpeditedSnap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class NextStepsContentServiceTest {
  private static final String CCAP = "CCAP";
  private static final String SNAP = "SNAP";
  private static final String GRH = "GRH";
  private static final String CASH = "CASH";
  private static final String EA = "EA";

  private static Stream<Arguments> nextStepMessageTestCases() {
    return Stream.of(
        Arguments.of(
            "Example 1 (Only Expedited SNAP)",
            List.of(SNAP),
            ExpeditedSnap.ELIGIBLE,
            ExpeditedCcap.NOT_ELIGIBLE,
            List.of(
                "Within 24 hours, expect a call from your county about your food assistance application.",
                "If you don't hear from your county within 3 days or want an update on your case, please call your county."
            )
        )
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("org.codeforamerica.shiba.pages.NextStepsContentServiceTest#nextStepMessageTestCases")
  void displaysCorrectSuccessMessageForApplicantPrograms(String testName, List<String> programs,
      ExpeditedSnap expeditedSnap,
      ExpeditedCcap expeditedCcap, List<String> expectedMessages) {
    List<String> nextStepSections = new NextStepsContentService().getNextSteps(programs,
        expeditedSnap, expeditedCcap);

    assertThat(nextStepSections).containsExactly(expectedMessages.toArray(new String[0]));
  }
}
