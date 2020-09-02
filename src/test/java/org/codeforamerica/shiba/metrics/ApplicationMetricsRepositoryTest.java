package org.codeforamerica.shiba.metrics;

import org.codeforamerica.shiba.County;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Sql(statements = "TRUNCATE TABLE application_metrics;")
class ApplicationMetricsRepositoryTest {

    @Autowired
    ApplicationMetricsRepository applicationMetricsRepository;

    County defaultCounty = County.OTHER;

    ZonedDateTime defaultCompletedAt = ZonedDateTime.now(ZoneOffset.UTC);

    Duration defaultDuration = Duration.ofMinutes(14);

    @MockBean
    Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    void shouldSaveApplicationMetrics() {
        ApplicationMetric applicationMetric = new ApplicationMetric(Duration.ofDays(1), OLMSTED, ZonedDateTime.now(ZoneOffset.UTC));
        applicationMetricsRepository.save(applicationMetric);

        assertThat(applicationMetricsRepository.findAll()).contains(applicationMetric);
    }

    @Test
    void shouldCalculateMedianTimeToComplete() {
        ApplicationMetric applicationMetric1 = new ApplicationMetric(Duration.ofDays(1), defaultCounty, defaultCompletedAt);
        ApplicationMetric applicationMetric2 = new ApplicationMetric(Duration.ofDays(2), defaultCounty, defaultCompletedAt);
        ApplicationMetric applicationMetric3 = new ApplicationMetric(Duration.ofDays(3), defaultCounty, defaultCompletedAt);
        ApplicationMetric applicationMetric4 = new ApplicationMetric(Duration.ofDays(4), defaultCounty, defaultCompletedAt);

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
        ApplicationMetric applicationMetric1 = new ApplicationMetric(defaultDuration, defaultCounty, defaultCompletedAt);
        ApplicationMetric applicationMetric2 = new ApplicationMetric(defaultDuration, defaultCounty, defaultCompletedAt);
        ApplicationMetric applicationMetric3 = new ApplicationMetric(defaultDuration, defaultCounty, defaultCompletedAt);
        ApplicationMetric applicationMetric4 = new ApplicationMetric(defaultDuration, defaultCounty, defaultCompletedAt);

        applicationMetricsRepository.save(applicationMetric1);
        applicationMetricsRepository.save(applicationMetric2);
        applicationMetricsRepository.save(applicationMetric3);
        applicationMetricsRepository.save(applicationMetric4);

        assertThat(applicationMetricsRepository.count()).isEqualTo(4);
    }

    @Test
    void shouldGetCountByCounty() {
        ApplicationMetric applicationMetric1 = new ApplicationMetric(defaultDuration, OLMSTED, defaultCompletedAt);
        ApplicationMetric applicationMetric2 = new ApplicationMetric(defaultDuration, defaultCounty, defaultCompletedAt);
        ApplicationMetric applicationMetric3 = new ApplicationMetric(defaultDuration, HENNEPIN, defaultCompletedAt);
        ApplicationMetric applicationMetric4 = new ApplicationMetric(defaultDuration, HENNEPIN, defaultCompletedAt);

        applicationMetricsRepository.save(applicationMetric1);
        applicationMetricsRepository.save(applicationMetric2);
        applicationMetricsRepository.save(applicationMetric3);
        applicationMetricsRepository.save(applicationMetric4);

        assertThat(applicationMetricsRepository.countByCounty()).isEqualTo(
                Map.of(
                        HENNEPIN, 2,
                        OLMSTED, 1,
                        OTHER, 1
                )
        );
    }

    @Test
    void shouldGetCountByCountyForWeekToDateInSpecifiedTimezone() {
        /*Calendar for reference
        S   M   T   W  TH  F  S
        29  30  31  1  2   3  4
        Chicago is in -06:00
        * */
        when(clock.instant()).thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        ApplicationMetric applicationMetric1 = new ApplicationMetric(defaultDuration, OLMSTED, ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")));
        ApplicationMetric applicationMetric2 = new ApplicationMetric(defaultDuration, OLMSTED, ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")));
        ApplicationMetric applicationMetric3 = new ApplicationMetric(defaultDuration, HENNEPIN, ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")));

        applicationMetricsRepository.save(applicationMetric1);
        applicationMetricsRepository.save(applicationMetric2);
        applicationMetricsRepository.save(applicationMetric3);

        assertThat(applicationMetricsRepository.countByCountyWeekToDate(ZoneId.of("America/Chicago"))).isEqualTo(
                Map.of(
                        HENNEPIN, 1,
                        OLMSTED, 1
                )
        );
    }

    @Test
    void shouldGetAverageTimeToCompleteForWeekToDateInSpecifiedTimezone() {
        when(clock.instant()).thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        ApplicationMetric applicationMetric1 = new ApplicationMetric(Duration.ofDays(1), defaultCounty, ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")));
        ApplicationMetric applicationMetric2 = new ApplicationMetric(Duration.ofDays(2), defaultCounty, ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")));
        ApplicationMetric applicationMetric3 = new ApplicationMetric(Duration.ofDays(3), defaultCounty, ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")));

        applicationMetricsRepository.save(applicationMetric1);
        applicationMetricsRepository.save(applicationMetric2);
        applicationMetricsRepository.save(applicationMetric3);

        assertThat(applicationMetricsRepository.getAverageTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
                .isEqualTo(Duration.ofDays(2).plusHours(12));
    }

    @Test
    void shouldGetAverageTimeToCompleteForWeekToDateInSpecifiedTimezone_whenNoApplicationIsFound() {
        assertThat(applicationMetricsRepository.getAverageTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
                .isEqualTo(Duration.ofSeconds(0));
    }

    @Test
    void shouldGetMedianForWeekToDate() {
        when(clock.instant()).thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
        ApplicationMetric applicationMetric1 = new ApplicationMetric(Duration.ofDays(1), defaultCounty, ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")));
        ApplicationMetric applicationMetric2 = new ApplicationMetric(Duration.ofDays(2), defaultCounty, ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")));
        ApplicationMetric applicationMetric3 = new ApplicationMetric(Duration.ofDays(4), defaultCounty, ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")));
        ApplicationMetric applicationMetric4 = new ApplicationMetric(Duration.ofDays(10), defaultCounty, ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")));
        ApplicationMetric applicationMetric5 = new ApplicationMetric(Duration.ofDays(20), defaultCounty, ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")));

        applicationMetricsRepository.save(applicationMetric1);
        applicationMetricsRepository.save(applicationMetric2);
        applicationMetricsRepository.save(applicationMetric3);
        applicationMetricsRepository.save(applicationMetric4);
        applicationMetricsRepository.save(applicationMetric5);

        assertThat(applicationMetricsRepository.getMedianTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
                .isEqualTo(Duration.ofDays(7));
    }

    @Test
    void shouldGetMedianForWeekToDate_whenNoApplicationIsFound() {
        assertThat(applicationMetricsRepository.getMedianTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
                .isEqualTo(Duration.ofSeconds(0));
    }
}