package org.codeforamerica.shiba.application;

import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.Utils;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;

@Repository
@Slf4j
public class ApplicationStatusRepository {

  private final JdbcTemplate jdbcTemplate;
  private final RoutingDecisionService routingDecisionService;
  private final FilenameGenerator filenameGenerator;
  private final PdfGenerator pdfGenerator;

  public ApplicationStatusRepository(JdbcTemplate jdbcTemplate,
      RoutingDecisionService routingDecisionService, FilenameGenerator filenameGenerator,
      PdfGenerator pdfGenerator) {
    this.jdbcTemplate = jdbcTemplate;
    this.routingDecisionService = routingDecisionService;
    this.filenameGenerator = filenameGenerator;
    this.pdfGenerator = pdfGenerator;
  }

  public List<ApplicationStatus> findAll(String applicationId) {
    return jdbcTemplate.query("SELECT * FROM application_status WHERE application_id = ?",
        new ApplicationStatusRowMapper(), applicationId);
  }

  public void createOrUpdateApplicationType(Application application, Status status) {
    List<Document> documents = DocumentListParser.parse(application.getApplicationData());
    handleDocumentDifference(application, documents);

    for (Document document : documents) {
      createOrUpdateAllForDocumentType(application, status, document);
    }
  }

  private void handleDocumentDifference(Application application, List<Document> documents) {
    List<Document> previousDocuments = new ArrayList<>();
    List<ApplicationStatus> listOfStatuses = findAll(application.getId());
    listOfStatuses.forEach(ds -> previousDocuments.add(ds.getDocumentType()));
    List<Document> docsToDelete = previousDocuments.stream()
        .filter(docType -> !documents.contains(docType))
        .collect(Collectors.toList());

    delete(application.getId(), docsToDelete);
  }

  public void createOrUpdateAllForDocumentType(Application application, Status status,
      Document document) {
    ApplicationData applicationData = application.getApplicationData();
    List<RoutingDestination> routingDestinations =
        routingDecisionService.getRoutingDestinations(applicationData, document);
    routingDestinations.forEach(routingDestination -> {
      var fileNames = getAndSetFileNames(application, document, routingDestination);
      fileNames.forEach(fileName -> createOrUpdate(applicationData.getId(), document,
          routingDestination.getName(), status, fileName));
    });
  }

  public List<String> getAndSetFileNames(Application application, Document document, RoutingDestination routingDest) {
    List<String> fileNames = new ArrayList<>();
    if (document.equals(UPLOADED_DOC)) {
      var uploadedDocs = application.getApplicationData().getUploadedDocs();
      if (uploadedDocs.size() == 0) {
        fileNames.add("");
      }
      byte[] coverPage = pdfGenerator.generateCoverPageForUploadedDocs(application);
      
      List<ApplicationFile> preparedDocumentList =
          pdfGenerator.generateCombinedUploadedDocument(uploadedDocs, application, coverPage, routingDest);
     
      if(preparedDocumentList!=null && !preparedDocumentList.isEmpty()) {
        for(ApplicationFile file:preparedDocumentList) {
          fileNames.add(file.getFileName());
        }
      } else {
        for (int i = 0; i < uploadedDocs.size(); i++) {
          String fileName = uploadedDocs.get(i).getSysFileName();
          if (fileName == null || !fileName.contains(routingDest.getDhsProviderId())) {
            String extension = Utils.getFileType(uploadedDocs.get(i).getFilename());
            fileName = filenameGenerator.generateUploadedDocumentName(application, i, extension,
                routingDest);
          }
          fileNames.add(fileName);
        }
      }
    } else {
      String fileName = filenameGenerator.generatePdfFilename(application, document, routingDest);
      fileNames.add(fileName);
    }
    return fileNames;
  }
  
public List<String> getAndSetFileNames(Application application, Document document){
    List<RoutingDestination> routingDestinations =
        routingDecisionService.getRoutingDestinations(application.getApplicationData(), document);
   var fileName = routingDestinations.stream().distinct().map(rtDest -> getAndSetFileNames(application, document, rtDest)).collect(Collectors.toList());
   return fileName.get(0);
  }
  public void createOrUpdate(String applicationId, Document document,
      String routingDestinationName,
      Status status, String documentName) {
    if (document == null || routingDestinationName == null) {
      return;
    }

    String updateStatement = """
        UPDATE application_status SET status = :status WHERE application_id = :application_id
        AND document_type = :document_type AND routing_destination = :routing_destination AND document_name = :document_name
        """;

    Map<String, Object> parameters = new HashMap<>();
    parameters.put("application_id", applicationId);
    parameters.put("status", status.toString());
    parameters.put("document_type", document.name());
    parameters.put("routing_destination", routingDestinationName);
    parameters.put("document_name", documentName);

    var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);

