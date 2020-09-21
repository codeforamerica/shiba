package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.TotalIncomeParser;
import org.codeforamerica.shiba.output.caf.JobIncomeInformation;
import org.codeforamerica.shiba.output.caf.ParsingConfiguration;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class TotalIncomeParserTest {
    private final ApplicationData applicationData = new ApplicationData();
    private final PagesData pagesData = new PagesData();
    private final Subworkflows subworkflows = new Subworkflows();

    @Autowired
    TotalIncomeParser totalIncomeParser;

    @MockBean
    ApplicationDataParser<List<JobIncomeInformation>> grossIncomeParser;

    @TestConfiguration
    @PropertySource(value = "classpath:test-parsing-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-parsing")
        public ParsingConfiguration parsingConfiguration() {
            return new ParsingConfiguration();
        }
    }

    @BeforeEach
    void setUp() {
        applicationData.setPagesData(pagesData);
        applicationData.setSubworkflows(subworkflows);
    }

    @Test
    void shouldParseTotalIncomeFromLastThirtyDaysAndJobIncomeInformation() {
        pagesData.putPage("incomePage", new PageData(Map.of("incomeInput", InputData.builder().value(List.of("1.0")).build())));
        JobIncomeInformation mockJobInfo1 = mock(JobIncomeInformation.class);
        JobIncomeInformation mockJobInfo2 = mock(JobIncomeInformation.class);
        List<JobIncomeInformation> jobInfo = List.of(mockJobInfo1, mockJobInfo2);
        when(grossIncomeParser.parse(applicationData)).thenReturn(jobInfo);

        TotalIncome totalIncome = totalIncomeParser.parse(applicationData);

        assertThat(totalIncome).isEqualTo(new TotalIncome(1.0, jobInfo));
    }
}