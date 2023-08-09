package org.codeforamerica.shiba.mnit;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Before Spring Boot 3, this class originally extends RetryListenerSupport, but RetryListenerSupport
 * has been deprecated in favor of the default implementations in {@link RetryListener}
 * Apparently the only reason this class exists is to log retry attempts.
 */

@Slf4j
@Component
public class EsbRetryListener implements RetryListener{

  private final String maxAttempts;

  public EsbRetryListener(@Value("${mnit-filenet.max-attempts}") String maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  @Override
  public <T, E extends Throwable> void close(RetryContext context,
      RetryCallback<T, E> callback, Throwable throwable) {
    log.info("ESB Retry closing for: " + MDC.get("applicationFile") + ", Retries Attempted: {} ",
        context.getRetryCount());
  }

  @Override
  public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
      Throwable throwable) {
    if (context.getRetryCount() < Integer.parseInt(maxAttempts)) {
      log.info("ESB Retry Unsuccessful for: " + MDC.get("applicationFile")
          + ", trying again. Retries Attempted: {} ", context.getRetryCount());
    } else {
      log.info(
          "ESB Retry Unsuccessful for: " + MDC.get("applicationFile") + ". Retries Attempted: {} ",
          context.getRetryCount());
    }
  }
}
