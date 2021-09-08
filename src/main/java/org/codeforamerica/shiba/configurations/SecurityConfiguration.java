package org.codeforamerica.shiba.configurations;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  // TODO provide this list via environment variables or application.yaml config or something
  // Or maybe just say it's okay to redeploy every time a new teammate joins
  public static List<String> ADMIN_EMAILS = List.of(
      "aedstrom@codeforamerica.org",
      "sprasad@codeforamerica.org");

  @Override
  protected AuthenticationManager authenticationManager() throws Exception {
    return super.authenticationManager();
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth,
      @Value("${shiba.username}") String username,
      @Value("${shiba.password}") String password) throws Exception {
    auth.inMemoryAuthentication()
        .withUser(username).password(passwordEncoder().encode(password))
        .authorities("admin");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests(r ->
            r.antMatchers(
                    "/download-caf/**",
                    "/download-ccap/??????????",
                    "/download-docs/??????????",
                    "/metrics",
                    "/resend-confirmation-email/??????????")
                .access("isAuthenticated() and @emailBasedAccessDecider.check(authentication)")
        )
        .oauth2Login();
  }

  @Bean
  public EmailBasedAccessDecider emailBasedAccessDecider() {
    return new EmailBasedAccessDecider();
  }

  // TODO do we want this to be a static class?
  public static class EmailBasedAccessDecider {

    public boolean check(Authentication authentication) {
      var principal = ((OAuth2AuthenticationToken) authentication).getPrincipal();
      var email = ((DefaultOidcUser) principal).getIdToken().getClaims().get("email");
      return ADMIN_EMAILS.contains(email);
    }
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
