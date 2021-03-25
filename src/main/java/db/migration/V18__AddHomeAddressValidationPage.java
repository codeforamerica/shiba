package db.migration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.aead.AeadConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.codeforamerica.shiba.application.FlowType;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jetbrains.annotations.NotNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;


/**
 * Migration file to add home address validation page data
 * <p>
 * Process:
 * 1. Pull all rows from applications table
 * 2. Pull out encryptedData column
 * 3. Decrypt encryptedData column
 * 4. Create representation of ApplicationData to cast decrypted 'encryptedData' column
 * 4. Add in homeAddressValidation (blank) to PagesData
 * 5. Encrypt data
 * 6. Save to encryptedData column based on id
 **/

public class V18__AddHomeAddressValidationPage extends BaseJavaMigration {
    public static final String HOME_ADDRESS_VALIDATION = "homeAddressValidation";

    @Override
    public void migrate(Context context) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        StringEncryptor stringEncryptor = new StringEncryptor(System.getenv("ENCRYPTION_KEY"));
        SqlParameterSource[] sqlParameterSources = jdbcTemplate.query(
                "SELECT id, encrypted_data FROM applications WHERE encrypted_data IS NOT NULL ",
                (rs, n) -> new MapSqlParameterSource("encryptedData", addMissingPageAndReturnEncryptedData(rs, stringEncryptor))
                        .addValue("id", rs.getString("id"))
        ).toArray(SqlParameterSource[]::new);

        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        namedParameterJdbcTemplate.batchUpdate("UPDATE applications " +
                "SET encrypted_data = :encryptedData " +
                "WHERE id = :id", sqlParameterSources);
    }

    private byte[] addMissingPageAndReturnEncryptedData(ResultSet result, StringEncryptor stringEncryptor) throws SQLException {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ApplicationDataEncryptor encryptor = new ApplicationDataEncryptor(objectMapper, stringEncryptor);
        byte[] encryptedData = result.getBytes("encrypted_data");
        V18ApplicationData applicationData = encryptor.decrypt(encryptedData);
        PagesData pagesData = applicationData.getPagesData();

        if (pagesData.get(HOME_ADDRESS_VALIDATION) == null) {
            PageData pageData = new PageData();
            pageData.put("useEnrichedAddress", new InputData(List.of("false")));
            pagesData.put(HOME_ADDRESS_VALIDATION, pageData);
            return encryptor.encrypt(applicationData);
        } else {
            return encryptedData;
        }

    }

    @Data
    @NoArgsConstructor
    protected static class V18ApplicationData {
        private PagesData pagesData = new PagesData();
        private Subworkflows subworkflows = new Subworkflows();
        private FlowType flow = null;
        private String id;
        private Instant startTime;
        @JsonIgnore
        private HashMap<String, PagesData> incompleteIterations;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    protected static class PagesData extends HashMap<String, PageData> {
    }

    protected static class Subworkflows extends HashMap<String, Subworkflow> {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    protected static class Subworkflow extends ArrayList<PagesData> {
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    @NoArgsConstructor
    protected static class PageData extends HashMap<String, InputData> {
    }

    @Value
    protected static class InputData {
        @NotNull List<String> value;
        @JsonIgnore
        List<String> validators;
        @JsonIgnore
        Boolean valid;

        InputData(List<String> value, List<String> validators) {
            this.value = Objects.requireNonNullElseGet(value, List::of);
            this.validators = validators;
            this.valid = null;
        }

        InputData() {
            this(emptyList(), emptyList());
        }

        InputData(List<String> value, Boolean valid) {
            this.value = Objects.requireNonNullElseGet(value, List::of);
            this.validators = emptyList();
            this.valid = valid;
        }

        InputData(List<String> value) {
            this.value = Objects.requireNonNullElseGet(value, List::of);
            this.validators = emptyList();
            this.valid = null;
        }
    }

    protected static class ApplicationDataEncryptor {
        private final ObjectMapper objectMapper;
        private final StringEncryptor stringEncryptor;

        public ApplicationDataEncryptor(ObjectMapper objectMapper, StringEncryptor stringEncryptor) {
            this.objectMapper = objectMapper;
            this.stringEncryptor = stringEncryptor;
        }

        public V18ApplicationData decrypt(byte[] encryptedData) {
            try {
                return objectMapper.readValue(stringEncryptor.decrypt(encryptedData), V18ApplicationData.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public byte[] encrypt(V18ApplicationData data) {
            try {
                return stringEncryptor.encrypt(objectMapper.writeValueAsString(data));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static class StringEncryptor {
        private final Aead aead;

        public StringEncryptor(String encryptionKey) throws GeneralSecurityException, IOException {
            AeadConfig.register();
            aead = CleartextKeysetHandle.read(
                    JsonKeysetReader.withString(encryptionKey)).getPrimitive(Aead.class);
        }

        public byte[] encrypt(String data) {
            try {
                return aead.encrypt(data.getBytes(), null);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        public String decrypt(byte[] encryptedData) {
            try {
                return new String(aead.decrypt(encryptedData, null));
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
