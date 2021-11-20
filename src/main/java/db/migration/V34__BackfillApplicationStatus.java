package db.migration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * 1. Get applications with non-null statuses
 * <p>
 * 2. Backfill rows in application_status
 */
public class V34__BackfillApplicationStatus extends BaseJavaMigration {

  public static final String BASE_QUERY = """
      SELECT id, %s as status, application_data, county
      FROM applications
      WHERE %s IS NOT NULL
      AND %s != 'null'""";
  ObjectMapper objectMapper;
  JdbcTemplate jdbcTemplate;

  public void migrate(Context context) throws GeneralSecurityException, IOException {
    jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
    objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    insertStatuses("caf_application_status", "CAF");
    insertStatuses("ccap_application_status", "CCAP");
    insertStatuses("uploaded_documents_status", "UPLOADED_DOC");
  }

  private void insertStatuses(String statusColumnName, String documentType) {
    List<Map<String, Object>> parametersForInsert = new ArrayList<>();

    String query = String.format(BASE_QUERY, statusColumnName, statusColumnName, statusColumnName);
    List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(query);
    for (Map<String, Object> rs : queryResults) {
      try {
        // Default to county if routing destinations not provided
        ApplicationData applicationData =
            objectMapper.readValue(rs.get("application_data").toString(), ApplicationData.class);
        List<String> routingDestinationNames = applicationData.getRoutingDestinationNames();
        if (routingDestinationNames == null || routingDestinationNames.isEmpty()) {
          routingDestinationNames = List.of(rs.get("county").toString());
        }

        for (String routingDestination : routingDestinationNames) {
          parametersForInsert.add(Map.of("application_id", rs.get("id"),
              "status", rs.get("status"),
              "document_type", documentType,
              "routing_destination", routingDestination));
        }
      } catch (Exception e) {
        System.out.println("Unable to migrate id=" + rs.get("id"));
        e.printStackTrace();
      }
    }

    NamedParameterJdbcTemplate namedParameterJdbcTemplate =
        new NamedParameterJdbcTemplate(jdbcTemplate);
    for (Map<String, Object> parameter : parametersForInsert) {
      namedParameterJdbcTemplate.update("""
          INSERT INTO application_status (application_id, document_type, routing_destination, status)
          VALUES (:application_id, :document_type, :routing_destination, :status)
          ON CONFLICT DO NOTHING""", parameter);
    }
  }

}