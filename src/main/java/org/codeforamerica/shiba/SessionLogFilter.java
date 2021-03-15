package org.codeforamerica.shiba;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.sentry.Sentry;
import io.sentry.protocol.User;
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
        MDC.put("pagesData", objectMapper.writeValueAsString(applicationData.getPagesData()));
        log.info(httpReq.getMethod() + " " + httpReq.getRequestURI());

        User user = new User();
        user.setId(httpReq.getSession().getId());
        Sentry.setExtra("pagesData", MDC.get("pagesData"));
        Sentry.setUser(user);

        if (applicationData.getId() != null) {
            monitoringService.setApplicationId(applicationData.getId());
        }

        chain.doFilter(request, response);
    }
}
