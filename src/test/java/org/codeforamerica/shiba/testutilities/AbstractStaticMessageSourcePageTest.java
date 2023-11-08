package org.codeforamerica.shiba.testutilities;

import java.io.IOException;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = StaticMessageSourceConfiguration.class)
public class AbstractStaticMessageSourcePageTest extends AbstractBasePageTest {

  protected StaticMessageSource staticMessageSource;
  @Autowired
  private MessageSource messageSource;

  @Override
  @BeforeEach
  protected void setUp() throws IOException {
    super.setUp();
    staticMessageSource = (StaticMessageSource) messageSource;
    staticMessageSource.addMessage("general.go-back", Locale.ENGLISH, "Go Back");
    staticMessageSource.addMessage("general.continue", Locale.ENGLISH, "Continue");
    staticMessageSource.addMessage("general.inputs.yes", Locale.ENGLISH, "Yes");
    staticMessageSource.addMessage("general.inputs.no", Locale.ENGLISH, "No");
    staticMessageSource.addMessage("dummy-page-title", Locale.ENGLISH, "Dummy page title");
    staticMessageSource.addMessage("generic.footer", Locale.ENGLISH, "footer");
    staticMessageSource.addMessage("general.privacy", Locale.ENGLISH, "privacy");
    staticMessageSource.addMessage("general.mn-gov-portal", Locale.ENGLISH, "mngov");
    staticMessageSource.addMessage("general.privacy-policy", Locale.ENGLISH, "pp");
    staticMessageSource.addMessage("faq.faq", Locale.ENGLISH, "faq");
    staticMessageSource
        .addMessage("language-preferences.title", Locale.ENGLISH, "Language Preferences");
    staticMessageSource.addMessage("language-preferences.english", Locale.ENGLISH, "english");
    staticMessageSource.addMessage("language-preferences.spanish", Locale.ENGLISH, "spanish");
    staticMessageSource.addMessage("success.standard-suggested-action", Locale.ENGLISH, "success");
    staticMessageSource.addMessage("snap-nds.header", Locale.ENGLISH, "SNAP NDS");
  }
}
