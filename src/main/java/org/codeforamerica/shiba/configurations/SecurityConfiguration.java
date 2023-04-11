package org.codeforamerica.shiba.configurations;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;
import org.springframework.stereotype.Component;


@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private ShibaInvalidSessionStrategy shibaInvalidSessionStrategy;

  public static final List<String> ADMIN_EMAILS = List.of(
      "john.bisek@state.mn.us",
      "eric.m.johnson@state.mn.us",
      "taylor.johnson@state.mn.us",
      "touhid.khan@state.mn.us",
      "william.prew@state.mn.us",
      "ramesh.shakya@state.mn.us",
      "marlene.merwarth@state.mn.us",
      "ryan.b.smith@state.mn.us",
      "michael.hauck@state.mn.us",
      "bernadette.shearer@state.mn.us"
  );

  @Override
  protected AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }


  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests(r ->
            r.antMatchers(
                    "/download/??????????",
                    "/resend-confirmation-email/??????????")
                .access("isAuthenticated() and @emailBasedAccessDecider.check(authentication)"))
        .oauth2Login();

    http.headers()
        .httpStrictTransportSecurity()
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000);

    http.sessionManagement(session -> session.invalidSessionStrategy(this.shibaInvalidSessionStrategy));
    http.sessionManagement().sessionConcurrency(concurrency -> concurrency.expiredSessionStrategy(this.shibaInvalidSessionStrategy));
  }

  @Bean
  public EmailBasedAccessDecider emailBasedAccessDecider() {
    return new EmailBasedAccessDecider();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  public static class EmailBasedAccessDecider {

    public boolean check(Authentication authentication) {
      
      var principal = ((OAuth2AuthenticationToken) authentication).getPrincipal();
      var email = principal.getAttributes().get("email");
      boolean isAuthorized = email != null && ADMIN_EMAILS.contains(email.toString().toLowerCase());
      
      if (isAuthorized) {
        log.info(String.format("Admin login for %s is authorized", email));
      } else {
        log.warn(String.format("Admin login for %s is not authorized", email));
      }
      return isAuthorized;
    }
  }


  @Component
  public static class ShibaInvalidSessionStrategy implements InvalidSessionStrategy, SessionInformationExpiredStrategy {
    final private SimpleRedirectInvalidSessionStrategy errorRedirectInvalidSessionStrategy;

    public ShibaInvalidSessionStrategy(@Value("${server.servlet.session.timeout-url}") String timeoutUrl) {
      errorRedirectInvalidSessionStrategy = new SimpleRedirectInvalidSessionStrategy(timeoutUrl);
    }

    @Override
    public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response) throws IOException {
      log.info("User session invalid on page: " + request.getRequestURL());
      errorRedirectInvalidSessionStrategy.onInvalidSessionDetected(request, response);
    }

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
      log.info("User session timed out on page: " + event.getRequest().getRequestURL());
      errorRedirectInvalidSessionStrategy.onInvalidSessionDetected(event.getRequest(), event.getResponse());
    }
  }

}
