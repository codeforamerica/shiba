package org.codeforamerica.shiba.application;

import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;

import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
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
    Application application = jdbcTemplate.queryForObject("SELECT * FROM applications WHERE id = ?",
        applicationRowMapper(), id);
    Objects.requireNonNull(application).setApplicationStatuses(
        jdbcTemplate.query("SELECT * FROM application_status WHERE application_id = ?",
            new ApplicationStatusRowMapper(), id));
    return application;
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

  public void updateStatusToInProgress(Application application,
      RoutingDecisionService routingDecisionService) {
    List<Document> documents = DocumentListParser.parse(application.getApplicationData());

    for (Document document : documents) {
      List<RoutingDestination> routingDestinations = routingDecisionService.getRoutingDestinations(
          application.getApplicationData(), document);
      updateStatus(application.getId(), document, routingDestinations, IN_PROGRESS);
    }
  }

  public void updateStatus(String id, Document document,
      List<RoutingDestination> routingDestinations, Status status) {
    routingDestinations.forEach(
        routingDestination -> updateStatus(id, document, routingDestination.getName(), status));
  }

  public void updateStatus(String id, Document document, RoutingDestination routingDestination,
      Status status) {
    updateStatus(id, document, routingDestination.getName(), status);
  }

  /**
   * Try to update existing status - if it's not found, add a new one.
   */
  public void updateStatus(String id, Document document, String routingDestination, Status status) {
    if (document == null || routingDestination == null) {
      return;
    }

    String updateStatement = """
        UPDATE application_status SET status = :status WHERE application_id = :application_id
        AND document_type = :document_type AND routing_destination = :routing_destination
        """;

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("application_id", id);
    parameters.put("status", status.toString());
    parameters.put("document_type", document.name());
    parameters.put("routing_destination", routingDestination);

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

    int rowCount = namedParameterJdbcTemplate.update(updateStatement, parameters);
    if (rowCount == 0) {
      // Not found, add a new entry
      String insertStatement = """
          INSERT INTO application_status (application_id, status, document_type, routing_destination)
          VALUES (:application_id, :status, :document_type, :routing_destination)
          """;
      rowCount = namedParameterJdbcTemplate.update(insertStatement, parameters);
    }

    if (rowCount != 0) {
      logStatusUpdate(id, document, routingDestination, status);
    }

  }

  private void logStatusUpdate(String id, Document document, String routingDestination,
      Status status) {
    if (status == null) {
      log.info(String.format("%s to %s #%s application status has been updated to null", document,
          routingDestination, id));
      return;
    }

    final String msg = String.format("%s to %s #%s has been updated to %s", document,
        routingDestination, id, status);
    switch (status) {
      case DELIVERY_FAILED, RESUBMISSION_FAILED -> log.error(msg);
      default -> log.info(msg);
    }
  }

  public void updateStatusToNull(Document document, String id) {
    Map<String, Object> parameters = Map.of("id", id);

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
      log.info(String.format("%s #%s application status has been updated to null", document, id));
    }
  }

  public List<ApplicationStatus> getApplicationStatusToResubmit() {
    return jdbcTemplate.query(
        "SELECT * FROM application_status WHERE document_type != 'XML' AND status = 'delivery_failed'",
        new ApplicationStatusRowMapper());
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

  private static class ApplicationStatusRowMapper implements RowMapper<ApplicationStatus> {

    @Override
    public ApplicationStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new ApplicationStatus(
          rs.getString("application_id"),
          Document.valueOf(rs.getString("document_type")),
          rs.getString("routing_destination"),
          Status.valueFor(rs.getString("status"))
      );
    }
  }
}
