package db.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.aead.AeadConfig;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.codeforamerica.shiba.application.Encryptor;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

@Slf4j
public class V26__UnencryptBlankSSN extends BaseJavaMigration {
	ObjectMapper objectMapper;
	ApplicationDataEncryptor applicationDataEncryptor;

	public void migrate(Context context) throws GeneralSecurityException, IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        applicationDataEncryptor = new ApplicationDataEncryptor(objectMapper, new StringEncryptor(System.getenv("ENCRYPTION_KEY")));
        SqlParameterSource[] sqlParameterSources = jdbcTemplate.query(
                "SELECT id, application_data " +
                        "FROM applications " +
                        "WHERE application_data IS NOT NULL ", (rs, rowNum) ->
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
		ApplicationData applicationData = applicationDataEncryptor.decrypt(rs.getString("application_data"));
		PGobject json = new PGobject();
		json.setType("jsonb");
		json.setValue(applicationDataEncryptor.encrypt(applicationData));
		return json;
	}

	public class ApplicationDataEncryptor {
		private final ObjectMapper objectMapper;
		private final Encryptor<String> stringEncryptor;

		public ApplicationDataEncryptor(ObjectMapper objectMapper, Encryptor<String> stringEncryptor) {
			this.objectMapper = objectMapper;
			this.stringEncryptor = stringEncryptor;
		}

		public String encrypt(ApplicationData applicationData) {
			try {
				runCryptographicFunctionOnData(stringEncryptor::encrypt, applicationData);
				return objectMapper.writeValueAsString(applicationData);
			} catch (JsonProcessingException e) {
				log.error("Unable to encrypt application data: applicationID=" + applicationData.getId());
				throw new RuntimeException(e);
			}
		}

		public ApplicationData decrypt(String encryptedData) {
			try {
				ApplicationData applicationData = objectMapper.readValue(encryptedData, ApplicationData.class);
				runCryptographicFunctionOnData(stringEncryptor::decrypt, applicationData);
				return applicationData;
			} catch (IOException e) {
				log.error("Error while deserializing application data");
				throw new RuntimeException(e);
			}
		}

		private void runCryptographicFunctionOnData(Function<String, String> encryptFunc,
				ApplicationData applicationData) {
			String applicantSSN = applicationData.getPagesData().getPageInputFirstValue("personalInfo", "ssn");
			if (applicantSSN.isBlank()) {
				log.info("SSN is blank, applicationID=" + applicationData.getId());
			}
			if (applicantSSN != null && !applicantSSN.isBlank()) {
				String encryptedApplicantSSN = encryptFunc.apply(applicantSSN);
				applicationData.getPagesData().getPage("personalInfo").get("ssn").setValue(encryptedApplicantSSN, 0);
			}

			boolean hasHousehold = applicationData.getSubworkflows().containsKey("household");
			if (hasHousehold) {
				applicationData.getSubworkflows().get("household").forEach(iteration -> {
					String houseHoldMemberSSN = iteration.getPagesData().getPageInputFirstValue("householdMemberInfo",
							"ssn");
					if (houseHoldMemberSSN != null && !houseHoldMemberSSN.isBlank()) {
						String encryptedHouseholdMemberSSN = encryptFunc.apply(houseHoldMemberSSN);
						iteration.getPagesData().getPage("householdMemberInfo").get("ssn")
								.setValue(encryptedHouseholdMemberSSN, 0);
					}
				});
			}
		}

	}

	public class StringEncryptor implements Encryptor<String> {
	    private final Aead aead;

	    public StringEncryptor(@Value("${encryption-key}") String encryptionKey) throws GeneralSecurityException, IOException {
	        AeadConfig.register();
	        aead = CleartextKeysetHandle.read(
	                JsonKeysetReader.withString(encryptionKey)).getPrimitive(Aead.class);
	    }

	    public String encrypt(String data) {
	        try {
	            return new String(Hex.encodeHex(aead.encrypt(data.getBytes(), null)));
	        } catch (GeneralSecurityException e) {
	            throw new RuntimeException(e);
	        }
	    }

	    public String decrypt(String encryptedData) {
	        try {
	            return new String(aead.decrypt(Hex.decodeHex(encryptedData.toCharArray()), null));
	        } catch (GeneralSecurityException | DecoderException e) {
	            throw new RuntimeException(e);
	        }
	    }
	}

}
