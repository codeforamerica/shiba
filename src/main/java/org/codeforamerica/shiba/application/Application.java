package org.codeforamerica.shiba.application;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import org.codeforamerica.shiba.County;
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
  private Status cafApplicationStatus;
  private Status ccapApplicationStatus;
  private Status uploadedDocumentApplicationStatus;
  private Status docUploadEmailStatus;

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
        cafApplicationStatus,
        ccapApplicationStatus,
        uploadedDocumentApplicationStatus,
        docUploadEmailStatus
    );
  }

  public void setCompletedAtTime(Clock clock) {
    completedAt = ZonedDateTime.now(clock);
    setTimeToComplete(Duration.between(applicationData.getStartTime(), completedAt));
  }
}
