package org.codeforamerica.shiba.configurations;

import org.codeforamerica.shiba.SessionLogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  SessionLogFilter sessionLogFilter;

  public FilterConfig(SessionLogFilter sessionLogFilter) {
    this.sessionLogFilter = sessionLogFilter;
  }

  @Bean
  public FilterRegistrationBean<SessionLogFilter> loggingFilter() {
    FilterRegistrationBean<SessionLogFilter> registrationBean = new FilterRegistrationBean<>();

    registrationBean.setFilter(sessionLogFilter);
    registrationBean.addUrlPatterns(
        "/pages/*",
        "/groups/*",
        "/document-upload",
        "/remove-upload/*",
        "/healthcare-renewal-remove-upload/*",
        "/submit",
        "/submit-feedback",
        "/submit-documents",
        "/download/*"
    );

    return registrationBean;
  }
}
