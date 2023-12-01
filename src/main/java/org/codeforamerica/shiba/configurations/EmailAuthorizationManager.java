package org.codeforamerica.shiba.configurations;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to authorize people who can download applications with the /download endpoint.
 */
@Slf4j
@Configuration
public class EmailAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext>  {
	
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
			public AuthorizationDecision check(Supplier<Authentication> authentication,
					RequestAuthorizationContext object) {
				Object authenicationObject = authentication.get();
				if (!(authenicationObject instanceof OAuth2AuthenticationToken)) {
					return new AuthorizationDecision(false);
				}

				var principal = ((OAuth2AuthenticationToken) authenicationObject).getPrincipal();
				var email = principal.getAttribute("email");

				boolean isAuthorized = email != null && ADMIN_EMAILS.contains(email.toString().toLowerCase());

				if (isAuthorized) {
					log.info(String.format("Admin login for %s is authorized", email));
				} else {
					log.warn(String.format("Admin login for %s is not authorized", email));
				}
				return new AuthorizationDecision(isAuthorized);
			}

		}
