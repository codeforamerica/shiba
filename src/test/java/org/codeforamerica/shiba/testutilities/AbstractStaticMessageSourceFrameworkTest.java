package org.codeforamerica.shiba.testutilities;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = StaticMessageSourceConfiguration.class)
@Tag("framework")
public class AbstractStaticMessageSourceFrameworkTest extends AbstractShibaMockMvcTest {

  protected final String dummyPageTitle = "Dummy page title";
  protected StaticMessageSource staticMessageSource;
  @Autowired
  private MessageSource messageSource;

  @Override
  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    staticMessageSource = (StaticMessageSource) messageSource;
    staticMessageSource.addMessage("general.go-back", Locale.ENGLISH, "Go Back");
    staticMessageSource.addMessage("general.continue", Locale.ENGLISH, "Continue");
    staticMessageSource.addMessage("general.inputs.yes", Locale.ENGLISH, "Yes");
    staticMessageSource.addMessage("general.inputs.no", Locale.ENGLISH, "No");
    staticMessageSource.addMessage("dummy-page-title", Locale.ENGLISH, dummyPageTitle);
    staticMessageSource.addMessage("generic.footer", Locale.ENGLISH, "footer");
    staticMessageSource.addMessage("general.privacy", Locale.ENGLISH, "privacy");
    staticMessageSource.addMessage("general.code-for-america", Locale.ENGLISH, "cfa");
    staticMessageSource.addMessage("general.privacy-policy", Locale.ENGLISH, "pp");
    staticMessageSource.addMessage("faq.faq", Locale.ENGLISH, "faq");
    staticMessageSource
        .addMessage("language-preferences.title", Locale.ENGLISH, "Language Preferences");
    staticMessageSource.addMessage("language-preferences.english", Locale.ENGLISH, "english");
    staticMessageSource.addMessage("language-preferences.spanish", Locale.ENGLISH, "spanish");
    staticMessageSource.addMessage("success.standard-suggested-action", Locale.ENGLISH, "success");
  }
}
