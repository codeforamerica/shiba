package org.codeforamerica.shiba.application;

import lombok.*;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.Feedback;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Value
@Builder
public class Application {
    String id;
    ZonedDateTime completedAt;
    ZonedDateTime updatedAt;
    ApplicationData applicationData;
    County county;
    Duration timeToComplete;
    FlowType flow;
    Status status;
    Sentiment sentiment;
    String feedback;

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
                status,
                sentiment,
                feedbackText
        );
    }

    public ApplicationData getApplicationDataWithoutDataUrls() {
        ApplicationData applicationData = getApplicationData();
        List<UploadedDocument> uploadedDocuments = applicationData.getUploadedDocs();
        uploadedDocuments.forEach(uploadedDocument -> uploadedDocument.setDataURL(""));
        applicationData.setUploadedDocs(uploadedDocuments);
        return applicationData;
    }
}
