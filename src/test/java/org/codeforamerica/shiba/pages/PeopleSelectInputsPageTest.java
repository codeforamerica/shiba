package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-people-inputs.yaml"})
public class PeopleSelectInputsPageTest extends AbstractExistingStartTimePageTest {

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("option1", Locale.ENGLISH, "option 1");
        staticMessageSource.addMessage("option2", Locale.ENGLISH, "option 2");
        staticMessageSource.addMessage("option3", Locale.ENGLISH, "option 3");
        staticMessageSource.addMessage("parent-not-at-home.none-of-the-children-have-parents-living-outside-the-home", Locale.ENGLISH, "None of the children have parents living outside the home");
    }

    @Test
    void shouldRenderInputsBasedOnSelectionsFromPreviousPage() {
        navigateTo("firstPage");
        testPage.enter("peopleSelect", List.of("option 1", "option 2"));
        testPage.clickContinue();

        takeSnapShot("test.png");
        assertThat(driver.findElement(By.id("Fake Person1 c6624883")).isDisplayed()).isTrue();
        assertThat(driver.findElement(By.id("Fake Person2 jre55443")).isDisplayed()).isTrue();
        assertThat(driver.findElements(By.id("Fake Person3 fafd2345"))).isEmpty();
        assertThat(driver.findElement(By.id("none__checkbox")).isDisplayed()).isTrue();
    }
}
