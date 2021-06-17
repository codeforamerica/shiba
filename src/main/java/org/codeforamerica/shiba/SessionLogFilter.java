package org.codeforamerica.shiba;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.MaskedSerializer;
import org.codeforamerica.shiba.pages.data.PageData;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Slf4j
public class SessionLogFilter implements Filter {
    private ApplicationData applicationData;
    private final MonitoringService monitoringService;
    // Note that this object mapper cannot be autowired as we register a custom serializer with it that
    // we do not want to use in the rest of the app
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SessionLogFilter(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        applicationData = (ApplicationData) WebApplicationContextUtils.
                getRequiredWebApplicationContext(filterConfig.getServletContext()).getBean("applicationData");
        SimpleModule mod = new SimpleModule();
        mod.addSerializer(PageData.class, new MaskedSerializer());
        objectMapper.registerModule(mod);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        MDC.put("url", String.valueOf(httpReq.getRequestURL()));
        MDC.put("sessionId", httpReq.getSession().getId());
        if (applicationData != null && applicationData.getId() != null) {
                monitoringService.setApplicationId(applicationData.getId());
                MDC.put("applicationId", applicationData.getId());
        }
        log.info(httpReq.getMethod() + " " + httpReq.getRequestURI());
        monitoringService.setSessionId(httpReq.getSession().getId());

        chain.doFilter(request, response);
    }
}
