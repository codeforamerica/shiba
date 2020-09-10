package db.migration;

import org.codeforamerica.shiba.StringEncryptor;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class V9__EncryptExistingData extends BaseJavaMigration {
    public void migrate(Context context) throws GeneralSecurityException, IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        StringEncryptor stringEncryptor = new StringEncryptor(System.getenv("ENCRYPTION_KEY"));
        SqlParameterSource[] sqlParameterSources = jdbcTemplate.query(
                "SELECT id, data " +
                        "FROM applications " +
                        "WHERE data IS NOT NULL " +
                        "AND encrypted_data IS NULL", (rs, rowNum) ->
                new MapSqlParameterSource("encryptedData", stringEncryptor.encrypt(rs.getString("data")))
                        .addValue("id", rs.getString("id")))
                .toArray(SqlParameterSource[]::new);
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.batchUpdate(
                "UPDATE applications " +
                        "SET encrypted_data = :encryptedData " +
                        "WHERE id = :id", sqlParameterSources);
    }
}
