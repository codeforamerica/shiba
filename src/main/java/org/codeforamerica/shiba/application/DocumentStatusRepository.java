package org.codeforamerica.shiba.application;

import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class DocumentStatusRepository {

  private final JdbcTemplate jdbcTemplate;
  private final RoutingDecisionService routingDecisionService;

  public DocumentStatusRepository(JdbcTemplate jdbcTemplate,
      RoutingDecisionService routingDecisionService) {
    this.jdbcTemplate = jdbcTemplate;
    this.routingDecisionService = routingDecisionService;
  }

  public List<DocumentStatus> findAll(String applicationId) {
    return jdbcTemplate.query("SELECT * FROM document_status WHERE application_id = ?",
        new DocumentStatusRowMapper(), applicationId);
  }

  public void createOrUpdateAll(Application application,
      Status status) {
    List<Document> documents = DocumentListParser.parse(application.getApplicationData());

    for (Document document : documents) {
      createOrUpdateAllForDocumentType(application.getApplicationData(), status, document);
    }
  }

  public void createOrUpdateAllForDocumentType(ApplicationData applicationData, Status status, Document document) {
		List<RoutingDestination> routingDestinations = routingDecisionService.getRoutingDestinations(
				applicationData, document);
	      routingDestinations.forEach(
	          routingDestination -> createOrUpdate(applicationData.getId(), document, routingDestination.getName(),
	              status));
  }
  
  public void createOrUpdate(String id, Document document, String routingDestinationName, Status status) {
    if (document == null || routingDestinationName == null) {
      return;
    }

    String updateStatement = """
        UPDATE document_status SET status = :status WHERE application_id = :application_id
        AND document_type = :document_type AND routing_destination = :routing_destination
        """;

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("application_id", id);
    parameters.put("status", status.toString());
    parameters.put("document_type", document.name());
    parameters.put("routing_destination", routingDestinationName);

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

    int rowCount = namedParameterJdbcTemplate.update(updateStatement, parameters);
    if (rowCount == 0) {
      // Not found, add a new entry
      String insertStatement = """
          INSERT INTO document_status (application_id, status, document_type, routing_destination)
          VALUES (:application_id, :status, :document_type, :routing_destination)
          """;
      rowCount = namedParameterJdbcTemplate.update(insertStatement, parameters);
    }

    if (rowCount != 0) {
      logStatusUpdate(id, document, routingDestinationName, status);
    }

  }

  public List<DocumentStatus> getDocumentStatusToResubmit() {
    return jdbcTemplate.query(
        "SELECT * FROM document_status WHERE document_type != 'XML' AND status = 'delivery_failed'",
        new DocumentStatusRowMapper());
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

  private static class DocumentStatusRowMapper implements RowMapper<DocumentStatus> {
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
