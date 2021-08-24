package org.codeforamerica.shiba.testutilities;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import org.codeforamerica.shiba.application.ApplicationDataEncryptor;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.StringEncryptor;
import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("test")
@Tag("db")
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@Sql(statements = {"ALTER SEQUENCE application_id RESTART WITH 12", "TRUNCATE TABLE applications"})
@ContextConfiguration(classes = {
    NonSessionScopedApplicationData.class,
    ApplicationDataEncryptor.class,
    StringEncryptor.class,
    ApplicationRepository.class})
@AutoConfigureJson
public class AbstractRepositoryTest {

}
