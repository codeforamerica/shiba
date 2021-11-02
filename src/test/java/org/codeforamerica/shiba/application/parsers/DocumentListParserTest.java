package org.codeforamerica.shiba.application.parsers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;

import java.util.List;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class DocumentListParserTest {

  @Test
  void parseShouldIncludeCCAPWhenProgramsContainCCAP() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withApplicantPrograms(List.of("CCAP", "SNAP"))
        .build();

    assertThat(DocumentListParser.parse(applicationData)).containsExactlyInAnyOrder(CAF, CCAP);
  }

  @Test
  void parseShouldIncludeCCAPAndCAFWhenHouseholdMembersProgramsIncludeCCAPAndOtherPrograms() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHouseholdMemberPrograms(List.of("SNAP", "CASH", "CCAP"))
        .build();

    assertThat(DocumentListParser.parse(applicationData)).containsExactlyInAnyOrder(CAF, CCAP);
  }

  @Test
  void parseShouldIncludeOnlyCCAPIfCCAPIsOnlyProgramSelected() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHouseholdMemberPrograms(List.of("CCAP"))
        .build();

    assertThat(DocumentListParser.parse(applicationData)).containsExactlyInAnyOrder(CCAP);

  }

  @Test
  void parseShouldOnlyIncludeCAFIfCCAPIsNotASelectedProgram() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withHouseholdMemberPrograms(List.of("SNAP"))
        .build();

    assertThat(DocumentListParser.parse(applicationData)).containsExactlyInAnyOrder(CAF);
  }
}
