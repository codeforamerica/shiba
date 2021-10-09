package org.codeforamerica.shiba.application;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import java.util.Map.Entry;
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
    parameters.put("docUploadEmailStatus",
        Optional.ofNullable(application.getDocUploadEmailStatus()).map(Status::toString)
            .orElse(null));

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    namedParameterJdbcTemplate.update("UPDATE applications SET " +
        "completed_at = :completedAt, " +
        "application_data = :applicationData ::jsonb, " +
        "county = :county, " +
        "time_to_complete = :timeToComplete, " +
        "sentiment = :sentiment, " +
        "feedback = :feedback, " +
        "doc_upload_email_status = :docUploadEmailStatus," +
        "flow = :flow WHERE id = :id", parameters);
    namedParameterJdbcTemplate.update(
        "INSERT INTO applications (id, completed_at, application_data, county, time_to_complete, sentiment, feedback, flow, doc_upload_email_status) "
            +
            "VALUES (:id, :completedAt, :applicationData ::jsonb, :county, :timeToComplete, :sentiment, :feedback, :flow, :docUploadEmailStatus) "
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

    String statement = switch (document) {
      case CAF -> "UPDATE applications SET caf_application_status = null WHERE id = :id";
      case CCAP -> "UPDATE applications SET ccap_application_status = null WHERE id = :id";
      case UPLOADED_DOC -> "UPDATE applications SET uploaded_documents_status = null WHERE id = :id";
      case CERTAIN_POPS -> "UPDATE applications SET certain_pops_application_status = null WHERE id = :id";
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
      case CERTAIN_POPS -> "UPDATE applications SET certain_pops_application_status = :status WHERE id = :id";
    };
  }

  public Map<Document, List<String>> getApplicationIdsToResubmit() {
    Map<Document, List<String>> failedSubmissions = new HashMap<>();
    failedSubmissions.put(CCAP, getCCAPSubmissionsToResubmit());
    failedSubmissions.put(CAF, getCAFSubmissionsToResubmit());
    failedSubmissions.put(UPLOADED_DOC, getUploadedDocSubmissionsToResubmit());
    return failedSubmissions;
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
            .docUploadEmailStatus(
                Optional.ofNullable(resultSet.getString("doc_upload_email_status"))
                    .map(Status::valueFor)
                    .orElse(null))
            .build();
  }
}
