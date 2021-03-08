package org.codeforamerica.shiba.configurations;

import org.codeforamerica.shiba.SessionLogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterRegistrationBean<SessionLogFilter> loggingFilter(){
        FilterRegistrationBean<SessionLogFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new SessionLogFilter());
        registrationBean.addUrlPatterns("/pages/*");

        return registrationBean;
    }
}
