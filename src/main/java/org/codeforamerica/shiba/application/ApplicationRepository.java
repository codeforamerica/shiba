package org.codeforamerica.shiba.application;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class ApplicationRepository {

  private final JdbcTemplate jdbcTemplate;
  private final Encryptor<ApplicationData> encryptor;

  public ApplicationRepository(JdbcTemplate jdbcTemplate,
      Encryptor<ApplicationData> encryptor) {
    this.jdbcTemplate = jdbcTemplate;
    this.encryptor = encryptor;
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
    ApplicationData applicationData = application.getApplicationData();
    HashMap<String, Object> parameters = new HashMap<>(Map.of(
        "id", application.getId(),
        "applicationData", encryptor.encrypt(applicationData),
        "county", application.getCounty().name()
    ));
    parameters.put("completedAt", convertToTimestamp(application.getCompletedAt()));
    parameters.put("timeToComplete",
        Optional.ofNullable(application.getTimeToComplete()).map(Duration::getSeconds)
            .orElse(null));
    parameters.put("flow",
        Optional.ofNullable(application.getFlow()).map(FlowType::name).orElse(null));
    parameters.put("sentiment",
        Optional.ofNullable(application.getSentiment()).map(Sentiment::name).orElse(null));
    parameters.put("feedback", application.getFeedback());
    parameters.put("docUploadEmailStatus",
        Optional.ofNullable(application.getDocUploadEmailStatus()).map(Status::toString)
            .orElse(null));

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    namedParameterJdbcTemplate.update(
        "UPDATE applications SET " +
            "completed_at = :completedAt, " +
            "application_data = :applicationData ::jsonb, " +
            "county = :county, " +
            "time_to_complete = :timeToComplete, " +
            "sentiment = :sentiment, " +
            "feedback = :feedback, " +
            "doc_upload_email_status = :docUploadEmailStatus, " +
            "flow = :flow WHERE id = :id", parameters);
    namedParameterJdbcTemplate.update(
        "INSERT INTO applications (id, completed_at, application_data, county, time_to_complete, sentiment, feedback, flow, doc_upload_email_status) "
            +
            "VALUES (:id, :completedAt, :applicationData ::jsonb, :county, :timeToComplete, :sentiment, :feedback, :flow, :docUploadEmailStatus) "
            +
            "ON CONFLICT DO NOTHING", parameters);
  }

  public Application find(String id) {
    // TODO use a single sql query with a join instead of doing two separate sql queries
    Application application = jdbcTemplate.queryForObject("SELECT * FROM applications WHERE id = ?",
        applicationRowMapper(), id);
    Objects.requireNonNull(application).setDocumentStatuses(
        jdbcTemplate.query("SELECT * FROM application_status WHERE application_id = ?",
            new ApplicationStatusRowMapper(), id));
    return application;
  }

  public List<Application> findApplicationsStuckInProgress() {
    Timestamp twelveHoursAgo = Timestamp.from(Instant.now().minus(Duration.ofHours(16)));
    List<Application> applicationsStuckInProgress = jdbcTemplate.query(
        "SELECT * FROM applications where completed_at IS NOT NULL AND completed_at BETWEEN '2021-12-06' AND ? AND id IN ("
        + "    SELECT application_id FROM application_status WHERE status= 'in_progress' OR status ='sending'"
        + "          AND (document_type='CAF' OR document_type='CCAP' OR document_type='UPLOADED_DOC' OR document_type='CERTAIN_POPS')"
        + "    ) ORDER BY completed_at LIMIT 5",
        applicationRowMapper(),
        twelveHoursAgo);

    // add document statuses to apps
    for (Application app : applicationsStuckInProgress) {
      app.setDocumentStatuses(
          jdbcTemplate.query("SELECT * FROM application_status WHERE application_id = ?",
              new ApplicationStatusRowMapper(), app.getId()));
    }

    return applicationsStuckInProgress;
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

  public void setDocUploadEmailStatus(String applicationId, Status status) {
    Map<String, String> parameters = Map.of(
        "id", applicationId,
        "status", status.toString()
    );

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    namedParameterJdbcTemplate.update(
        "UPDATE applications SET doc_upload_email_status = :status WHERE id = :id", parameters);
  }

  public List<Application> getApplicationsSubmittedBetweenTimestamps(Timestamp start,
      Timestamp end) {
    return jdbcTemplate.query(
        "SELECT * FROM applications WHERE completed_at >= ? AND completed_at <= ?",
        applicationRowMapper(),
        start,
        end);
  }

  @NotNull
  private RowMapper<Application> applicationRowMapper() {
    return (resultSet, rowNum) ->
        Application.builder()
            .id(resultSet.getString("id"))
            .completedAt(convertToZonedDateTime(resultSet.getTimestamp("completed_at")))
            .updatedAt(convertToZonedDateTime(resultSet.getTimestamp("updated_at")))
            .applicationData(encryptor.decrypt(resultSet.getString("application_data")))
            .county(County.getCountyForName(resultSet.getString("county")))
            .timeToComplete(Duration.ofSeconds(resultSet.getLong("time_to_complete")))
            .sentiment(Optional.ofNullable(resultSet.getString("sentiment"))
                .map(Sentiment::valueOf)
                .orElse(null))
            .feedback(resultSet.getString("feedback"))
            .flow(Optional.ofNullable(resultSet.getString("flow"))
                .map(FlowType::valueOf)
                .orElse(null))
            .docUploadEmailStatus(
                Optional.ofNullable(resultSet.getString("doc_upload_email_status"))
                    .map(Status::valueFor)
                    .orElse(null))
            .build();
  }

  private static class ApplicationStatusRowMapper implements RowMapper<DocumentStatus> {

    @Override
    public DocumentStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new DocumentStatus(
          rs.getString("application_id"),
          Document.valueOf(rs.getString("document_type")),
          rs.getString("routing_destination"),
          Status.valueFor(rs.getString("status"))
      );
    }
  }
}
