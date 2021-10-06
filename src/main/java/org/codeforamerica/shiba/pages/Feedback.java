package org.codeforamerica.shiba.pages;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

public record Feedback(Sentiment sentiment, String feedback) {

  boolean isInvalid() {
    return sentiment == null && !StringUtils.hasText(feedback);
  }

  boolean isSentimentOnly() {
    return sentiment != null && !StringUtils.hasText(feedback);
  }

  @NotNull
  String getMessageKey() {
    if (isInvalid()) {
      return "success.feedback-failure";
    } else if (isSentimentOnly()) {
      return "success.feedback-rating-success";
    } else {
      return "success.feedback-success";
    }
  }
}
