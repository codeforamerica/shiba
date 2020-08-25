package org.codeforamerica.shiba.metrics;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.OLMSTED;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Sql(statements = {"ALTER SEQUENCE application_id RESTART WITH 12", "TRUNCATE TABLE applications"})
class ApplicationRepositoryTest {

    @Autowired
    ApplicationRepository applicationRepository;

    @Test
    void shouldGenerateIdForNextApplication() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).endsWith("12");

        String nextIdAgain = applicationRepository.getNextId();

        assertThat(nextIdAgain).endsWith("13");
    }

    @Test
    void shouldPrefixIdWithRandom3DigitSalt() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).matches("^[1-9]\\d{2}.*");

        String nextIdAgain = applicationRepository.getNextId();

        assertThat(nextIdAgain.substring(0, 3)).isNotEqualTo(nextId.substring(0, 3));
    }

    @Test
    void shouldPadTheIdWithZeroesUntilReach10Digits() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).hasSize(10);
        assertThat(nextId.substring(3, 8)).isEqualTo("00000");
    }

    @Test
    void shouldSaveApplication() {
        ApplicationData applicationData = new ApplicationData();
        PageData pageData = new PageData();
        pageData.put("someInput", new InputData(List.of("someValue")));
        applicationData.setPagesData(new PagesData(Map.of("somePage", pageData)));
        Subworkflows subworkflows = new Subworkflows();
        PagesData subflowIteration = new PagesData();
        PageData groupedPage = new PageData();
        groupedPage.put("someGroupedPageInput", new InputData(List.of("someGroupedPageValue")));
        subflowIteration.put("someGroupedPage", groupedPage);
        subworkflows.addIteration("someGroup", subflowIteration);
        applicationData.setSubworkflows(subworkflows);

        Application application = new Application("someid", ZonedDateTime.now(ZoneOffset.UTC), applicationData, OLMSTED);

        applicationRepository.save(application);

        assertThat(applicationRepository.find("someid")).isEqualTo(application);
    }
}