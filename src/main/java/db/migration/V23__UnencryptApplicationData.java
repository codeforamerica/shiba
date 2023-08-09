package db.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.subtle.Hex;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V23__UnencryptApplicationData extends BaseJavaMigration {
    ObjectMapper objectMapper;
    ApplicationDataEncryptor applicationDataEncryptor;

    public void migrate(Context context) throws GeneralSecurityException, IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        applicationDataEncryptor = new ApplicationDataEncryptor(objectMapper, System.getenv("ENCRYPTION_KEY"));
        SqlParameterSource[] sqlParameterSources = jdbcTemplate.query(
                "SELECT id, encrypted_data " +
                        "FROM applications " +
                        "WHERE encrypted_data IS NOT NULL " +
                        "AND application_data IS NULL", (rs, rowNum) ->
                {
                    try {
                        return new MapSqlParameterSource("application_data", unencryptedAppDataWithEncryptedSSN(rs))
                                .addValue("id", rs.getString("id"));
                    } catch (Exception e) {
                        System.out.println("Unable to migrate id=" + rs.getString("id"));
                        e.printStackTrace();
                        return new MapSqlParameterSource("application_data", "")
                                .addValue("id", rs.getString("id"));
                    }
                })
                .toArray(SqlParameterSource[]::new);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.batchUpdate(
                "UPDATE applications " +
                        "SET application_data = to_json(:application_data)" +
                        "WHERE id = :id", sqlParameterSources);
    }

    private PGobject unencryptedAppDataWithEncryptedSSN(ResultSet rs) throws SQLException, JsonProcessingException {
        ApplicationData applicationData = applicationDataEncryptor.decrypt(rs.getBytes("encrypted_data"));
        PGobject json = new PGobject();
        json.setType("jsonb");
        json.setValue(applicationDataEncryptor.encrypt(applicationData));
        return json;
    }

    protected static class ApplicationDataEncryptor {
        private final ObjectMapper objectMapper;
        private final Aead aead;

        public ApplicationDataEncryptor(ObjectMapper objectMapper, String encryptionKey) throws GeneralSecurityException, IOException {
            this.objectMapper = objectMapper;
            AeadConfig.register();
            aead = CleartextKeysetHandle.read(
                    JsonKeysetReader.withString(encryptionKey)).getPrimitive(Aead.class);
        }


        public String encryptString(String data) {
            try {
                return new String(Hex.encode(aead.encrypt(data.getBytes(), null)));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        public ApplicationData decrypt(byte[] encryptedData) throws JsonProcessingException {
            try {
                String result = new String(aead.decrypt(encryptedData, null));
                return objectMapper.readValue(result, ApplicationData.class);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        public String encrypt(ApplicationData applicationData) {
            try {
                String applicantSSN = applicationData.getPagesData().getPageInputFirstValue("personalInfo", "ssn");
                if (applicantSSN != null) {
                    String encryptedApplicantSSN = encryptString(applicantSSN);
                    applicationData.getPagesData().getPage("personalInfo").get("ssn").setValue(encryptedApplicantSSN, 0);

                    boolean hasHousehold = applicationData.getSubworkflows().containsKey("household");
                    if (hasHousehold) {
                        applicationData.getSubworkflows().get("household").forEach(iteration -> {
                            String houseHoldMemberSSN = iteration.getPagesData().getPageInputFirstValue("householdMemberInfo", "ssn");
                            if (houseHoldMemberSSN != null) {
                                String encryptedHouseholdMemberSSN = encryptString(houseHoldMemberSSN);
                                iteration.getPagesData().getPage("householdMemberInfo").get("ssn").setValue(encryptedHouseholdMemberSSN, 0);
                            }
                        });
                    }
                }
                return objectMapper.writeValueAsString(applicationData);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to encrypt application. application_id=" + applicationData.getId(), e);
            }
        }
    }
}