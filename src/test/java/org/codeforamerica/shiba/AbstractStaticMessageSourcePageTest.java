package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticMessageSource;

import java.io.IOException;
import java.util.Locale;

@Import(StaticMessageSourceConfiguration.class)
public class AbstractStaticMessageSourcePageTest extends AbstractBasePageTest {
    @Autowired
    private MessageSource messageSource;

    protected StaticMessageSource staticMessageSource;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource = (StaticMessageSource) messageSource;
        staticMessageSource.addMessage("general.go-back", Locale.US, "Go Back");
        staticMessageSource.addMessage("general.continue", Locale.US, "Continue");
        staticMessageSource.addMessage("general.inputs.yes", Locale.US, "Yes");
        staticMessageSource.addMessage("general.inputs.no", Locale.US, "No");
        staticMessageSource.addMessage("dummy-page-title", Locale.US, "Dummy page title");
        staticMessageSource.addMessage("generic.footer", Locale.US, "footer");
        staticMessageSource.addMessage("general.privacy", Locale.US, "privacy");
        staticMessageSource.addMessage("general.code-for-america", Locale.US, "cfa");
        staticMessageSource.addMessage("general.privacy-policy", Locale.US, "pp");
    }
}
