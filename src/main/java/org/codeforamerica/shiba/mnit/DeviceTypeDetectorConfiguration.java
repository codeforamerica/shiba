package org.codeforamerica.shiba.mnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.mobile.device.DeviceResolverHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class DeviceTypeDetectorConfiguration  implements WebMvcConfigurer{
	public void addInterceptors(InterceptorRegistry registry) {
	    registry.addInterceptor(new DeviceResolverHandlerInterceptor());
	}
}
