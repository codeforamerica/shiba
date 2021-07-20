package org.codeforamerica.shiba.testutilities;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.StaticMessageSource;

@TestConfiguration
public class StaticMessageSourceConfiguration {
    @Bean
    public MessageSource messageSource() {
        return new StaticMessageSource();
    }
}
