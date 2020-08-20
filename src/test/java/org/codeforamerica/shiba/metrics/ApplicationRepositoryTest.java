package org.codeforamerica.shiba.metrics;

import org.codeforamerica.shiba.ApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Sql(statements = "UPDATE application_id_counter SET counter = 0;")
class ApplicationRepositoryTest {

    @Autowired
    ApplicationRepository applicationRepository;

    @Test
    void shouldSaveApplicationMetrics() {
        Integer nextId = applicationRepository.getNextId();

        assertThat(nextId).isEqualTo(1);

        Integer nextIdAgain = applicationRepository.getNextId();

        assertThat(nextIdAgain).isEqualTo(2);
    }
}