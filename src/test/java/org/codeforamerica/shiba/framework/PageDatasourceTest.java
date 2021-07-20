package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.FormPage;
import org.codeforamerica.shiba.pages.YesNoAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-page-datasources.yaml"})
public class PageDatasourceTest extends AbstractFrameworkTest {
    private final String staticPageWithDatasourceInputsTitle = "staticPageWithDatasourceInputsTitle";
    private final String yesHeaderText = "yes header text";
    private final String noHeaderText = "no header text";
    private final String noAnswerTitle = "no answer title";
    private final String yesAnswerTitle = "yes answer title";

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", ENGLISH, "firstPageTitle");
        staticMessageSource.addMessage("static-page-with-datasource-inputs-title", ENGLISH, staticPageWithDatasourceInputsTitle);
        staticMessageSource.addMessage("yes-header-text", ENGLISH, yesHeaderText);
        staticMessageSource.addMessage("no-header-text", ENGLISH, noHeaderText);
        staticMessageSource.addMessage("general.inputs.yes", ENGLISH, YesNoAnswer.YES.getDisplayValue());
        staticMessageSource.addMessage("general.inputs.no", ENGLISH, YesNoAnswer.NO.getDisplayValue());
        staticMessageSource.addMessage("some-other-header", ENGLISH, "some other header");
        staticMessageSource.addMessage("some-header", ENGLISH, "some other header");
        staticMessageSource.addMessage("no-answer-title", ENGLISH, noAnswerTitle);
        staticMessageSource.addMessage("radio-value-key-1", ENGLISH, "radio value 1");
        staticMessageSource.addMessage("radio-value-key-2", ENGLISH, "radio value 2");
        staticMessageSource.addMessage("foo", ENGLISH, "wrong title");
        staticMessageSource.addMessage("no-answer-title", ENGLISH, noAnswerTitle);
        staticMessageSource.addMessage("yes-answer-title", ENGLISH, yesAnswerTitle);
    }

    @Test
    void shouldDisplayDataEnteredFromAPreviousPage() throws Exception {
        var inputText = "some input";
        var nextPage = postAndFollowRedirect("firstPage", "someInputName", inputText);
        assertThat(nextPage.findElementTextById("someInputName")).isEqualTo(inputText);
    }

    @Test
    void shouldDisplayPageTitleBasedOnCondition() throws Exception {
        postExpectingNextPageTitle("yesNoQuestionPage", "yesNoQuestion", "false", noAnswerTitle);
    }

    @Test
    void shouldDisplayPageHeaderBasedOnCondition() throws Exception {
        var page = postAndFollowRedirect("yesNoQuestionPage", "yesNoQuestion", "true");
        assertThat(page.getTitle()).isEqualTo(yesAnswerTitle);
        assertThat(page.findElementByCssSelector("h1").text()).isEqualTo(yesHeaderText);
    }
}
