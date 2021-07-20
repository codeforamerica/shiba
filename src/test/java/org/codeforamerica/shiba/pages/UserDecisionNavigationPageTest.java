package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.testutilities.AbstractExistingStartTimePageTest;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.YES;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Import(UserDecisionNavigationPageTest.TestController.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"pagesConfig=pages-config/test-user-decision-navigation.yaml"})
public class UserDecisionNavigationPageTest extends AbstractExistingStartTimePageTest {

    private final String optionZeroPageTitle = "page zero title";
    private final String optionOnePageTitle = "page one title";
    private final String yesAnswerTitle = "yes answer title";

    @Controller
    static class TestController {
        private final ApplicationData applicationData;

        public TestController(ApplicationData applicationData) {
            this.applicationData = applicationData;
        }

        @GetMapping("/pathExposingFlow")
        ModelAndView endpointExposingFlow() {
            return new ModelAndView("viewExposingFlow", "flow", applicationData.getFlow());
        }
    }

    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("option-zero-page-title", Locale.ENGLISH, optionZeroPageTitle);
        staticMessageSource.addMessage("option-one-page-title", Locale.ENGLISH, optionOnePageTitle);
        staticMessageSource.addMessage("yes-answer-title", Locale.ENGLISH, yesAnswerTitle);
    }

    @Test
    void shouldNavigateToOptionZeroPageWhenUserSelectOptionOne() {
        driver.navigate().to(baseUrl + "/pages/userDecisionNavigationPage");
        driver.findElement(By.partialLinkText("option 1")).click();

        assertThat(driver.getTitle()).isEqualTo(optionOnePageTitle);
    }

    @Test
    void shouldSetTheFlow_fromTheSelectedOption() {
        driver.navigate().to(baseUrl + "/pathExposingFlow");
        assertThat(driver.findElement(By.id("flow")).getText()).isEqualTo("UNDETERMINED");

        driver.navigate().to(baseUrl + "/pages/userDecisionNavigationPage");
        driver.findElement(By.partialLinkText("option 1")).click();

        driver.navigate().to(baseUrl + "/pathExposingFlow");
        assertThat(driver.findElement(By.id("flow")).getText()).isEqualTo("FULL");
    }

    @Test
    void shouldNotUnsetTheFlow_ifTheSelectedOptionDoesNotHaveOne() {
        driver.navigate().to(baseUrl + "/pathExposingFlow");
        assertThat(driver.findElement(By.id("flow")).getText()).isEqualTo("UNDETERMINED");

        driver.navigate().to(baseUrl + "/pages/userDecisionNavigationPage");
        driver.findElement(By.partialLinkText("option 1")).click();
        driver.navigate().back();
        driver.findElement(By.partialLinkText("option 0")).click();

        driver.navigate().to(baseUrl + "/pathExposingFlow");
        assertThat(driver.findElement(By.id("flow")).getText()).isEqualTo("FULL");

    }

    @Test
    void shouldNavigateToNextPageBasedOnCondition() {
        navigateTo("formPageBranchingNavigationPage");
        testPage.enter("yesNoQuestion", YES.getDisplayValue());

        assertThat(driver.getTitle()).isEqualTo(yesAnswerTitle);
    }
}
