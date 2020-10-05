package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.HashMap;

@Configuration
public class TestCountyMapConfiguration {

    @Bean
    @Profile("test")
    @ConfigurationProperties(prefix = "test")
    CountyMap<MnitCountyInformation> testMapping() {
        CountyMap<MnitCountyInformation> mnitCountyInformationCountyMap = new CountyMap<>();
        mnitCountyInformationCountyMap.setCounties(new HashMap<>());
        mnitCountyInformationCountyMap.setDefaultValue(new MnitCountyInformation(
                "some-folder-id", "some-dhs-provider-id", "someemail"
        ));
        return mnitCountyInformationCountyMap;
    }
}
