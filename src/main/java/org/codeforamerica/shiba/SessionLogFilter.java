package org.codeforamerica.shiba;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class SessionLogFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        MDC.put("url", String.valueOf(httpReq.getRequestURL()));
        MDC.put("sessionId", httpReq.getSession().getId());
        log.info("Request Log");
        chain.doFilter(request, response);
    }
}
