package org.codeforamerica.shiba.application;

import lombok.Builder;
import lombok.Data;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.Feedback;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

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

    public Application addFeedback(Feedback feedback) {
        var sentiment = Optional.ofNullable(feedback.getSentiment()).orElse(this.sentiment);
        var feedbackText = StringUtils.hasLength(feedback.getFeedback()) ? feedback.getFeedback() : this.feedback;
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
                uploadedDocumentApplicationStatus
        );
    }

    public ApplicationData getApplicationDataWithoutDataUrls() {
        ApplicationData applicationData = getApplicationData();
        List<UploadedDocument> uploadedDocuments = applicationData.getUploadedDocs();
        uploadedDocuments.forEach(uploadedDocument -> uploadedDocument.setDataURL(""));
        applicationData.setUploadedDocs(uploadedDocuments);
        return applicationData;
    }

    public void setCompletedAtTime(Clock clock) {
        completedAt = ZonedDateTime.now(clock);
        setTimeToComplete(Duration.between(applicationData.getStartTime(), completedAt));
    }
}
