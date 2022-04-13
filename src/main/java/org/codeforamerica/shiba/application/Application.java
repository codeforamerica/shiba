package org.codeforamerica.shiba.application;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collections;
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
  private List<ApplicationStatus> applicationStatuses;

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
        applicationStatuses
    );
  }

  public void setCompletedAtTime(Clock clock) {
    completedAt = ZonedDateTime.now(clock);
    setTimeToComplete(Duration.between(applicationData.getStartTime(), completedAt));
  }

  public List<Status> getApplicationStatuses(Document document) {
    if (applicationStatuses != null) {
      return applicationStatuses.stream()
          .filter(appStatus -> appStatus.getDocumentType() == document)
          .map(ApplicationStatus::getStatus)
          .toList();
    }
    return Collections.emptyList();
  }

  public Status getApplicationStatus(Document document, String routingDestination) {
    if (applicationStatuses != null) {
      return applicationStatuses.stream()
          .filter(appStatus -> appStatus.getDocumentType() == document
              && (appStatus.getRoutingDestinationName() == null
              || appStatus.getRoutingDestinationName()
              .equals(routingDestination)))
          .findFirst()
          .map(ApplicationStatus::getStatus).orElse(null);
    }
    return null;
  }
}
