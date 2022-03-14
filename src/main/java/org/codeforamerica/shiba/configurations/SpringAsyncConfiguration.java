package org.codeforamerica.shiba.configurations;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class SpringAsyncConfiguration implements AsyncConfigurer {

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    // keep the queue small to avoid excessive delay if there are ESB retries
    executor.setQueueCapacity(2);
    executor.setWaitForTasksToCompleteOnShutdown(true); // avoid interruptionException
    executor.setThreadNamePrefix("AsyncExecutor-");
    executor.initialize();
    return executor;
  }
}
