package org.codeforamerica.shiba;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.StaticMessageSource;

class StaticMessageSourceConfiguration {
    @Bean
    public MessageSource messageSource() {
        return new StaticMessageSource();
    }
}
