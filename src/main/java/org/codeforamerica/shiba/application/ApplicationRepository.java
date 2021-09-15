package org.codeforamerica.shiba.application;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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
        "applicationData", encryptor.encrypt(application.getApplicationData()),
        "county", application.getCounty().name()
    ));
    parameters.put("completedAt", convertToTimestamp(application.getCompletedAt()));
    parameters.put("timeToComplete",
        Optional.ofNullable(application.getTimeToComplete()).map(Duration::getSeconds)
            .orElse(null));
    parameters
        .put("flow",
            Optional.ofNullable(application.getFlow()).map(FlowType::name).orElse(null));
    parameters.put("sentiment",
        Optional.ofNullable(application.getSentiment()).map(Sentiment::name).orElse(null));
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
        "INSERT INTO applications (id, completed_at, application_data, county, time_to_complete, sentiment, feedback, flow) "
            +
            "VALUES (:id, :completedAt, :applicationData ::jsonb, :county, :timeToComplete, :sentiment, :feedback, :flow) "
            +
            "ON CONFLICT DO NOTHING", parameters);
    setUpdatedAtTime(application.getId());
  }

  public Application find(String id) {
    try {
      return jdbcTemplate
          .queryForObject("SELECT * FROM applications WHERE id = ?", applicationRowMapper(),
              id);
    } catch (EmptyResultDataAccessException e) {
      log.error("Unable to locate application with ID " + id);
      throw e;
    }
  }

  private ZonedDateTime convertToZonedDateTime(Timestamp timestamp) {
    return Optional.ofNullable(timestamp)
        .map(time -> ZonedDateTime.ofInstant(time.toInstant(), ZoneOffset.UTC))
        .orElse(null);
  }

  private Timestamp convertToTimestamp(ZonedDateTime dateTime) {
    return Optional.ofNullable(dateTime)
        .map(time -> Timestamp.from(time.toInstant()))
        .orElse(null);
  }

  public Duration getMedianTimeToComplete() {
    Long medianTimeToComplete = jdbcTemplate.queryForObject(
        "SELECT COALESCE(PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY time_to_complete), 0) FROM applications  WHERE flow <> 'LATER_DOCS' AND completed_at IS NOT NULL;",
        Long.class);
    return Duration.ofSeconds(Objects.requireNonNull(medianTimeToComplete));
  }

  public Integer count() {
    return jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM applications WHERE flow <> 'LATER_DOCS' AND completed_at IS NOT NULL;",
        Integer.class);
  }

  public Map<County, Integer> countByCounty() {
    return jdbcTemplate.query(
        "SELECT county, COUNT(*) AS count " +
            "FROM applications  WHERE flow <> 'LATER_DOCS' AND completed_at IS NOT NULL " +
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
            "WHERE flow <> 'LATER_DOCS' AND completed_at >= ? " +
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
            "WHERE flow <> 'LATER_DOCS' AND completed_at >= ?",
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
            "WHERE flow <> 'LATER_DOCS' AND completed_at >= ? ",
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
    Map<String, Object> parameters = Map.of(
        "updatedAt", Timestamp.from(Instant.now()),
        "id", id
    );

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    namedParameterJdbcTemplate.update("UPDATE applications SET " +
        "updated_at = :updatedAt " +
        "WHERE id = :id", parameters);
  }

  public void updateStatus(String id, Document document, Status status) {
    Map<String, Object> parameters = Map.of(
        "status", status.toString(),
        "id", id
    );

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    namedParameterJdbcTemplate.update(selectStatusColumn(document), parameters);
    switch (status) {
      case IN_PROGRESS, SENDING, DELIVERED -> log
          .info(String.format("%s #%s has been updated to %s", document, id, status));
      case DELIVERY_FAILED, RESUBMISSION_FAILED -> log
          .error(String.format("%s #%s has been updated to %s", document, id, status));
    }
    setUpdatedAtTime(id);
  }

  public void updateStatusToNull(Document document, String id) {
    Map<String, String> parameters = Map.of(
        "id", id
    );

    String statement =
        switch (document) {
          case CAF -> "UPDATE applications SET caf_application_status = null WHERE id = :id";
          case CCAP -> "UPDATE applications SET ccap_application_status = null WHERE id = :id";
          case UPLOADED_DOC -> "UPDATE applications SET uploaded_documents_status = null WHERE id = :id";
        };

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    namedParameterJdbcTemplate.update(statement, parameters);
    log.info(String.format("%s #%s application status has been updated to null", document, id));
    setUpdatedAtTime(id);
  }

  private String selectStatusColumn(Document document) {
    return switch (document) {
      case CAF -> "UPDATE applications SET caf_application_status = :status WHERE id = :id";
      case CCAP -> "UPDATE applications SET ccap_application_status = :status WHERE id = :id";
      case UPLOADED_DOC -> "UPDATE applications SET uploaded_documents_status = :status WHERE id = :id";
    };
  }

  public Map<Document, List<String>> getApplicationIdsToResubmit() {
    Map<Document, List<String>> failedSubmissions = new HashMap<>();
    failedSubmissions.put(CCAP, getCCAPSubmissionsToResubmit());
    failedSubmissions.put(CAF, getCAFSubmissionsToResubmit());
    failedSubmissions.put(UPLOADED_DOC, getUploadedDocSubmissionsToResubmit());
    return failedSubmissions;
  }

  private List<String> getCCAPSubmissionsToResubmit() {
    return jdbcTemplate.queryForList(
        "SELECT id FROM applications WHERE ccap_application_status = 'delivery_failed' AND completed_at IS NOT NULL",
        String.class);
  }

  private List<String> getCAFSubmissionsToResubmit() {
    return jdbcTemplate.queryForList(
        "SELECT id FROM applications WHERE caf_application_status = 'delivery_failed' AND completed_at IS NOT NULL",
        String.class);
  }

  private List<String> getUploadedDocSubmissionsToResubmit() {
    return jdbcTemplate.queryForList(
        "SELECT id FROM applications WHERE uploaded_documents_status = 'delivery_failed' AND completed_at IS NOT NULL",
        String.class);
  }

  @NotNull
  private RowMapper<Application> applicationRowMapper() {
    return (resultSet, rowNum) ->
        Application.builder()
            .id(resultSet.getString("id"))
            .completedAt(convertToZonedDateTime(resultSet.getTimestamp("completed_at")))
            .updatedAt(convertToZonedDateTime(resultSet.getTimestamp("updated_at")))
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
            .cafApplicationStatus(
                Optional.ofNullable(resultSet.getString("caf_application_status"))
                    .map(Status::valueFor)
                    .orElse(null))
            .ccapApplicationStatus(
                Optional.ofNullable(resultSet.getString("ccap_application_status"))
                    .map(Status::valueFor)
                    .orElse(null))
            .uploadedDocumentApplicationStatus(
                Optional.ofNullable(resultSet.getString("uploaded_documents_status"))
                    .map(Status::valueFor)
                    .orElse(null))
            .build();
  }

}
