package org.codeforamerica.shiba.testutilities;

import java.util.HashMap;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import mobi.openddr.classifier.model.Device;

@TestConfiguration
public class ClientDevice {
	
	@Bean
	public Device device() {
		HashMap<String,String> properties = new HashMap<String,String>();
		properties.put("id", "genericAndroid");
		properties.put("is_tablet", "false");
		properties.put("is_desktop", "false");
		properties.put("device_os", "Android");

		return new Device("genericAndroid", properties);
	}

}
