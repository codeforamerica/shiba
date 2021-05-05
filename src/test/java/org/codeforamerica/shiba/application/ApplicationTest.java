package org.codeforamerica.shiba.application;

import org.codeforamerica.shiba.pages.Feedback;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {
    @Test
    void shouldAddFeedback() {
        Application application = Application.builder().build();

        Sentiment sentiment = Sentiment.HAPPY;
        String feedback = "someFeedback";
        Application updatedApplication = application.addFeedback(new Feedback(sentiment, feedback));

        assertThat(updatedApplication.getSentiment()).isEqualTo(sentiment);
        assertThat(updatedApplication.getFeedback()).isEqualTo(feedback);
    }

    @Test
    void shouldNotReplaceExistingSentiment_whenSentimentIsNull() {
        Sentiment originalSentiment = Sentiment.HAPPY;
        Application application = Application.builder()
                .sentiment(originalSentiment)
                .build();

        Application updatedApplication = application.addFeedback(new Feedback(null, null));

        assertThat(updatedApplication.getSentiment()).isEqualTo(originalSentiment);
    }

    @Test
    void shouldNotReplaceExistingFeedbackText_whenFeedbackTextIsNull() {
        String originalFeedbackText = "someFeedback";
        Application application = Application.builder()
                .feedback(originalFeedbackText)
                .build();

        Application updatedApplication = application.addFeedback(new Feedback(null, null));

        assertThat(updatedApplication.getFeedback()).isEqualTo(originalFeedbackText);
    }

    @Test
    void shouldNotReplaceExistingFeedbackText_whenFeedbackTextIsEmpty() {
        String originalFeedbackText = "someFeedback";
        Application application = Application.builder()
                .feedback(originalFeedbackText)
                .build();

        Application updatedApplication = application.addFeedback(new Feedback(null, ""));

        assertThat(updatedApplication.getFeedback()).isEqualTo(originalFeedbackText);
    }

    @Test
    void shouldReturnApplicationDataWithoutDataURLs() {
        ApplicationData applicationData = new ApplicationData();
        MockMultipartFile image = new MockMultipartFile("image", "test".getBytes());
        applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");

        Application application = Application.builder()
                .applicationData(applicationData)
                .build();

        assertThat(application.getApplicationDataWithoutDataUrls().getUploadedDocs().get(0).getDataURL()).isEqualTo("");
    }
}