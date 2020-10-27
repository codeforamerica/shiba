package db.migration.V19_AddIterationToSubworkflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.StringEncryptor;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Migration process
 * <p>
 * Process:
 * 1. Pull all rows from applications table
 * 2. Pull out encryptedData column
 * 3. Decrypt encryptedData column into the data format used before Iteration was added (by overriding certain classes here)
 * 4. Move data from step 3 into current data format (using actual model classes)
 * 5. Re-encrypt data
 * 6. Save to encryptedData column based on id
 **/

public class V19__AddIterationToSubworkflow extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        StringEncryptor stringEncryptor = new StringEncryptor(System.getenv("ENCRYPTION_KEY"));
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ApplicationDataEncryptor encryptor = new ApplicationDataEncryptor(objectMapper, stringEncryptor);
        SqlParameterSource[] sqlParameterSources = jdbcTemplate.query(
                "SELECT id, encrypted_data FROM applications WHERE encrypted_data IS NOT NULL ",
                (rs, n) -> new MapSqlParameterSource("encryptedData", insertIterationsIntoSubworkflows(rs, encryptor))
                        .addValue("id", rs.getString("id"))
        ).toArray(SqlParameterSource[]::new);

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.batchUpdate("UPDATE applications " +
                "SET encrypted_data = :encryptedData " +
                "WHERE id = :id", sqlParameterSources);
    }

    private byte[] insertIterationsIntoSubworkflows(ResultSet result, ApplicationDataEncryptor encryptor) throws SQLException {
        byte[] encryptedData = result.getBytes("encrypted_data");
        try {
            // If this data is in the v18 structure (Subworkflows are a list of PageData instead of Iteration), create a new ApplicationData object to encrypt
            V18ApplicationData oldApplicationData = encryptor.decrypt(encryptedData);

            ApplicationData migratedApplicationData = new ApplicationData();
            migratedApplicationData.setPagesData(oldApplicationData.getPagesData());
            migratedApplicationData.setFlow(oldApplicationData.getFlow());
            migratedApplicationData.setId(oldApplicationData.getId());
            migratedApplicationData.setStartTime(oldApplicationData.getStartTime());
            migratedApplicationData.setIncompleteIterations(oldApplicationData.getIncompleteIterations());

            oldApplicationData.getSubworkflows().forEach((groupName, subworkflow) -> {
                subworkflow.forEach(pagesData -> {
                    migratedApplicationData.getSubworkflows().addIteration(groupName, pagesData);
                });
            });

            return encryptor.encrypt(migratedApplicationData);
        } catch (MismatchedInputException exception) {
            // The encrypted data is already in the new format, so just return it
            return encryptedData;
        } catch (JsonProcessingException e) {
            // Something unexpected
            throw new IllegalStateException(e);
        }
    }

    @Data
    @NoArgsConstructor
    protected static class V18ApplicationData {
        private PagesData pagesData = new PagesData();
        private V18Subworkflows subworkflows = new V18Subworkflows();
        private FlowType flow = null;
        private String id;
        private Instant startTime;
        @JsonIgnore
        private HashMap<String, PagesData> incompleteIterations;
    }

    protected static class V18Subworkflows extends HashMap<String, V18Subworkflow> {
    }

    protected static class V18Subworkflow extends ArrayList<PagesData> {
    }

    protected static class ApplicationDataEncryptor {
        private final ObjectMapper objectMapper;
        private final StringEncryptor stringEncryptor;

        public ApplicationDataEncryptor(ObjectMapper objectMapper, StringEncryptor stringEncryptor) {
            this.objectMapper = objectMapper;
            this.stringEncryptor = stringEncryptor;
        }

        public V18ApplicationData decrypt(byte[] encryptedData) throws JsonProcessingException {
            return objectMapper.readValue(stringEncryptor.decrypt(encryptedData), V18ApplicationData.class);
        }

        public byte[] encrypt(ApplicationData data) {
            try {
                return stringEncryptor.encrypt(objectMapper.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
