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
		staticMessageSource.addMessage("general.county-and-phone", Locale.ENGLISH, "somethin");
		staticMessageSource.addMessage("general.and", Locale.ENGLISH, "and");
		staticMessageSource.addMessage("dummy-page-title", Locale.ENGLISH, dummyPageTitle);
		staticMessageSource.addMessage("generic.footer", Locale.ENGLISH, "footer");
		staticMessageSource.addMessage("general.privacy", Locale.ENGLISH, "privacy");
		staticMessageSource.addMessage("general.mn-gov-portal", Locale.ENGLISH, "mngov");
		staticMessageSource.addMessage("general.privacy-policy", Locale.ENGLISH, "pp");
		staticMessageSource.addMessage("faq.faq", Locale.ENGLISH, "faq");
		staticMessageSource.addMessage("language-preferences.title", Locale.ENGLISH, "Language Preferences");
		staticMessageSource.addMessage("language-preferences.english", Locale.ENGLISH, "english");
		staticMessageSource.addMessage("language-preferences.spanish", Locale.ENGLISH, "spanish");
		staticMessageSource.addMessage("success.standard-suggested-action", Locale.ENGLISH, "success");
		staticMessageSource.addMessage("success.suggested-action-header", Locale.ENGLISH, "Suggested Action");
		staticMessageSource.addMessage("success.childcare", Locale.ENGLISH, "");
		staticMessageSource.addMessage("success.food-support", Locale.ENGLISH, "");
		staticMessageSource.addMessage("success.contact-promise", Locale.ENGLISH, "");
		staticMessageSource.addMessage("success.contact-promise-header", Locale.ENGLISH, "");
		staticMessageSource.addMessage("next-steps.no-document-upload-message", Locale.ENGLISH, "some message");
		staticMessageSource.addMessage("next-steps.document-upload-message", Locale.ENGLISH, "some message");
		staticMessageSource.addMessage("next-steps.allow-time-for-review", Locale.ENGLISH, "some message");
		staticMessageSource.addMessage("next-steps.allow-time-for-review-expedited-snap", Locale.ENGLISH, "some message");
		staticMessageSource.addMessage("next-steps.allow-time-for-review-expedited-ccap", Locale.ENGLISH, "some message");
		staticMessageSource.addMessage("next-steps.allow-time-for-review-not-expedited", Locale.ENGLISH, "some message");
	}
}
