package org.codeforamerica.shiba;

import mobi.openddr.classifier.model.Device;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mobi.openddr.classifier.Classifier;
import mobi.openddr.classifier.loader.LoaderOption;

public class OpenDDRDeviceResolverHandlerInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Classifier classifier = Classifier.builder().with(LoaderOption.JAR).build();
		String ua = request.getHeader("User-Agent");
		if (ua == null) ua=""; // when there is no User-Agent header default to an unknown device
		Device device = classifier.classifyDevice(ua);
		request.setAttribute("currentDevice", device);
		return true;
	}
}
