package org.codeforamerica.shiba;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.MaskedSerializer;
import org.codeforamerica.shiba.pages.data.PageData;
import org.slf4j.MDC;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Component
@Slf4j
public class SessionLogFilter implements Filter {

  private final MonitoringService monitoringService;
  // Note that this object mapper cannot be autowired as we register a custom serializer with it that
  // we do not want to use in the rest of the app
  private final ObjectMapper objectMapper = new ObjectMapper();
  private ApplicationData applicationData;
  private String clientIP;

  public SessionLogFilter(MonitoringService monitoringService) {
    this.monitoringService = monitoringService;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
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

    // Don't create a new session in logging and create a "unique" id for expired sessions for tracking
    String sessionId = httpReq.getSession(false) != null ?
            httpReq.getSession(false).getId() :  "expired : " + System.currentTimeMillis();

    MDC.put("url", String.valueOf(httpReq.getRequestURL()));
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
    String requestIpHeader = Optional.ofNullable(request.getHeader("X-RDWR-IP")).orElse("");
    String[] ipAddresses = requestIpHeader.split(",");
    return ipAddresses.length > 1 ? ipAddresses[ipAddresses.length - 2].trim()
        : request.getRemoteAddr();
  }
}
