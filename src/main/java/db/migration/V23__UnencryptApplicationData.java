package db.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.codeforamerica.shiba.application.ApplicationDataEncryptor;
import org.codeforamerica.shiba.application.StringEncryptor;
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
    StringEncryptor stringEncryptor;
    ApplicationDataEncryptor applicationDataEncryptor;

    public void migrate(Context context) throws GeneralSecurityException, IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        stringEncryptor = new StringEncryptor(System.getenv("ENCRYPTION_KEY"));
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        applicationDataEncryptor = new ApplicationDataEncryptor(objectMapper, stringEncryptor);
        SqlParameterSource[] sqlParameterSources = jdbcTemplate.query(
                "SELECT id, encrypted_data " +
                        "FROM applications " +
                        "WHERE encrypted_data IS NOT NULL " +
                        "AND json_data IS NULL", (rs, rowNum) ->
                {
                    try {
                        return new MapSqlParameterSource("json_data", unencryptedAppDataWithEncryptedSSN(rs))
                                .addValue("id", rs.getString("id"));
                    } catch (Exception e) {
                        return new MapSqlParameterSource("json_data", "")
                                .addValue("id", rs.getString("id"));
                    }
                })
                .toArray(SqlParameterSource[]::new);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.batchUpdate(
                "UPDATE applications " +
                        "SET json_data = to_json(:json_data)" +
                        "WHERE id = :id", sqlParameterSources);
    }

    private PGobject unencryptedAppDataWithEncryptedSSN(ResultSet rs) throws SQLException, JsonProcessingException {
        ApplicationData applicationData = applicationDataEncryptor.decrypt(rs.getBytes("encrypted_data"));
        PGobject json = new PGobject();
        json.setType("jsonb");
        json.setValue(objectMapper.writeValueAsString(applicationData.encrypted()));
        return json;
    }
}