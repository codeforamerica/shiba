package org.codeforamerica.shiba.application;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    String cafStatus = String.valueOf(application.getCafApplicationStatus());
    String ccapStatus = String.valueOf(application.getCcapApplicationStatus());
    String certainPopsStatus = String.valueOf(application.getCertainPopsApplicationStatus());

    if (application.getCafApplicationStatus() != DELIVERED) {
      cafStatus = applicationData.isCAFApplication() ? IN_PROGRESS.toString() : "null";
    }
    if (application.getCcapApplicationStatus() != DELIVERED) {
      ccapStatus = applicationData.isCCAPApplication() ? IN_PROGRESS.toString() : "null";
    }
    if (application.getCertainPopsApplicationStatus() != DELIVERED) {
      certainPopsStatus =
          applicationData.isCertainPopsApplication() ? IN_PROGRESS.toString() : "null";
    }

    parameters.put("cafStatus", cafStatus);
    parameters.put("ccapStatus", ccapStatus);
    parameters.put("certainPopsStatus", certainPopsStatus);

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    int rowCount = namedParameterJdbcTemplate.update(
        "UPDATE applications SET " +
            "completed_at = :completedAt, " +
            "application_data = :applicationData ::jsonb, " +
            "county = :county, " +
            "time_to_complete = :timeToComplete, " +
            "sentiment = :sentiment, " +
            "feedback = :feedback, " +
            "doc_upload_email_status = :docUploadEmailStatus, " +
            "caf_application_status = :cafStatus, " +
            "ccap_application_status = :ccapStatus, " +
            "certain_pops_application_status = :certainPopsStatus, " +
            "flow = :flow WHERE id = :id", parameters);
    rowCount += namedParameterJdbcTemplate.update(
        "INSERT INTO applications (id, completed_at, application_data, county, time_to_complete, sentiment, feedback, flow, doc_upload_email_status) "
            +
            "VALUES (:id, :completedAt, :applicationData ::jsonb, :county, :timeToComplete, :sentiment, :feedback, :flow, :docUploadEmailStatus) "
            +
            "ON CONFLICT DO NOTHING", parameters);

    if (rowCount > 0) {
      logStatusUpdate(application.getId(), CAF, Status.valueFor(cafStatus));
      logStatusUpdate(application.getId(), CCAP, Status.valueFor(ccapStatus));
      logStatusUpdate(application.getId(), CERTAIN_POPS, Status.valueFor(certainPopsStatus));
    }
  }

  public Application find(String id) {
    return jdbcTemplate
        .queryForObject("SELECT * FROM applications WHERE id = ?", applicationRowMapper(),
            id);
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

  public void updateStatus(String id, Document document, Status status) {
    Map<String, Object> parameters = Map.of(
        "status", status.toString(),
        "id", id
    );

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    String statement = switch (document) {
      case CAF -> getUpdateStatusQueryString("caf_application_status", status);
      case CCAP -> getUpdateStatusQueryString("ccap_application_status", status);
      case UPLOADED_DOC -> getUpdateStatusQueryString("uploaded_documents_status", status);
      case CERTAIN_POPS -> getUpdateStatusQueryString("certain_pops_application_status", status);
      default -> null;
    };

    if (statement != null && namedParameterJdbcTemplate.update(statement, parameters) > 0) {
      logStatusUpdate(id, document, status);
    }
  }

  private String getUpdateStatusQueryString(String column, Status status) {
    if (status != DELIVERED) {
      return String.format(
          "UPDATE applications SET %s = :status WHERE id = :id and (%s != 'delivered' or %s is NULL)",
          column, column, column);
    }
    return String.format("UPDATE applications SET %s = :status WHERE id = :id", column);
  }

  private void logStatusUpdate(String id, Document document, Status status) {
    if (status == null) {
      log.info(String.format("%s #%s application status has been updated to null", document, id));
      return;
    }

    final String msg = String.format("%s #%s has been updated to %s", document, id, status);
    switch (status) {
      case DELIVERY_FAILED, RESUBMISSION_FAILED -> log.error(msg);
      default -> log.info(msg);
    }
  }

  public void updateStatusToNull(Document document, String id) {
    Map<String, Object> parameters = Map.of(
        "id", id
    );

    String statement = switch (document) {
      case CAF -> "UPDATE applications SET caf_application_status = null WHERE id = :id";
      case CCAP -> "UPDATE applications SET ccap_application_status = null WHERE id = :id";
      case UPLOADED_DOC -> "UPDATE applications SET uploaded_documents_status = null WHERE id = :id";
      case CERTAIN_POPS -> "UPDATE applications SET certain_pops_application_status = null WHERE id = :id";
      default -> null;
    };

    if (statement != null) {
      var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
      namedParameterJdbcTemplate.update(statement, parameters);
      logStatusUpdate(id, document, null);
    }
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
            .county(County.getCountyForName(resultSet.getString("county")))
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
            .certainPopsApplicationStatus(
                Optional.ofNullable(resultSet.getString("certain_pops_application_status"))
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
