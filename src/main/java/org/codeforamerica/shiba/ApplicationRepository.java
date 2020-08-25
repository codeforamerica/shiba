package org.codeforamerica.shiba;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public ApplicationRepository(JdbcTemplate jdbcTemplate,
                                 ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
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
        try {
            jdbcInsert.execute(Map.of(
                    "id", application.getId(),
                    "completed_at", Timestamp.from(application.getCompletedAt().toInstant()),
                    "data", objectMapper.writeValueAsString(application.getApplicationData()),
                    "county", application.getCounty().name()
            ));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public Application find(String id) {
        return jdbcTemplate.queryForObject("SELECT * FROM applications WHERE id = ?", (resultSet, rowNum) -> {
            try {
                ApplicationData applicationData;
                applicationData = objectMapper.readValue(resultSet.getString("data"), ApplicationData.class);

                return new Application(
                        id,
                        ZonedDateTime.ofInstant(resultSet.getTimestamp("completed_at").toInstant(), ZoneOffset.UTC),
                        applicationData,
                        County.valueOf(resultSet.getString("county")));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, id);
    }
}