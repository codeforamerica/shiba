package org.codeforamerica.shiba;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.slf4j.MDC;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class SessionLogFilter implements Filter {
    ApplicationData applicationData;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        applicationData = (ApplicationData) WebApplicationContextUtils.
                getRequiredWebApplicationContext(filterConfig.getServletContext()).getBean("applicationData");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        MDC.put("url", String.valueOf(httpReq.getRequestURL()));
        MDC.put("sessionId", httpReq.getSession().getId());
        MDC.put("pagesData", new ObjectMapper().writeValueAsString(applicationData.getPagesData()));
        log.info(httpReq.getMethod() + " " + httpReq.getRequestURI());
        chain.doFilter(request, response);
    }
}
