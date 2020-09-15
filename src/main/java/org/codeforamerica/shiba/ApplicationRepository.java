package org.codeforamerica.shiba;

import org.codeforamerica.shiba.metrics.ApplicationMetric;
import org.codeforamerica.shiba.pages.data.ApplicationData;
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
import java.util.Random;
import java.util.stream.Collectors;

@Repository
public class ApplicationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Encryptor<ApplicationData> encryptor;
    private final ApplicationFactory applicationFactory;
    private final Clock clock;

    public ApplicationRepository(JdbcTemplate jdbcTemplate,
                                 Encryptor<ApplicationData> encryptor,
                                 ApplicationFactory applicationFactory,
                                 Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.encryptor = encryptor;
        this.applicationFactory = applicationFactory;
        this.clock = clock;
    }

    @SuppressWarnings("ConstantConditions")
    public String getNextId() {
        int random3DigitNumber = new Random().nextInt(900) + 100;

        String id = jdbcTemplate.queryForObject("SELECT nextval('application_id');", String.class);
        int numberOfZeros = 10 - id.length();
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(random3DigitNumber);
        while (idBuilder.length() < numberOfZeros) {
            idBuilder.append('0');
        }
        idBuilder.append(id);
        return idBuilder.toString();
    }

    public void save(Application application) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("applications");
        jdbcInsert.execute(Map.of(
                "id", application.getId(),
                "completed_at", Timestamp.from(application.getCompletedAt().toInstant()),
                "encrypted_data", encryptor.encrypt(application.getApplicationData()),
                "county", application.getCounty().name(),
                "time_to_complete", application.getTimeToComplete().getSeconds()
        ));
    }

    public Application find(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM applications WHERE id = ?",
                (resultSet, rowNum) -> applicationFactory.reconstitueApplication(
                        id,
                        ZonedDateTime.ofInstant(resultSet.getTimestamp("completed_at").toInstant(), ZoneOffset.UTC),
                        encryptor.decrypt(resultSet.getBytes("encrypted_data")),
                        County.valueOf(resultSet.getString("county")),
                        Duration.ofSeconds(resultSet.getLong("time_to_complete"))),
                id);
    }

    public Duration getMedianTimeToComplete() {
        Long medianTimeToComplete = jdbcTemplate.queryForObject("SELECT COALESCE(percentile_cont(0.5) WITHIN GROUP (ORDER BY time_to_complete), 0) FROM applications", Long.class);
        return Duration.ofSeconds(Objects.requireNonNull(medianTimeToComplete));
    }

    public Integer count() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM applications;", Integer.class);
    }

    public List<ApplicationMetric> findAll() {
        return jdbcTemplate.query("SELECT * FROM applications;", (resultSet, rowNumber) ->
                new ApplicationMetric(Duration.of(resultSet.getLong("time_to_complete"), ChronoUnit.SECONDS),
                        County.valueOf(resultSet.getString("county")),
                        ZonedDateTime.ofInstant(resultSet.getTimestamp("completed_at").toInstant(), ZoneOffset.UTC)));
    }

    public Map<County, Integer> countByCounty() {
        return jdbcTemplate.query(
                "SELECT county, count(*) AS count " +
                        "FROM applications " +
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
                        "FROM applications " +
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
                        "FROM applications " +
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
                        "FROM applications " +
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