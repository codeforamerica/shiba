package org.codeforamerica.shiba;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.MaskedSerializer;
import org.codeforamerica.shiba.pages.data.PageData;
import org.slf4j.MDC;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * SessionLogFilter writes a number of things to the Sentry logs:
 * applicationId, clientIP, sessionId, httpRequestURL, email of user who is logged in with OAuth2
 */
@Component
@Slf4j
public class SessionLogFilter implements Filter {

  private final MonitoringService monitoringService;
  // ORIGINAL COMMENT:
  // Note that this object mapper cannot be autowired as we register a custom serializer with it that
  // we do not want to use in the rest of the app
  private final ObjectMapper objectMapper = new ObjectMapper();
  private ApplicationData applicationData;
  private String clientIP;

  public SessionLogFilter(MonitoringService monitoringService) {
	  //MonitoringService class type: org.codeforamerica.shiba.SentryClient
    this.monitoringService = monitoringService;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
	  // filterConfig class type is a org.apache.catalina.core.ApplicationFilterConfig
    applicationData = (ApplicationData) WebApplicationContextUtils.
        getRequiredWebApplicationContext(filterConfig.getServletContext())
        .getBean("applicationData");
    SimpleModule mod = new SimpleModule();
    mod.addSerializer(PageData.class, new MaskedSerializer());
    objectMapper.registerModule(mod);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest httpReq = (HttpServletRequest) request;
    //ORIGINAL COMMENT: Don't create a new session in logging and create a "unique" id for expired sessions for tracking
    String sessionId = httpReq.getSession(false) != null ?
            httpReq.getSession(false).getId() :  "expired : " + System.currentTimeMillis();
	String url = String.valueOf(httpReq.getRequestURL());
	if(!url.endsWith("navigation")) {
		MDC.put("url", url);
	}
    
    MDC.put("sessionId", sessionId);
    clientIP = createRequestIp(httpReq);
    if (applicationData != null && applicationData.getClientIP() == null) {
    	applicationData.setClientIP(clientIP);
    }
    MDC.put("ip", clientIP);
    if (applicationData != null && applicationData.getId() != null) {
      monitoringService.setApplicationId(applicationData.getId());
      MDC.put("applicationId", applicationData.getId());
    }
    if (httpReq.getUserPrincipal() instanceof OAuth2AuthenticationToken token) {
      String email = token.getPrincipal().getAttribute("email");
      MDC.put("admin", email);
    } else {
      MDC.remove("admin");
    }
    log.info(httpReq.getMethod() + " " + httpReq.getRequestURI());
    monitoringService.setSessionId(sessionId);

    chain.doFilter(request, response);
    MDC.clear();
  }

  private String createRequestIp(HttpServletRequest request) {
	// Note: X-RDWR-IP header will only be present when Radware is monitoring (ATST and Production)
	String clientIp = Optional.ofNullable(request.getHeader("X-RDWR-IP")).orElse("");
	clientIp = clientIp.trim();
    if (clientIp.isBlank()) {  // No Radware? this might get us something.
		clientIp = request.getRemoteAddr();
	}
	return clientIp;
  }
}