    int rowCount = namedParameterJdbcTemplate.update(updateStatement, parameters);
    if (rowCount == 0) {
      // Not found, add a new entry
      String insertStatement = """
          INSERT INTO application_status (application_id, status, document_type, routing_destination, document_name)
          VALUES (:application_id, :status, :document_type, :routing_destination, :document_name)
          """;
      rowCount = namedParameterJdbcTemplate.update(insertStatement, parameters);
    }

    if (rowCount != 0) {
      logStatusUpdate(applicationId, document, routingDestinationName, status, documentName);
    }
  }

  public void updateFilenetId(String applicationId, Document document,
      String routingDestinationName, Status status,
      String documentName, String filenetId) {
    String updateStatement = """
        UPDATE application_status SET filenet_id = ? WHERE application_id = ?
        AND document_type = ? AND status = ? AND routing_destination = ? AND document_name = ?
        """;
    jdbcTemplate.update(updateStatement, filenetId, applicationId, document.name(),
        status.toString(), routingDestinationName, documentName);
  }

  public void delete(String applicationId, List<Document> documents) {
    if (!documents.isEmpty()) {
      String deleteStatement = """
          DELETE FROM application_status WHERE application_id = :application_id
          AND document_type in (:document_types)
          """;
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("application_id", applicationId);
      parameters.put("document_types", documents.stream().map(Enum::toString).toList());

      var namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
      namedParameterJdbcTemplate.update(deleteStatement, parameters);
    }
  }

  public List<ApplicationStatus> getDocumentStatusToResubmit() {
    return jdbcTemplate.query(
        "SELECT * FROM application_status WHERE document_type != 'XML' AND status = 'delivery_failed'",
        new ApplicationStatusRowMapper());
  }

  private void logStatusUpdate(String id, Document document, String routingDestination,
      Status status, String documentName) {
    MDC.put("applicationId", id);
    if (status == null) {
      log.info(
          String.format("%s = %s to %s #%s application status has been updated to null", document,
              documentName,
              routingDestination, id));
      return;
    }

    log.info(String.format("%s = %s to %s #%s has been updated to %s", document, documentName,
        routingDestination, id, status));
  }

  public ApplicationStatus find(String id, Document document, String routingDestinationName,
      String documentName) {
    return jdbcTemplate.queryForObject(
        "SELECT * FROM application_status WHERE application_id = ? AND "
            + "document_type = ? AND routing_destination = ? AND document_name = ?"
        , new ApplicationStatusRowMapper(), id, document.toString(),
        routingDestinationName, documentName
    );
  }

  private static class ApplicationStatusRowMapper implements RowMapper<ApplicationStatus> {

    @Override
    public ApplicationStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new ApplicationStatus(
          rs.getString("application_id"),
          Document.valueOf(rs.getString("document_type")),
          rs.getString("routing_destination"),
          Status.valueFor(rs.getString("status")),
          rs.getString("document_name"),
          rs.getString("filenet_id")
      );
    }
  }
}
