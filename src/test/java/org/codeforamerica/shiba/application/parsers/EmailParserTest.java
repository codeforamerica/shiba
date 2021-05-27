package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EmailParserTest {

    ApplicationData applicationData = new ApplicationData();
    PagesData pagesData = new PagesData();
    PageData contactInfo = new PageData();

    @Test
    void shouldParseEmail() {
        String email = "email@address";
        contactInfo.put("email", InputData.builder().value(List.of(email)).build());
        pagesData.put("contactInfo", contactInfo);
        applicationData.setPagesData(pagesData);

        Optional<String> parsedEmail = EmailParser.parse(applicationData);

        assertThat(parsedEmail.get()).isEqualTo(email);
    }

    @Test
    void shouldParseToEmptyResult_whenEmailIsEmpty() {
        String email = "";
        contactInfo.put("email", InputData.builder().value(List.of(email)).build());
        pagesData.put("contactInfo", contactInfo);
        applicationData.setPagesData(pagesData);

        Optional<String> parsedEmail = EmailParser.parse(applicationData);

        assertThat(parsedEmail).isEmpty();
    }
}