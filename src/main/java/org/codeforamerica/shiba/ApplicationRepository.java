package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Random;

@Repository
public class ApplicationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Encryptor<ApplicationData> encryptor;
    private final ApplicationFactory applicationFactory;

    public ApplicationRepository(JdbcTemplate jdbcTemplate,
                                 Encryptor<ApplicationData> encryptor,
                                 ApplicationFactory applicationFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.encryptor = encryptor;
        this.applicationFactory = applicationFactory;
    }

    @SuppressWarnings("ConstantConditions")
    public String getNextId() {
        int random3DigitNumber = new Random().nextInt(900) + 100;

        String id = jdbcTemplate.queryForObject("SELECT nextval('application_id');", String.class);
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
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("applications");
        jdbcInsert.execute(Map.of(
                "id", application.getId(),
                "completed_at", Timestamp.from(application.getCompletedAt().toInstant()),
                "encrypted_data", encryptor.encrypt(application.getApplicationData()),
                "county", application.getCounty().name()
        ));
    }

    public Application find(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM applications WHERE id = ?",
                (resultSet, rowNum) -> applicationFactory.reconstitueApplication(
                        id,
                        ZonedDateTime.ofInstant(resultSet.getTimestamp("completed_at").toInstant(), ZoneOffset.UTC),
                        encryptor.decrypt(resultSet.getBytes("encrypted_data")),
                        County.valueOf(resultSet.getString("county"))),
                id);
    }
}