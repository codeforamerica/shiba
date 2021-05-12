package org.codeforamerica.shiba.internationalization;

import org.springframework.context.MessageSource;

import java.util.Locale;

public record LocaleSpecificMessageSource(Locale locale, MessageSource messageSource) {
    public String getMessage(String messageKey) {
        return getMessage(messageKey, null);
    }

    public String getMessage(String messageKey, String[] args) {
        return messageSource.getMessage(messageKey, args, locale);
    }
}