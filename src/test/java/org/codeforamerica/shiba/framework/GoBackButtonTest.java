package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.testutilities.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-conditional-rendering.yaml"})
@Tag("framework")
public class GoBackButtonTest extends AbstractExistingStartTimePageTest {
    private final String firstPageTitle = "firstPageTitle";
    private final String secondPageTitle = "secondPageTitle";
    private final String thirdPageTitle = "thirdPageTitle";

    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("starting-page-title", ENGLISH, "starting page");
        staticMessageSource.addMessage("first-page-title", ENGLISH, firstPageTitle);
        staticMessageSource.addMessage("second-page-title", ENGLISH, secondPageTitle);
        staticMessageSource.addMessage("third-page-title", ENGLISH, thirdPageTitle);
        staticMessageSource.addMessage("fourth-page-title", ENGLISH, "fourthPageTitle");
        staticMessageSource.addMessage("eighth-page-title", ENGLISH, "eighthPageTitle");
        staticMessageSource.addMessage("ninth-page-title", ENGLISH, "ninthPageTitle");
        staticMessageSource.addMessage("skip-message-key", ENGLISH, "SKIP PAGE");
        staticMessageSource.addMessage("not-skip-message-key", ENGLISH, "NOT SKIP PAGE");
        staticMessageSource.addMessage("page-to-skip-title", ENGLISH, "pageToSkip");
        staticMessageSource.addMessage("last-page-title", ENGLISH, "lastPageTitle");
    }

    @Test
    void shouldBeAbleToNavigateBackMoreThanOnePage() {
        // should be able to navigate back more than one page
        driver.navigate().to(baseUrl + "/pages/firstPage");
        driver.findElement(By.tagName("button")).click();
        assertThat(driver.getTitle()).isEqualTo(secondPageTitle);

        driver.findElement(By.tagName("button")).click();
        assertThat(driver.getTitle()).isEqualTo(thirdPageTitle);

        driver.findElement(By.partialLinkText("Back")).click();
        driver.findElement(By.partialLinkText("Back")).click();
        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);

        // should skip going backwards over skip condition pages
        testPage.enter("someRadioInputName", "SKIP PAGE");

        driver.findElement(By.tagName("button")).click();

        assertThat(driver.getTitle()).isEqualTo(thirdPageTitle);
        driver.findElement(By.partialLinkText("Back")).click();

        assertThat(driver.getTitle()).isEqualTo(firstPageTitle);

    }
}
