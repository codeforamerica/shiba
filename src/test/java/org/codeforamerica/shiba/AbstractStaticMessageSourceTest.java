package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.Locale;

@ContextConfiguration(classes = StaticMessageSourceConfiguration.class)
public class AbstractStaticMessageSourceTest {
    @Autowired
    private MessageSource messageSource;

    protected StaticMessageSource staticMessageSource;

    @BeforeEach
    protected void setUp() throws IOException {
        staticMessageSource = (StaticMessageSource) messageSource;
        staticMessageSource.addMessage("general.go-back", Locale.ENGLISH, "Go Back");
        staticMessageSource.addMessage("general.continue", Locale.ENGLISH, "Continue");
        staticMessageSource.addMessage("general.inputs.yes", Locale.ENGLISH, "Yes");
        staticMessageSource.addMessage("general.inputs.no", Locale.ENGLISH, "No");
        staticMessageSource.addMessage("dummy-page-title", Locale.ENGLISH, "Dummy page title");
        staticMessageSource.addMessage("generic.footer", Locale.ENGLISH, "footer");
        staticMessageSource.addMessage("general.privacy", Locale.ENGLISH, "privacy");
        staticMessageSource.addMessage("general.code-for-america", Locale.ENGLISH, "cfa");
        staticMessageSource.addMessage("general.privacy-policy", Locale.ENGLISH, "pp");
        staticMessageSource.addMessage("language-preferences.english", Locale.ENGLISH, "english");
        staticMessageSource.addMessage("language-preferences.spanish", Locale.ENGLISH, "spanish");
    }
}
