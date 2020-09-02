package org.codeforamerica.shiba.metrics;

import org.codeforamerica.shiba.County;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class ApplicationMetricsRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final Clock clock;

    public ApplicationMetricsRepository(JdbcTemplate jdbcTemplate,
                                        Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("application_metrics");
        this.clock = clock;
    }

    public void save(ApplicationMetric applicationMetric) {
        jdbcInsert.execute(Map.of(
                "time_to_complete", applicationMetric.getTimeToComplete().getSeconds(),
                "county", applicationMetric.getCounty().name(),
                "completed_at", Timestamp.from(applicationMetric.getCompletedAt().toInstant())));
    }

    public Duration getMedianTimeToComplete() {
        Long medianTimeToComplete = jdbcTemplate.queryForObject("SELECT COALESCE(percentile_cont(0.5) WITHIN GROUP (ORDER BY time_to_complete), 0) FROM application_metrics", Long.class);
        return Duration.ofSeconds(Objects.requireNonNull(medianTimeToComplete));
    }

    public Integer count() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM application_metrics;", Integer.class);
    }

    public List<ApplicationMetric> findAll() {
        return jdbcTemplate.query("SELECT * FROM application_metrics;", (resultSet, rowNumber) ->
                new ApplicationMetric(Duration.of(resultSet.getLong("time_to_complete"), ChronoUnit.SECONDS),
                        County.valueOf(resultSet.getString("county")),
                        ZonedDateTime.ofInstant(resultSet.getTimestamp("completed_at").toInstant(), ZoneOffset.UTC)));
    }

    public Map<County, Integer> countByCounty() {
        return jdbcTemplate.query(
                "SELECT county, count(*) AS count " +
                        "FROM application_metrics " +
                        "GROUP BY county", (resultSet, rowNumber) ->
                        Map.entry(
                                County.valueOf(resultSet.getString("county")),
                                resultSet.getInt("count"))).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<County, Integer> countByCountyWeekToDate(ZoneId zoneId) {
        ZonedDateTime lowerBound = getBeginningOfWeekForTimeZone(zoneId);
        return jdbcTemplate.query(
                "SELECT county, count(*) AS count " +
                        "FROM application_metrics " +
                        "WHERE completed_at >= ? " +
                        "GROUP BY county",
                new Object[]{Timestamp.from(lowerBound.toInstant())},
                (resultSet, rowNumber) ->
                        Map.entry(
                                County.valueOf(resultSet.getString("county")),
                                resultSet.getInt("count"))).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Duration getAverageTimeToCompleteWeekToDate(ZoneId zoneId) {
        ZonedDateTime lowerBound = getBeginningOfWeekForTimeZone(zoneId);
        Double averageTimeToComplete = jdbcTemplate.queryForObject(
                "SELECT COALESCE(AVG(time_to_complete), 0) as averageTime " +
                        "FROM application_metrics " +
                        "WHERE completed_at >= ?",
                new Object[]{Timestamp.from(lowerBound.toInstant())},
                Double.class
        );

        return Duration.ofSeconds(Objects.requireNonNull(averageTimeToComplete).longValue());
    }

    public Duration getMedianTimeToCompleteWeekToDate(ZoneId zoneId) {
        ZonedDateTime lowerBound = getBeginningOfWeekForTimeZone(zoneId);
        Long medianTimeToComplete = jdbcTemplate.queryForObject(
                "SELECT COALESCE(percentile_cont(0.5) WITHIN GROUP (ORDER BY time_to_complete), 0) " +
                        "FROM application_metrics " +
                        "WHERE completed_at >= ?",
                new Object[]{Timestamp.from(lowerBound.toInstant())},
                Long.class);
        return Duration.ofSeconds(Objects.requireNonNull(medianTimeToComplete));
    }

    @NotNull
    private ZonedDateTime getBeginningOfWeekForTimeZone(ZoneId zoneId) {
        LocalDate localDate = LocalDate.ofInstant(clock.instant(), zoneId);
        LocalDate beginningOfWeek = localDate.minusDays(localDate.getDayOfWeek().getValue());
        return beginningOfWeek.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
    }
}
