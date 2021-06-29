package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.ContextConfiguration;

import static java.util.Locale.ENGLISH;

@ContextConfiguration(classes = {StaticMessageSourceConfiguration.class})
public class AbstractFrameworkTest extends AbstractShibaMockMvcTest {
    @Autowired
    protected StaticMessageSource staticMessageSource;

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("general.go-back", ENGLISH, "Go Back");
        staticMessageSource.addMessage("general.continue", ENGLISH, "Continue");
        staticMessageSource.addMessage("general.inputs.yes", ENGLISH, "Yes");
        staticMessageSource.addMessage("general.inputs.no", ENGLISH, "No");
        staticMessageSource.addMessage("dummy-page-title", ENGLISH, "Dummy page title");
        staticMessageSource.addMessage("generic.footer", ENGLISH, "footer");
        staticMessageSource.addMessage("general.privacy", ENGLISH, "privacy");
        staticMessageSource.addMessage("general.code-for-america", ENGLISH, "cfa");
        staticMessageSource.addMessage("general.privacy-policy", ENGLISH, "pp");
        staticMessageSource.addMessage("faq.faq", ENGLISH, "faq");
        staticMessageSource.addMessage("language-preferences.title", ENGLISH, "Language Preferences");
        staticMessageSource.addMessage("language-preferences.english", ENGLISH, "english");
        staticMessageSource.addMessage("language-preferences.spanish", ENGLISH, "spanish");
        staticMessageSource.addMessage("success.standard-suggested-action", ENGLISH, "success");
    }
}
