package org.codeforamerica.shiba.application;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

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
        HashMap<String, Object> parameters = new HashMap<>(Map.of(
                "id", application.getId(),
                "completedAt", Timestamp.from(application.getCompletedAt().toInstant()),
                "encryptedData", encryptor.encrypt(application.getApplicationData()),
                "county", application.getCounty().name(),
                "timeToComplete", application.getTimeToComplete().getSeconds()
        ));
        parameters.put("flow", Optional.ofNullable(application.getFlow()).map(FlowType::name).orElse(null));
        parameters.put("sentiment", Optional.ofNullable(application.getSentiment()).map(Sentiment::name).orElse(null));
        parameters.put("feedback", application.getFeedback());
        new NamedParameterJdbcTemplate(jdbcTemplate)
                .update("INSERT INTO applications (id, completed_at, encrypted_data, county, time_to_complete, sentiment, feedback, flow) " +
                        "VALUES (:id, :completedAt, :encryptedData, :county, :timeToComplete, :sentiment, :feedback, :flow) " +
                        "ON CONFLICT (id) DO UPDATE SET " +
                        "completed_at = :completedAt, " +
                        "encrypted_data = :encryptedData, " +
                        "county = :county, " +
                        "time_to_complete = :timeToComplete, " +
                        "sentiment = :sentiment, " +
                        "feedback = :feedback, " +
                        "flow = :flow", parameters);
    }

    public Application find(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM applications WHERE id = ?",
                (resultSet, rowNum) -> Application.builder()
                                .id(id)
                                .completedAt(ZonedDateTime.ofInstant(resultSet.getTimestamp("completed_at").toInstant(), ZoneOffset.UTC))
                                .applicationData(encryptor.decrypt(resultSet.getBytes("encrypted_data")))
                                .county(County.valueFor(resultSet.getString("county")))
                                .timeToComplete(Duration.ofSeconds(resultSet.getLong("time_to_complete")))
                                .sentiment(Optional.ofNullable(resultSet.getString("sentiment"))
                                                .map(Sentiment::valueOf)
                                                .orElse(null))
                                .feedback(resultSet.getString("feedback"))
                                .flow(Optional.ofNullable(resultSet.getString("flow"))
                                .map(FlowType::valueOf)
                                .orElse(null))
                                .build(),
                id);
    }

    public List<Application> findAll() {
        return jdbcTemplate.query("SELECT * FROM applications",
                (resultSet, rowNum) -> Application.builder()
                        .id(resultSet.getString("id"))
                        .completedAt(ZonedDateTime.ofInstant(resultSet.getTimestamp("completed_at").toInstant(), ZoneOffset.UTC))
                        .applicationData(encryptor.decrypt(resultSet.getBytes("encrypted_data")))
                        .county(County.valueFor(resultSet.getString("county")))
                        .timeToComplete(Duration.ofSeconds(resultSet.getLong("time_to_complete")))
                        .sentiment(Optional.ofNullable(resultSet.getString("sentiment"))
                                .map(Sentiment::valueOf)
                                .orElse(null))
                        .feedback(resultSet.getString("feedback"))
                        .flow(Optional.ofNullable(resultSet.getString("flow"))
                                .map(FlowType::valueOf)
                                .orElse(null))
                        .build());
    }

    public Duration getMedianTimeToComplete() {
        Long medianTimeToComplete = jdbcTemplate.queryForObject("SELECT COALESCE(percentile_cont(0.5) WITHIN GROUP (ORDER BY time_to_complete), 0) FROM applications", Long.class);
        return Duration.ofSeconds(Objects.requireNonNull(medianTimeToComplete));
    }

    public Integer count() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM applications;", Integer.class);
    }

    public Map<County, Integer> countByCounty() {
        return jdbcTemplate.query(
                "SELECT county, count(*) AS count " +
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
                "SELECT county, count(*) AS count " +
                        "FROM applications " +
                        "WHERE completed_at >= ? " +
                        "GROUP BY county",
                new Object[]{Timestamp.from(lowerBound.toInstant())},
                (resultSet, rowNumber) ->
                        Map.entry(
                                County.valueFor(resultSet.getString("county")),
                                resultSet.getInt("count"))).stream()
                .collect(toMap(Entry::getKey, Entry::getValue));
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

    public Map<Sentiment, Double> getSentimentDistribution() {
        return jdbcTemplate.query(
                "SELECT sentiment, count, sum(count) over () as total_count " +
                        "FROM (" +
                        "         SELECT sentiment, count(id) as count " +
                        "         FROM applications " +
                        "         WHERE sentiment IS NOT NULL " +
                        "         GROUP BY sentiment " +
                        "     ) as subquery",
                (resultSet, rowNumber) -> Map.entry(
                        Sentiment.valueOf(resultSet.getString("sentiment")),
                        resultSet.getDouble("count") / resultSet.getDouble("total_count"))).stream()
                .collect(toMap(Entry::getKey, Entry::getValue));
    }
}