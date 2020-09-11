package org.codeforamerica.shiba.pages;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestCountyEmailMapConfiguration {

    @Bean
    CountyEmailMap testMapping() {
        return new CountyEmailMap();
    }
}
