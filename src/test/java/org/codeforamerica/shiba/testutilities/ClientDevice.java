package org.codeforamerica.shiba.testutilities;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.mobile.device.DeviceType;
import org.springframework.mobile.device.LiteDevice;

@TestConfiguration
public class ClientDevice {
	
	@Bean
	public Device device() {
		return LiteDevice.from(DeviceType.MOBILE, DevicePlatform.ANDROID);
	}

}
