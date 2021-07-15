package org.codeforamerica.shiba.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CityInfoConfigurationFactoryAppConfig {

    @Bean
    public CityInfoConfigurationFactory cityInfoConfigurationFactory() {
        return new CityInfoConfigurationFactory();
    }

}
