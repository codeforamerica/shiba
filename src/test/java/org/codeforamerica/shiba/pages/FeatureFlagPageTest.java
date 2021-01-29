package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "pagesConfig=pages-config/test-feature-flag.yaml"
})
public class FeatureFlagPageTest extends AbstractExistingStartTimePageTest {

    @Override
    @BeforeEach
    public void setUp() throws java.io.IOException {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", Locale.ENGLISH, "firstPage");
        staticMessageSource.addMessage("second-page-title", Locale.ENGLISH, "secondPage");
        staticMessageSource.addMessage("first-feature-page-title", Locale.ENGLISH, "firstFeature");
        staticMessageSource.addMessage("second-feature-page-title", Locale.ENGLISH, "secondFeature");
    }

    @DynamicPropertySource
    static void registerFeatureFlagProperty(DynamicPropertyRegistry registry) {
        registry.add("feature-flags.first-feature", () -> "on");
        registry.add("feature-flags.second-feature", () -> "off");
    }

    @Test
    void shouldGoToSpecificPageIfFeatureFlagIsEnabled() {
        navigateTo("firstPage");
        testPage.enter("foo", "bar");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("firstFeature");
    }

    @Test
    void shouldGoToOtherNextPageIfFeatureFlagIsDisabled() {
        navigateTo("firstFeaturePage");
        testPage.enter("foo", "bar");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("secondPage");
    }

    @Test
    void shouldGoToNextPageIfFeatureFlagIsUnset() {
        navigateTo("secondPage");
        testPage.enter("foo", "bar");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("firstPage");
    }

    @Test
    void shouldUseConfigOrderToDeterminePagePrecedenceAndIgnoreFlag() {
        navigateTo("secondFeaturePage");
        testPage.enter("foo", "bar");
        testPage.clickContinue();

        assertThat(driver.getTitle()).isEqualTo("secondPage");
    }
}

