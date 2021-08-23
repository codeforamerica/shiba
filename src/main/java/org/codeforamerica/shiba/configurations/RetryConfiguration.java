package org.codeforamerica.shiba.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@Configuration
public class RetryConfiguration {

} // Required for Spring Retry to be used anywhere
