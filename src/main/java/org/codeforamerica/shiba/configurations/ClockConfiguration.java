package org.codeforamerica.shiba.configurations;

import static java.time.Clock.systemUTC;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfiguration {

  @Bean
  Clock clock() {
    return systemUTC();
  }
}
