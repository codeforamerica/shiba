package org.codeforamerica.shiba.mnit;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EsbRetryListener extends RetryListenerSupport {

  private final String maxAttempts;

  public EsbRetryListener(@Value("${mnit-esb.max-attempts}") String maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  @Override
  public <T, E extends Throwable> void close(RetryContext context,
      RetryCallback<T, E> callback, Throwable throwable) {
    log.info("ESB Retry closing for: " + MDC.get("applicationFile") + ", Retries Attempted: {} ",
        context.getRetryCount());
    super.close(context, callback, throwable);
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

    super.onError(context, callback, throwable);
  }
}
