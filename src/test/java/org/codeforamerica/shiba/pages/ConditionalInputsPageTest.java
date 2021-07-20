package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.testutilities.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-conditional-inputs.yaml"})
public class ConditionalInputsPageTest extends AbstractExistingStartTimePageTest {

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("option1", Locale.ENGLISH, "option 1");
        staticMessageSource.addMessage("option2", Locale.ENGLISH, "option 2");
        staticMessageSource.addMessage("option3", Locale.ENGLISH, "option 3");
    }

    @Test
    void shouldOnlyRenderInputsBasedOnCondition() {
        navigateTo("firstPage");

        testPage.enter("options", List.of("option 1", "option 2"));
        testPage.clickContinue();

        assertThat(driver.findElement(By.name("option1Text[]")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.name("option2Text[]")).isDisplayed()).isTrue();
        assertThat(driver.findElements(By.name("option3Text[]"))).isEmpty();
    }
}
