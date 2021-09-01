package org.codeforamerica.shiba.testutilities;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/*
 * An ApplicationData instance which is not tied to a session
 *
 * If you use this, it is necessary to call TestUtils.resetApplicationData(applicationData) in the
 * @AfterEach to avoid test pollution
 */
@TestConfiguration
public class NonSessionScopedApplicationData {

  @Bean
  public ApplicationData applicationData() {
    return new ApplicationData();
  }
}
