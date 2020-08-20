package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class GrossMonthlyIncomeMapperTest {
    private final ApplicationData applicationData = new ApplicationData();

    @Autowired
    private GrossMonthlyIncomeMapper grossMonthlyIncomeMapper;

    @TestConfiguration
    @PropertySource(value = "classpath:test-gross-monthly-income-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-gross-monthly-income")
        public GrossMonthlyIncomeConfiguration grossMonthlyIncomeConfiguration() {
            return new GrossMonthlyIncomeConfiguration();
        }
    }

    @Test
    void shouldCalculateGrossMonthlyIncomeForEachHourlyJob() {
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

        List<ApplicationInput> applicationInputs = grossMonthlyIncomeMapper.map(applicationData);

        assertThat(applicationInputs).contains(
                new ApplicationInput("employee", "grossMonthlyIncome", List.of("1440.0"), SINGLE_VALUE, 0),
                new ApplicationInput("employee", "grossMonthlyIncome", List.of("1080.0"), SINGLE_VALUE, 1)
        );
    }

    @Test
    void shouldNotIncludeGrossMonthlyIncomeForHourlyJobWithInsufficientInformationForCalculation() {
        Subworkflows subworkflows = new Subworkflows();
        Subworkflow subworkflow = new Subworkflow();
        PagesData pagesData = new PagesData();
        PageData paidByTheHourPageData = new PageData();
        paidByTheHourPageData.put("paidByTheHourInput", InputData.builder().value(List.of("true")).build());
        pagesData.put("paidByTheHourPage", paidByTheHourPageData);
        PageData hourlyWagePageData1 = new PageData();
        hourlyWagePageData1.put("hourlyWageInput", InputData.builder().value(List.of("")).build());
        pagesData.put("hourlyWagePage", hourlyWagePageData1);
        PageData hoursAWeekPageData1 = new PageData();
        hoursAWeekPageData1.put("hoursAWeekInput", InputData.builder().value(List.of("")).build());
        pagesData.put("hoursAWeekPage", hoursAWeekPageData1);
        subworkflow.add(pagesData);
        subworkflows.put("jobsGroup", subworkflow);
        applicationData.setSubworkflows(subworkflows);

        List<ApplicationInput> applicationInputs = grossMonthlyIncomeMapper.map(applicationData);

        assertThat(applicationInputs).isEmpty();
    }

    @Test
    void shouldNotIncludeGrossMonthlyIncomeWhenJobsInformationIsNotAvailable() {
        List<ApplicationInput> applicationInputs = grossMonthlyIncomeMapper.map(applicationData);

        assertThat(applicationInputs).isEmpty();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "EVERY_WEEK,4.4",
            "EVERY_TWO_WEEKS,2.2",
            "TWICE_A_MONTH,2.2",
            "EVERY_MONTH,1.1",
            "IT_VARIES,1.1"
    })
    void shouldCalculateGrossIncomeBasedOnPayPeriod(String payPeriod, String income) {
        Subworkflows subworkflows = new Subworkflows();
        Subworkflow subworkflow = new Subworkflow();
        PagesData pagesData = new PagesData();

        PageData paidByTheHourPageData = new PageData();
        paidByTheHourPageData.put("paidByTheHourInput", InputData.builder().value(List.of("false")).build());
        pagesData.put("paidByTheHourPage", paidByTheHourPageData);

        PageData payPeriodPageData = new PageData();
        payPeriodPageData.put("payPeriodInput", InputData.builder().value(List.of(payPeriod)).build());
        pagesData.put("payPeriodPage", payPeriodPageData);

        PageData incomePerPayPeriod = new PageData();
        incomePerPayPeriod.put("incomePerPayPeriodInput", InputData.builder().value(List.of("1.1")).build());
        pagesData.put("incomePerPayPeriodPage", incomePerPayPeriod);

        subworkflow.add(pagesData);
        subworkflows.put("jobsGroup", subworkflow);
        applicationData.setSubworkflows(subworkflows);

        List<ApplicationInput> applicationInputs = grossMonthlyIncomeMapper.map(applicationData);

        assertThat(applicationInputs).contains(
                new ApplicationInput("employee", "grossMonthlyIncome", List.of(income), SINGLE_VALUE, 0)
        );
    }
}