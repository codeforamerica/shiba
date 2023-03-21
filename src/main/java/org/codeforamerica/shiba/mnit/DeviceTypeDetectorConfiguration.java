package org.codeforamerica.shiba.mnit;

import org.codeforamerica.shiba.OpenDDRDeviceResolverHandlerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class DeviceTypeDetectorConfiguration  implements WebMvcConfigurer{
	public void addInterceptors(InterceptorRegistry registry) {
	    registry.addInterceptor(new OpenDDRDeviceResolverHandlerInterceptor()).addPathPatterns("/submit");
	}
}
