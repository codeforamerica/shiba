package org.codeforamerica.shiba.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Sql(statements = "TRUNCATE TABLE application_metrics;")
class ApplicationMetricsRepositoryTest {

    @Autowired
    ApplicationMetricsRepository applicationMetricsRepository;

    @Test
    void shouldSaveApplicationMetrics() {
        ApplicationMetric applicationMetric = new ApplicationMetric(Duration.ofDays(1));
        applicationMetricsRepository.save(applicationMetric);

        assertThat(applicationMetricsRepository.findAll()).contains(applicationMetric);
    }

    @Test
    void shouldCalculateMedianTimeToComplete() {
        ApplicationMetric applicationMetric1 = new ApplicationMetric(Duration.ofDays(1));
        ApplicationMetric applicationMetric2 = new ApplicationMetric(Duration.ofDays(2));
        ApplicationMetric applicationMetric3 = new ApplicationMetric(Duration.ofDays(3));
        ApplicationMetric applicationMetric4 = new ApplicationMetric(Duration.ofDays(4));

        applicationMetricsRepository.save(applicationMetric1);
        applicationMetricsRepository.save(applicationMetric2);
        applicationMetricsRepository.save(applicationMetric3);
        applicationMetricsRepository.save(applicationMetric4);

        assertThat(applicationMetricsRepository.getMedianTimeToComplete()).isEqualTo(Duration.ofDays(2).plusHours(12));
    }

    @Test
    void shouldReturn0ForMedianTimeToCompleteWhenThereIsNoEntries() {
        assertThat(applicationMetricsRepository.getMedianTimeToComplete()).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldGetCount() {
        ApplicationMetric applicationMetric1 = new ApplicationMetric(Duration.ofDays(1));
        ApplicationMetric applicationMetric2 = new ApplicationMetric(Duration.ofDays(2));
        ApplicationMetric applicationMetric3 = new ApplicationMetric(Duration.ofDays(3));
        ApplicationMetric applicationMetric4 = new ApplicationMetric(Duration.ofDays(4));

        applicationMetricsRepository.save(applicationMetric1);
        applicationMetricsRepository.save(applicationMetric2);
        applicationMetricsRepository.save(applicationMetric3);
        applicationMetricsRepository.save(applicationMetric4);

        assertThat(applicationMetricsRepository.count()).isEqualTo(4);
    }
}