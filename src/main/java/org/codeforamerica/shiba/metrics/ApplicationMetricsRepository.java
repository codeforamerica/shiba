package org.codeforamerica.shiba.metrics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Repository
public class ApplicationMetricsRepository {
    private final JdbcTemplate jdbcTemplate;
    SimpleJdbcInsert jdbcInsert;

    public ApplicationMetricsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("application_metrics");
    }

    public void save(ApplicationMetric applicationMetric) {
        jdbcInsert.execute(Map.of("time_to_complete", applicationMetric.getTimeToComplete().getSeconds()));
    }

    public Duration getMedianTimeToComplete() {
        long medianTimeToComplete = jdbcTemplate.queryForObject("SELECT COALESCE(percentile_cont(0.5) WITHIN GROUP (ORDER BY time_to_complete), 0) FROM application_metrics", Long.class);
        return Duration.ofSeconds(medianTimeToComplete);
    }

    public int count() {
        return jdbcTemplate.queryForObject("SELECT count(*) FROM application_metrics;", Integer.class);
    }

    public List<ApplicationMetric> findAll() {
        return jdbcTemplate.query("SELECT * FROM application_metrics;", (resultSet, rowNumber) -> new ApplicationMetric(Duration.of(resultSet.getLong("time_to_complete"), ChronoUnit.SECONDS)));
    }
}
