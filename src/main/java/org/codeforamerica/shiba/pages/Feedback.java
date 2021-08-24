package org.codeforamerica.shiba.pages;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

@Value
public class Feedback {

  Sentiment sentiment;
  String feedback;

  boolean isInvalid() {
    return getSentiment() == null && StringUtils.isEmpty(getFeedback());
  }

  boolean isSentimentOnly() {
    return getSentiment() != null && StringUtils.isEmpty(getFeedback());
  }

  @NotNull String getMessageKey() {
    if (isInvalid()) {
      return "success.feedback-failure";
    } else if (isSentimentOnly()) {
      return "success.feedback-rating-success";
    } else {
      return "success.feedback-success";
    }
  }
}
