package org.codeforamerica.shiba.testutilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@TestConfiguration
public class WebDriverConfiguration {

  @Autowired
  private Path tempdir;

  @Bean(initMethod = "start", destroyMethod = "stop")
  @Scope("singleton")
  public SeleniumFactory seleniumComponent() {
    return new SeleniumFactory(tempdir);
  }

  @Bean
  public Path tempDir() throws IOException {
    return Files.createTempDirectory("");
  }
}
