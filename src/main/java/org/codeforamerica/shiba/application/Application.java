package org.codeforamerica.shiba.application;

import lombok.Builder;
import lombok.Value;
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
    ApplicationData applicationData;
    County county;
    Duration timeToComplete;
    FlowType flow;
    Sentiment sentiment;
    String feedback;

    public Application addFeedback(Feedback feedback) {
        Sentiment sentiment = Optional.ofNullable(feedback.getSentiment()).orElse(this.sentiment);
        String feedbackText = !StringUtils.isEmpty(feedback.getFeedback()) ? feedback.getFeedback() : this.feedback;

        return new Application(
                this.id,
                this.completedAt,
                this.applicationData,
                this.county,
                this.timeToComplete,
                this.flow,
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
