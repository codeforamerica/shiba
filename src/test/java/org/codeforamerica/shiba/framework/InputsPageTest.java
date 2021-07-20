package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.FormPage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-input.yaml"})
public class InputsPageTest extends AbstractFrameworkTest {
    final String radioOption1 = "radio option 1";
    final String radioOption2 = "option-2";
    final String checkboxOption1 = "checkbox option 1";
    final String checkboxOption2 = "checkbox option 2";
    final String noneCheckboxOption = "none checkbox option";
    final String selectOption1 = "select option 1";
    final String selectOption2 = "select option 2";
    final String followUpTrue = "YEP";
    final String followUpFalse = "NOPE";
    final String followUpUncertain = "UNSURE";
    final String promptMessage = "prompt message";
    final String helpMessage = "help message";
    final String optionHelpMessage = "option help message";
    final String placeholder = "optional input";

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", ENGLISH, "firstPageTitle");
        staticMessageSource.addMessage("next-page-title", ENGLISH, "nextPageTitle");
        staticMessageSource.addMessage("radio-option-1", ENGLISH, radioOption1);
        staticMessageSource.addMessage("radio-option-2", ENGLISH, radioOption2);
        staticMessageSource.addMessage("checkbox-option-1", ENGLISH, checkboxOption1);
        staticMessageSource.addMessage("checkbox-option-2", ENGLISH, checkboxOption2);
        staticMessageSource.addMessage("none-checkbox-option", ENGLISH, noneCheckboxOption);
        staticMessageSource.addMessage("select-option-1", ENGLISH, selectOption1);
        staticMessageSource.addMessage("select-option-2", ENGLISH, selectOption2);
        staticMessageSource.addMessage("follow-up-true", ENGLISH, followUpTrue);
        staticMessageSource.addMessage("follow-up-false", ENGLISH, followUpFalse);
        staticMessageSource.addMessage("follow-up-uncertain", ENGLISH, followUpUncertain);
        staticMessageSource.addMessage("prompt-message-key", ENGLISH, promptMessage);
        staticMessageSource.addMessage("help-message-key", ENGLISH, helpMessage);
        staticMessageSource.addMessage("option-help-key", ENGLISH, optionHelpMessage);
        staticMessageSource.addMessage("general.optional", ENGLISH, placeholder);
        staticMessageSource.addMessage("general.month", ENGLISH, "month");
        staticMessageSource.addMessage("general.day", ENGLISH, "day");
        staticMessageSource.addMessage("general.year", ENGLISH, "year");
    }

    @Test
    void shouldDisplayPromptMessageFragment() throws Exception {
        var page = getFormPage("inputWithPromptFragmentPage");
        assertThat(page.findLinksByText("test message")).isNotNull();
    }

    @Test
    void shouldShowHelpMessageKeyOnCheckboxOptions() throws Exception {
        var page = getFormPage("firstPage");
        var checkboxElements = page.findElementsByClassName("checkbox");
        assertThat(checkboxElements).hasSize(6);
        assertThat(checkboxElements.get(0).text()).contains(optionHelpMessage);
    }

    @Test
    void shouldNotDisplayPrimaryButtonWhenHasPrimaryButtonIsFalse() throws Exception {
        var page = getFormPage("doNotHavePrimaryButtonPage");
        assertThat(page.findElementsByClassName("button--primary")).isEmpty();
    }

    @Test
    void shouldDisplayFragmentForPage() throws Exception {
        var page = getFormPage("pageWithContextFragment");
        assertThat(page.getElementById("pageContext").text()).isEqualTo("this is context");
    }

    @Test
    void shouldHaveAccessToDatasources() throws Exception {
        postExpectingRedirect("firstPage", "editableTextInput", "Datasource Text", "nextPage");

        postExpectingRedirect("subworkflowPage", "value1", "a", "subworkflowPage");
        postExpectingRedirect("subworkflowPage", "value1", "b", "subworkflowPage");
        postExpectingRedirect("subworkflowPage", "value1", "c", "subworkflowPage");

        var page = getFormPage("pageWithReferenceCheckboxes");
        assertThat(page.findElementTextById("iteration0")).isEqualTo("a");
        assertThat(page.findElementTextById("iteration1")).isEqualTo("b");
        assertThat(page.findElementTextById("iteration2")).isEqualTo("c");
        assertThat(page.findElementTextById("datasourceText")).isEqualTo("Datasource Text");
    }

}
