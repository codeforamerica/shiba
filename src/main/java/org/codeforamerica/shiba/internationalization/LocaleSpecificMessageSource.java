package org.codeforamerica.shiba.internationalization;

import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import reactor.util.annotation.Nullable;

public record LocaleSpecificMessageSource(Locale locale, MessageSource messageSource) {

  public String getMessage(String messageKey) {
    return getMessage(messageKey, null);
  }

  public String getMessage(String messageKey, @Nullable List<String> args) {
    return messageSource
        .getMessage(messageKey, ofNullable(args).map(List::toArray).orElse(null), locale);
  }
}
