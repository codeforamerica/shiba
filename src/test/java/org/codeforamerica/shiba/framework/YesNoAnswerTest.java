package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.codeforamerica.shiba.testutilities.FormPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"pagesConfig=pages-config/yes-no-answer.yaml"})
public class YesNoAnswerTest extends AbstractFrameworkTest {
    private final String answerPageTitle = "option-zero-page-title";

    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("answer-page", ENGLISH, answerPageTitle);
    }

    @Test
    void shouldDisplaySelectedAnswer() throws Exception {
        var answerPage = postAndFollowRedirect("yesNoQuestionPage", "yesOrNo", "true");
        assertThat(answerPage.getTitle()).isEqualTo(answerPageTitle);
        assertThat(answerPage.findElementTextById("yesOrNo")).isEqualTo("true");
    }
}
