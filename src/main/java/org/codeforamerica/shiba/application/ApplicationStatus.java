package org.codeforamerica.shiba.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.codeforamerica.shiba.output.Document;

@Data
@AllArgsConstructor
public class ApplicationStatus {

  private String applicationId;
  private Document documentType;
  private String routingDestinationName;
  private Status status;
  private String documentName;
}
