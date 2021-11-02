package org.codeforamerica.shiba.application.parsers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.Test;

class EmailParserTest {

  @Test
  void shouldParseEmail() {
    String email = "email@address";
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("contactInfo", "email", email).build();

    Optional<String> parsedEmail = EmailParser.parse(applicationData);

    assertThat(parsedEmail.get()).isEqualTo(email);
  }

  @Test
  void shouldParseToEmptyResult_whenEmailIsEmpty() {
    String email = "";
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("contactInfo", "email", email).build();

    Optional<String> parsedEmail = EmailParser.parse(applicationData);

    assertThat(parsedEmail).isEmpty();
  }
}
