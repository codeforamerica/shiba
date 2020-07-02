package org.codeforamerica.shiba.pages;

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
    void setUp() throws IOException {
        super.setUp();
        staticMessageSource = (StaticMessageSource) messageSource;
        staticMessageSource.addMessage("general.go-back", Locale.US, "Go Back");
        staticMessageSource.addMessage("general.continue", Locale.US, "Continue");
    }
}
