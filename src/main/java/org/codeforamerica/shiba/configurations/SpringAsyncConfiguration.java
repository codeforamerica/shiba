package org.codeforamerica.shiba.configurations;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class SpringAsyncConfiguration implements AsyncConfigurer {
  @Value("${asyncConfiguration.corePoolSize}") int corePoolSize;
  @Value("${asyncConfiguration.queueCapacity}") int queueCapacity;

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    // keep the queue small to avoid excessive delay if there are ESB retries
    executor.setQueueCapacity(queueCapacity );
    executor.setWaitForTasksToCompleteOnShutdown(true); // avoid interruptionException
    executor.setThreadNamePrefix("AsyncExecutor-");
    executor.initialize();
    return executor;
  }
}
