package org.codeforamerica.shiba.journeys;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.codeforamerica.shiba.Program.CCAP;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.NO;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.YES;
import static org.springframework.test.web.servlet.result.StatusResultMatchersExtensionsKt.isEqualTo;

import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("journey")
public class NextStepsJourneyTest extends JourneyTest {

  @Test
  void seeNextStepsWhenApplicationIsExpeditedSnap() {
    // Landing pages
    testPage.clickButton("Apply now");

    // Language Preferences
    testPage.enter("writtenLanguage", "English");
    testPage.enter("spokenLanguage", "English");
    testPage.enter("needInterpreter", "Yes");
    testPage.clickContinue();

    // Program Selection
    testPage.enter("programs", List.of(PROGRAM_SNAP, PROGRAM_CCAP));
    testPage.clickContinue();

    // Expedited CCAP
    testPage.enter("expeditedCcap", NO.getDisplayValue());

    // Expedited SNAP
    testPage.enter("expeditedSnap", YES.getDisplayValue());

    // Next Steps
    assertThat(testPage.getTitle()).isEqualTo("Your next steps");
  }
}
