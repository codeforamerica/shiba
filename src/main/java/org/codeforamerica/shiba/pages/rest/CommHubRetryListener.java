package org.codeforamerica.shiba.pages.rest;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Copied from EsbRetryListener to log retry attempts.
 */

@Slf4j
@Component
public class CommHubRetryListener implements RetryListener{

  private final String maxAttempts;

  public CommHubRetryListener(@Value("${comm-hub.max-attempts}") String maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  @Override
  public <T, E extends Throwable> void close(RetryContext context,
      RetryCallback<T, E> callback, Throwable throwable) {
    log.info("Comm Hub Text Retry closing for: " + MDC.get("applicationId") + ", Retries Attempted: {} ",
        context.getRetryCount());
  }

  @Override
  public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
      Throwable throwable) {
    if (context.getRetryCount() < Integer.parseInt(maxAttempts)) {
      log.info("Comm Hub Text Retry Unsuccessful for: " + MDC.get("applicationId")
          + ", trying again. Retries Attempted: {} ", context.getRetryCount());
    } else {
      log.info(
          "Comm Hub Text Retry Unsuccessful for: " + MDC.get("applicationId") + ". Retries Attempted: {} ",
          context.getRetryCount());
    }
  }
}
