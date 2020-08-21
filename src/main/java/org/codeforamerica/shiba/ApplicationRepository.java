package org.codeforamerica.shiba;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Random;

@Repository
public class ApplicationRepository {
    private final JdbcTemplate jdbcTemplate;

    public ApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}