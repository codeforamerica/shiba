package org.codeforamerica.shiba.application;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@Repository
@Slf4j
public class ApplicationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Encryptor<ApplicationData> encryptor;
    private final Clock clock;

    public ApplicationRepository(JdbcTemplate jdbcTemplate,
                                 Encryptor<ApplicationData> encryptor,
                                 Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.encryptor = encryptor;
        this.clock = clock;
    }

    @SuppressWarnings("ConstantConditions")
    public String getNextId() {
        int random3DigitNumber = new SecureRandom().nextInt(900) + 100;

        String id = jdbcTemplate.queryForObject("SELECT NEXTVAL('application_id');", String.class);
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
        HashMap<String, Object> parameters = new HashMap<>(Map.of(
                "id", application.getId(),
                "completedAt", Timestamp.from(application.getCompletedAt().toInstant()),
                "applicationData", encryptor.encrypt(application.getApplicationDataWithoutDataUrls()),
                "county", application.getCounty().name(),
                "timeToComplete", application.getTimeToComplete().getSeconds()
        ));
        parameters.put("flow", Optional.ofNullable(application.getFlow()).map(FlowType::name).orElse(null));
        parameters.put("sentiment", Optional.ofNullable(application.getSentiment()).map(Sentiment::name).orElse(null));
        parameters.put("feedback", application.getFeedback());

        var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.update("UPDATE applications SET " +
                "completed_at = :completedAt, " +
                "application_data = :applicationData ::jsonb, " +
                "county = :county, " +
                "time_to_complete = :timeToComplete, " +
                "sentiment = :sentiment, " +
                "feedback = :feedback, " +
                "flow = :flow WHERE id = :id", parameters);
        namedParameterJdbcTemplate.update(
                "INSERT INTO applications (id, completed_at, application_data, county, time_to_complete, sentiment, feedback, flow) " +
                        "VALUES (:id, :completedAt, :applicationData ::jsonb, :county, :timeToComplete, :sentiment, :feedback, :flow) " +
                        "ON CONFLICT DO NOTHING", parameters);
        setUpdatedAtTime(application.getId());
    }

    public Application find(String id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM applications WHERE id = ?",
                    (resultSet, rowNum) ->
                            Application.builder()
                                    .id(id)
                                    .completedAt(ZonedDateTime.ofInstant(resultSet.getTimestamp("completed_at").toInstant(), ZoneOffset.UTC))
                                    .applicationData(encryptor.decrypt(resultSet.getString("application_data")))
                                    .county(County.valueFor(resultSet.getString("county")))
                                    .timeToComplete(Duration.ofSeconds(resultSet.getLong("time_to_complete")))
                                    .sentiment(Optional.ofNullable(resultSet.getString("sentiment"))
                                            .map(Sentiment::valueOf)
                                            .orElse(null))
                                    .feedback(resultSet.getString("feedback"))
                                    .flow(Optional.ofNullable(resultSet.getString("flow"))
                                            .map(FlowType::valueOf)
                                            .orElse(null))
                                    .build(), id);

        } catch (EmptyResultDataAccessException e) {
            log.error("Unable to locate application with ID " + id);
            throw e;
        }
    }

    public Duration getMedianTimeToComplete() {
        Long medianTimeToComplete = jdbcTemplate.queryForObject("SELECT COALESCE(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY time_to_complete), 0) FROM applications", Long.class);
        return Duration.ofSeconds(Objects.requireNonNull(medianTimeToComplete));
    }

    public Integer count() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM applications;", Integer.class);
    }

    public Map<County, Integer> countByCounty() {
        return jdbcTemplate.query(
                "SELECT county, COUNT(*) AS count " +
                        "FROM applications " +
                        "GROUP BY county", (resultSet, rowNumber) ->
                        Map.entry(
                                County.valueFor(resultSet.getString("county")),
                                resultSet.getInt("count"))).stream()
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    public Map<County, Integer> countByCountyWeekToDate(ZoneId zoneId) {
        ZonedDateTime lowerBound = getBeginningOfWeekForTimeZone(zoneId);
        return jdbcTemplate.query(
                "SELECT county, COUNT(*) AS count " +
                        "FROM applications " +
                        "WHERE completed_at >= ? " +
                        "GROUP BY county",
                (resultSet, rowNumber) -> Map.entry(
                        County.valueFor(resultSet.getString("county")),
                        resultSet.getInt("count")),
                Timestamp.from(lowerBound.toInstant())
        ).stream().collect(toMap(Entry::getKey, Entry::getValue));
    }

    public Duration getAverageTimeToCompleteWeekToDate(ZoneId zoneId) {
        ZonedDateTime lowerBound = getBeginningOfWeekForTimeZone(zoneId);
        Double averageTimeToComplete = jdbcTemplate.queryForObject(
                "SELECT COALESCE(AVG(time_to_complete), 0) AS averagetime " +
                        "FROM applications " +
                        "WHERE completed_at >= ?",
                Double.class,
                Timestamp.from(lowerBound.toInstant())
        );

        return Duration.ofSeconds(Objects.requireNonNull(averageTimeToComplete).longValue());
    }

    public Duration getMedianTimeToCompleteWeekToDate(ZoneId zoneId) {
        ZonedDateTime lowerBound = getBeginningOfWeekForTimeZone(zoneId);
        Long medianTimeToComplete = jdbcTemplate.queryForObject(
                "SELECT COALESCE(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY time_to_complete), 0) " +
                        "FROM applications " +
                        "WHERE completed_at >= ?",
                Long.class,
                Timestamp.from(lowerBound.toInstant()));
        return Duration.ofSeconds(Objects.requireNonNull(medianTimeToComplete));
    }

    @NotNull
    private ZonedDateTime getBeginningOfWeekForTimeZone(ZoneId zoneId) {
        LocalDate localDate = LocalDate.ofInstant(clock.instant(), zoneId);
        LocalDate beginningOfWeek = localDate.minusDays(localDate.getDayOfWeek().getValue());
        return beginningOfWeek.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC);
    }

    public Map<Sentiment, Double> getSentimentDistribution() {
        return jdbcTemplate.query(
                "SELECT sentiment, count, SUM(count) OVER () AS total_count " +
                        "FROM (" +
                        "         SELECT sentiment, COUNT(id) AS count " +
                        "         FROM applications " +
                        "         WHERE sentiment IS NOT NULL " +
                        "         GROUP BY sentiment " +
                        "     ) AS subquery",
                (resultSet, rowNumber) -> Map.entry(
                        Sentiment.valueOf(resultSet.getString("sentiment")),
                        resultSet.getDouble("count") / resultSet.getDouble("total_count"))).stream()
                .collect(toMap(Entry::getKey, Entry::getValue));
    }

    private void setUpdatedAtTime(String id) {
        HashMap<String, Object> parameters = new HashMap<>(Map.of(
                "updatedAt", Timestamp.from(Instant.now()),
                "id", id
        ));

        var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.update("UPDATE applications SET " +
                "updated_at = :updatedAt " +
                "WHERE id = :id", parameters);
    }
    
    public void updateStatus(String id, ApplicationStatusType applicationStatusType, Status status) {
        HashMap<String, Object> parameters = new HashMap<>(Map.of(
                "status", status,
                "id", id
        ));

        var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.update("UPDATE applications SET" +
                applicationStatusType + "= :status " +
                "WHERE id = :id", parameters);
        this.setUpdatedAtTime(id);
    }
}