package org.codeforamerica.shiba;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ApplicationRepository {
    private final JdbcTemplate jdbcTemplate;

    public ApplicationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Integer getNextId() {
        this.jdbcTemplate.update("UPDATE application_id_counter SET counter = counter + 1;");
        return this.jdbcTemplate.queryForObject("SELECT counter FROM application_id_counter;", Integer.class);
    }
}
