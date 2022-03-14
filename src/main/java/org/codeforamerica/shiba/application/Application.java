package org.codeforamerica.shiba.application;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.Feedback;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.util.StringUtils;

@Data
@Builder
public class Application {

  private String id;
  private ZonedDateTime completedAt;
  private ZonedDateTime updatedAt;
  private ApplicationData applicationData;
  private County county;
  private Duration timeToComplete;
  private FlowType flow;
  private Sentiment sentiment;
  private String feedback;
  private Status docUploadEmailStatus;
  private List<DocumentStatus> documentStatuses;

  public Application addFeedback(Feedback feedback) {
    var sentiment = Optional.ofNullable(feedback.sentiment()).orElse(this.sentiment);
    var feedbackText =
        StringUtils.hasLength(feedback.feedback()) ? feedback.feedback() : this.feedback;
    return new Application(
        id,
        completedAt,
        updatedAt,
        applicationData,
        county,
        timeToComplete,
        flow,
        sentiment,
        feedbackText,
        docUploadEmailStatus,
        documentStatuses
    );
  }

  public void setCompletedAtTime(Clock clock) {
    completedAt = ZonedDateTime.now(clock);
    setTimeToComplete(Duration.between(applicationData.getStartTime(), completedAt));
  }

  public Status getApplicationStatus(Document document, String routingDestination) {
    if (documentStatuses != null) {
      return documentStatuses.stream()
          .filter(appStatus -> appStatus.getDocumentType() == document
              && (appStatus.getRoutingDestinationName() == null
              || appStatus.getRoutingDestinationName()
              .equals(routingDestination)))
          .findFirst()
          .map(DocumentStatus::getStatus).orElse(null);
    }
    return null;
  }
}
