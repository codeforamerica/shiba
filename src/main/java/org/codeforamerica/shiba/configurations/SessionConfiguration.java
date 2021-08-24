package org.codeforamerica.shiba.configurations;

import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SessionConfiguration {

  @Bean
  @Scope(
      value = SCOPE_SESSION,
      proxyMode = TARGET_CLASS
  )
  public ApplicationData applicationData() {
    return new ApplicationData();
  }
}
