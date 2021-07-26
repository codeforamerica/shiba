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
    String id;
    ZonedDateTime completedAt;
    ZonedDateTime updatedAt;
    ApplicationData applicationData;
    County county;
    Duration timeToComplete;
    FlowType flow;
    Sentiment sentiment;
    String feedback;
    Status cafApplicationStatus;
    Status ccapApplicationStatus;
    Status uploadedDocumentApplicationStatus;

    public Application addFeedback(Feedback feedback) {
        Sentiment sentiment = Optional.ofNullable(feedback.getSentiment()).orElse(this.sentiment);
        String feedbackText = !StringUtils.isEmpty(feedback.getFeedback()) ? feedback.getFeedback() : this.feedback;
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
