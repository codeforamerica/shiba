package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class EmailParserTest extends AbstractParserTest {
    EmailParser emailParser;

    ApplicationData applicationData = new ApplicationData();
    PagesData pagesData = new PagesData();
    PageData contactInfo = new PageData();

    @BeforeEach
    void setUp() {
        emailParser = new EmailParser(parsingConfiguration);
    }

    @Test
    void shouldParseEmail() {
        String email = "email@address";
        contactInfo.put("contactEmail", InputData.builder().value(List.of(email)).build());
        pagesData.put("contactInfoPageName", contactInfo);
        applicationData.setPagesData(pagesData);

        Optional<String> parsedEmail = emailParser.parse(applicationData);

        assertThat(parsedEmail.get()).isEqualTo(email);
    }

    @Test
    void shouldParseToEmptyResult_whenEmailIsEmpty() {
        String email = "";
        contactInfo.put("contactEmail", InputData.builder().value(List.of(email)).build());
        pagesData.put("contactInfoPageName", contactInfo);
        applicationData.setPagesData(pagesData);

        Optional<String> parsedEmail = emailParser.parse(applicationData);

        assertThat(parsedEmail).isEmpty();
    }
}