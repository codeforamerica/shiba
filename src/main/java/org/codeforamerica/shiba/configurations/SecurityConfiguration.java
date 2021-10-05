package org.codeforamerica.shiba.configurations;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
  public static final List<String> ADMIN_EMAILS = List.of(
      "aedstrom@codeforamerica.org",
      "agonzalez@codeforamerica.org",
      "ashukla@codeforamerica.org",
      "axie@codeforamerica.org",
      "bcalegari@codeforamerica.org",
      "bgarcia@codeforamerica.org",
      "bepps@codeforamerica.org",
      "cguilfoile@codeforamerica.org",
      "cparedes@codeforamerica.org",
      "cenyia@codeforamerica.org",
      "deirdre@codeforamerica.org",
      "dustin@codeforamerica.org",
      "edavis@codeforamerica.org",
      "eric@codeforamerica.org",
      "jazmyn@codeforamerica.org",
      "kerry@codeforamerica.org",
      "lhaynes@codeforamerica.org",
      "lmoore@codeforamerica.org",
      "lraymontanez@codeforamerica.org",
      "luigi@codeforamerica.org",
      "mloew@codeforamerica.org",
      "mrotondo@codeforamerica.org",
      "nmartinez@codeforamerica.org",
      "sgole@codeforamerica.org",
      "tpatterson@codeforamerica.org",
      "sprasad@codeforamerica.org"
  );

  @Override
  protected AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }


  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests(r ->
            r.antMatchers(
                    "/download-caf/??????????",
                    "/download-ccap/??????????",
                    "/download-docs/??????????",
                    "/metrics",
                    "/resend-confirmation-email/??????????")
                .access("isAuthenticated() and @emailBasedAccessDecider.check(authentication)"))
        .oauth2Login();

    http.headers()
        .httpStrictTransportSecurity()
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000);
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
      boolean isAuthorized = email != null && ADMIN_EMAILS.contains((String) email);
      log.info(String.format("Admin login for %s is %s", email, isAuthorized ? "authorized" : "not authorized"));
      return isAuthorized;
    }
  }
}
