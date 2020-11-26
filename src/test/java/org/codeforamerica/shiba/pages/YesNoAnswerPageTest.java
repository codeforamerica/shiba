package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractExistingStartTimePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "pagesConfig=pages-config/yes-no-answer.yaml"
})
public class YesNoAnswerPageTest extends AbstractExistingStartTimePageTest {

    private final String answerPage = "option-zero-page-title";

    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        staticMessageSource.addMessage("answer-page", Locale.ENGLISH, answerPage);
    }

    @Test
    void shouldDisplaySelectedAnswer() {
        driver.navigate().to(baseUrl + "/pages/yesNoQuestionPage");
        driver.findElement(By.cssSelector("button")).click();

        assertThat(driver.getTitle()).isEqualTo(answerPage);
        assertThat(testPage.findElementTextByName("yesOrNo")).isEqualTo("true");
    }

}
