package org.codeforamerica.shiba;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "12h")
@EnableTransactionManagement
public class ShibaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ShibaApplication.class, args);
  }
}
