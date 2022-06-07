package org.codeforamerica.shiba.application;

import lombok.Data;
import org.codeforamerica.shiba.output.Document;

@Data
public class ApplicationStatus {

  private String applicationId;
  private Document documentType;
  private String routingDestinationName;
  private Status status;
  private String documentName;
  private String filenetId;

  public ApplicationStatus(String applicationId, Document documentType,
      String routingDestinationName, Status status, String documentName) {
    this.applicationId = applicationId;
    this.documentType = documentType;
    this.routingDestinationName = routingDestinationName;
    this.status = status;
    this.documentName = documentName;
  }

  public ApplicationStatus(String applicationId, Document documentType,
      String routingDestinationName, Status status, String documentName, String filenetId) {
    this.applicationId = applicationId;
    this.documentType = documentType;
    this.routingDestinationName = routingDestinationName;
    this.status = status;
    this.documentName = documentName;
    this.filenetId = filenetId;
  }
}
