package org.codeforamerica.shiba.internationalization;

import org.springframework.context.MessageSource;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Locale;

import static java.util.Optional.ofNullable;

public record LocaleSpecificMessageSource(Locale locale, MessageSource messageSource) {
    public String getMessage(String messageKey) {
        return getMessage(messageKey, null);
    }

    public String getMessage(String snapExpeditedWaitTime, @Nullable List<String> args) {
        return messageSource.getMessage(
                snapExpeditedWaitTime,
                ofNullable(args).map(List::toArray).orElse(null),
                locale
        );
    }
}