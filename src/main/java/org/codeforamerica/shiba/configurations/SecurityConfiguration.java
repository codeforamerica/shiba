package org.codeforamerica.shiba.configurations;

import java.io.IOException;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SecurityConfiguration {
	
	private static int environmentUrlLength;
	
	public SecurityConfiguration(@Value("${mnbenefits_env_url}") String environmentUrl) {
		environmentUrlLength = environmentUrl.length();
	}

	@Autowired
	private ShibaInvalidSessionStrategy shibaInvalidSessionStrategy;
	
	@Autowired 
	private HandlerMappingIntrospector handlerMappingIntrospector;
	
	@Bean
	public MvcRequestMatcher.Builder mvc() {
		return new MvcRequestMatcher.Builder(handlerMappingIntrospector);
	}

	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfiguration) throws Exception {
		return authConfiguration.getAuthenticationManager();
	}
	 
	@Bean
	@SuppressWarnings("removal")
	@Order(1)
	public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
		EmailAuthorizationManager eam = new EmailAuthorizationManager();

		http.authorizeHttpRequests(r -> {
			r.requestMatchers(mvc.pattern("/pages/*"),
					mvc.pattern("/groups/*"),
					mvc.pattern("/document-upload"),
					mvc.pattern("/remove-upload/*"),
					mvc.pattern("/submit"),
					mvc.pattern("/submit-feedback"),
					mvc.pattern("/submit-documents")
					)
			.permitAll();
			try {
				r.requestMatchers(mvc.pattern("/download/??????????"),
						mvc.pattern("/resend-confirmation-email/??????????"))
				.access(eam)
				.anyRequest()
				.permitAll()
				.and()
				.oauth2Login();
			} catch (Exception e) {
				log.error("OAuth2 Error", e);
			}

		});

		http
		.headers()
		.httpStrictTransportSecurity()
		.requestMatcher(AnyRequestMatcher.INSTANCE)
		.includeSubDomains(true)
		.maxAgeInSeconds(31536000)
		.preload(true);

		http.sessionManagement(session -> session.invalidSessionStrategy(this.shibaInvalidSessionStrategy));
		http.sessionManagement(management -> management.sessionConcurrency(
				concurrency -> concurrency.expiredSessionStrategy(this.shibaInvalidSessionStrategy)));
		return http.build();
	}

	@Component
	public static class ShibaInvalidSessionStrategy
			implements InvalidSessionStrategy, SessionInformationExpiredStrategy {
		final private SimpleRedirectInvalidSessionStrategy errorRedirectInvalidSessionStrategy;

		public ShibaInvalidSessionStrategy(@Value("${server.servlet.session.timeout-url}") String timeoutUrl) {
			errorRedirectInvalidSessionStrategy = new SimpleRedirectInvalidSessionStrategy(timeoutUrl);
		}

		/**
		 * Ignore session expiration on the landing page.
		 */
		@Override
		public void onInvalidSessionDetected(HttpServletRequest request, HttpServletResponse response)
				throws IOException {
			String pageNameFromRequest = request.getRequestURL().toString();
			String pageName = pageNameFromRequest.substring(environmentUrlLength);
			log.info(StringEscapeUtils.escapeJava("User session invalid on page: " + pageName));
			if (pageName.equalsIgnoreCase("/pages/landing/navigation")) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/identifyCountyBeforeApplying");
				try {
					dispatcher.forward(request, response);
				} catch (ServletException | IOException e) {
					log.error("Invalid Session Redirect for " + pageName, e);
				}

			} else if (pageName.equalsIgnoreCase("/pages/landing")) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/identifyCountyBeforeApplying");
				try {
					dispatcher.forward(request, response);
				} catch (ServletException | IOException e) {
					log.error("Invalid Session Redirect for " + pageName, e);
				}

			} else if (pageName.equalsIgnoreCase("/")) {
				// language was changed on landing page so stay there
				RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/landing");
				try {
					dispatcher.forward(request, response);
				} catch (ServletException | IOException e) {
					log.error("Invalid Session Redirect for " + pageName, e);
				}

			} else if (pageName.contains("identifyCountyOrTribalNation")) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/identifyCountyOrTribalNation");
				try {
					dispatcher.forward(request, response);
				} catch (ServletException | IOException e) {
					log.error("Invalid Session Redirect for " + pageName, e);
				}

			} else if (pageName.contains("faq")) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/faq");
				try {
					dispatcher.forward(request, response);
				} catch (ServletException | IOException e) {
					log.error("Invalid Session Redirect for " + pageName, e);
				}

			} else if (pageName.contains("snapNDS")) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/snapNDS");
				try {
					dispatcher.forward(request, response);
				} catch (ServletException | IOException e) {
					log.error("Invalid Session Redirect for " + pageName, e);
				}

			} else if (pageName.contains("privacy")) {
				RequestDispatcher dispatcher = request.getRequestDispatcher("/privacy");
				try {
					dispatcher.forward(request, response);
				} catch (ServletException | IOException e) {
					log.error("Invalid Session Redirect for " + pageName, e);
				}

			} else {
				//normal redirect to session expired page
				errorRedirectInvalidSessionStrategy.onInvalidSessionDetected(request, response);
			}
		}

		@Override
		public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException {
			log.info("User session timed out on page: " + event.getRequest().getRequestURL());
			errorRedirectInvalidSessionStrategy.onInvalidSessionDetected(event.getRequest(), event.getResponse());
		}
	}
	
}