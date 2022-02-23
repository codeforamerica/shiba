package org.codeforamerica.shiba.exception.controller;

import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.connector.ClientAbortException;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@ControllerAdvice
@Slf4j
public class ControllerAdvisor {
  
  private ApplicationData applicationData;

  public ControllerAdvisor(ApplicationData applicationData) {
    this.applicationData = applicationData;
  }

  @ExceptionHandler(ClientAbortException.class)
  public void handleClientAbortException(final ClientAbortException ex, final WebRequest request,
      final HttpServletRequest req) {
    log.info("Document Upload Cancelled by Client for application ID: " + applicationData.getId()
        + " and last viewed page is " + applicationData.getLastPageViewed());
  }


}
