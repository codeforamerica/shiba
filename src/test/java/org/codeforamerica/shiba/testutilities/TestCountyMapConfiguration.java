package org.codeforamerica.shiba.testutilities;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

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
                "some-folder-id", "some-dhs-provider-id", "someemail", "somephone"
        ));
        return mnitCountyInformationCountyMap;
    }
}
