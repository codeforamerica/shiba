package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.application.parsers.GrossMonthlyIncomeParser;
import org.codeforamerica.shiba.application.parsers.ParsingConfiguration;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
class GrossMonthlyIncomeParserTest {
    private final ApplicationData applicationData = new ApplicationData();

    @Autowired
    private GrossMonthlyIncomeParser grossMonthlyIncomeParser;

    private final PagesDataBuilder pagesDataBuilder = new PagesDataBuilder();

    @TestConfiguration
    @PropertySource(value = "classpath:test-parsing-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @SuppressWarnings("ConfigurationProperties")
        @Bean
        @ConfigurationProperties(prefix = "test-parsing")
        public ParsingConfiguration parsingConfiguration() {
            return new ParsingConfiguration();
        }
    }

    @Test
    void shouldProvideHourlyJobInformation() {
        Subworkflows subworkflows = new Subworkflows();

        Subworkflow subworkflow = new Subworkflow();
        PagesData pagesData1 = new PagesData();
        PageData paidByTheHourPageData1 = new PageData();
        paidByTheHourPageData1.put("paidByTheHourInput", InputData.builder().value(List.of("true")).build());
        pagesData1.put("paidByTheHourPage", paidByTheHourPageData1);
        PageData hourlyWagePageData1 = new PageData();
        hourlyWagePageData1.put("hourlyWageInput", InputData.builder().value(List.of("12")).build());
        pagesData1.put("hourlyWagePage", hourlyWagePageData1);
        PageData hoursAWeekPageData1 = new PageData();
        hoursAWeekPageData1.put("hoursAWeekInput", InputData.builder().value(List.of("30")).build());
        pagesData1.put("hoursAWeekPage", hoursAWeekPageData1);
        subworkflow.add(pagesData1);

        PagesData pagesData2 = new PagesData();
        PageData paidByTheHourPageData2 = new PageData();
        paidByTheHourPageData2.put("paidByTheHourInput", InputData.builder().value(List.of("true")).build());
        pagesData2.put("paidByTheHourPage", paidByTheHourPageData2);
        PageData hourlyWagePageData2 = new PageData();
        hourlyWagePageData2.put("hourlyWageInput", InputData.builder().value(List.of("6")).build());
        pagesData2.put("hourlyWagePage", hourlyWagePageData2);
        PageData hoursAWeekPageData2 = new PageData();
        hoursAWeekPageData2.put("hoursAWeekInput", InputData.builder().value(List.of("45")).build());
        pagesData2.put("hoursAWeekPage", hoursAWeekPageData2);
        subworkflow.add(pagesData2);
        subworkflows.put("jobsGroup", subworkflow);

        applicationData.setSubworkflows(subworkflows);

        List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser.parse(applicationData);

        assertThat(jobIncomeInformation).contains(
                new HourlyJobIncomeInformation("12", "30", 0, subworkflow.get(0)),
                new HourlyJobIncomeInformation("6", "45", 1, subworkflow.get(1))
        );
    }

    @Test
    void shouldNotProvideJobInformationForJobsWithInsufficientInformationForCalculation() {
        PagesData hourlyJobPagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("paidByTheHourPage", Map.of("paidByTheHourInput", List.of("true"))),
                new PageDataBuilder("hourlyWagePage", Map.of("hourlyWageInput", List.of(""))),
                new PageDataBuilder("hoursAWeekPage", Map.of("hoursAWeekInput", List.of("")))
        ));

        PagesData nonHourlyJobPagesData = pagesDataBuilder.build(List.of(
                new PageDataBuilder("paidByTheHourPage", Map.of("paidByTheHourInput", List.of("false"))),
                new PageDataBuilder("payPeriodPage", Map.of("payPeriodInput", List.of(""))),
                new PageDataBuilder("incomePerPayPeriodPage", Map.of("incomePerPayPeriodInput", List.of(""))),
                new PageDataBuilder("last30DaysJobIncomePage", Map.of("last30DaysJobIncomeInput", List.of("")))
        ));

        Subworkflow subworkflow = new Subworkflow();
        Subworkflows subworkflows = new Subworkflows();
        subworkflow.add(hourlyJobPagesData);
        subworkflow.add(nonHourlyJobPagesData);
        subworkflows.put("jobsGroup", subworkflow);
        applicationData.setSubworkflows(subworkflows);

        List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser.parse(applicationData);

        assertThat(jobIncomeInformation).isEmpty();
    }

    @Test
    void shouldNotIncludeGrossMonthlyIncomeWhenJobsInformationIsNotAvailable() {
        List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser.parse(applicationData);

        assertThat(jobIncomeInformation).isEmpty();
    }

    @Test
    void shouldProvideNonHourlyJobInformation() {
        Subworkflows subworkflows = new Subworkflows();
        Subworkflow subworkflow = new Subworkflow();
        PagesData pagesData = new PagesData();

        PageData paidByTheHourPageData = new PageData();
        paidByTheHourPageData.put("paidByTheHourInput", InputData.builder().value(List.of("false")).build());
        pagesData.put("paidByTheHourPage", paidByTheHourPageData);

        PageData payPeriodPageData = new PageData();
        payPeriodPageData.put("payPeriodInput", InputData.builder().value(List.of("EVERY_WEEK")).build());
        pagesData.put("payPeriodPage", payPeriodPageData);

        PageData incomePerPayPeriod = new PageData();
        incomePerPayPeriod.put("incomePerPayPeriodInput", InputData.builder().value(List.of("1.1")).build());
        pagesData.put("incomePerPayPeriodPage", incomePerPayPeriod);

        subworkflow.add(pagesData);
        subworkflows.put("jobsGroup", subworkflow);
        applicationData.setSubworkflows(subworkflows);

        List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser.parse(applicationData);

        assertThat(jobIncomeInformation).contains(
                new NonHourlyJobIncomeInformation("EVERY_WEEK", "1.1", 0, subworkflow.get(0))
        );
    }

    @Test
    void shouldReturnNonHourlyJobInformationIfHourlyJobPageIsNotAvailable() {
        Subworkflow subworkflow = new Subworkflow();
        subworkflow.add(pagesDataBuilder.build(List.of(
            new PageDataBuilder("payPeriodPage", Map.of("payPeriodInput", List.of("EVERY_WEEK"))),
            new PageDataBuilder("incomePerPayPeriodPage", Map.of("incomePerPayPeriodInput", List.of("1.1")))
        )));
        applicationData.setSubworkflows(new Subworkflows(Map.of("jobsGroup", subworkflow)));

        List<JobIncomeInformation> jobIncomeInformation = grossMonthlyIncomeParser.parse(applicationData);

        assertThat(jobIncomeInformation).contains(
                new NonHourlyJobIncomeInformation("EVERY_WEEK", "1.1", 0, subworkflow.get(0))
        );
    }
}