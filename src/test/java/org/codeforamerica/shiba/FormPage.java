package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.DatePart;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.test.web.servlet.ResultActions;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Helps parse mockmvc result html
 */
public class FormPage {
    private final Document html;

    public FormPage(ResultActions resultActions) throws UnsupportedEncodingException {
        html = Jsoup.parse(resultActions.andReturn().getResponse().getContentAsString());
    }

    public boolean hasInputError() {
        // It seems like we need to use this one on inputs of type SELECT, not sure why
        Element element = html.select("p.text--error").first();
        return element != null;
    }

    public boolean hasInputError(String inputName) {
        Element element = html.select("input[name='%s[]'] ~ p.text--error".formatted(inputName)).first();
        return element != null;
    }

    public Element getInputError(String inputName) {
        return html.select("input[name='%s[]'] ~ p.text--error".formatted(inputName)).first();
    }

    public String getWarningMessage() {
        return html.select("p.notice--warning").first().text();
    }

    public String findElementTextById(String id) {
        return html.getElementById(id).text();
    }

    public Elements findLinksByText(String text) {
        String cssSelector = String.format("a:contains(%s)", text);
        return html.select(cssSelector);
    }

    public String getTitle() {
        return html.title();
    }

    public Element getElementById(String id) {
        return html.getElementById(id);
    }

    public String getInputValue(String inputName) {
        return html.select("input[name='%s[]']".formatted(inputName)).attr("value");
    }

    public String getCardValue(String title) {
        return html.getElementsByClass("statistic-card").stream()
                .filter(card -> card.getElementsByClass("statistic-card__label").get(0).ownText().contains(title))
                .findFirst().orElseThrow()
                .getElementsByClass("statistic-card__number").get(0).ownText();
    }

    public List<Element> findElementsByTag(String tag) {
        return html.getElementsByTag(tag);
    }

    public List<Element> findElementsByClassName(String classname) {
        return html.getElementsByClass(classname);
    }

    public Element findInputByName(String name) {
        return html.select("input[name='%s[]']".formatted(name)).first();
    }

    public Element findElementByText(String text) {
        return html.getElementsContainingText(text).first();
    }

    public String getBirthDateValue(String inputName, DatePart datePart) {
        return html.select(
                "input[name='%s[]']:nth-of-type(%d)".formatted(inputName, datePart.getPosition())
        ).attr("value");
    }

    public String getRadioValue(String inputName) {
        return html.select("input[name='%s[]']".formatted(inputName)).stream()
                .filter(element -> element.hasAttr("checked"))
                .findFirst()
                .map(element -> element.attr("value"))
                .orElse(null);
    }

    public void assertLinkWithTextHasCorrectUrl(String linkText, String expectedUrl) {
        assertThat(findLinksByText(linkText)).hasSize(1);
        var url = findLinksByText(linkText).get(0).attr("href");
        assertThat(url).isEqualTo(expectedUrl);
    }

    public List<String> getCheckboxValues(String inputName) {
        return html.select("input[name='%s[]']".formatted(inputName)).stream()
                .filter(element -> element.hasAttr("checked"))
                .map(element -> element.attr("value"))
                .toList();

    }

    public String getSelectValue(String inputName) {
        var optionElements = html.select("select[name='%s[]']".formatted(inputName)).select("option");
        return optionElements.stream()
                .filter(element -> element.hasAttr("selected"))
                .findFirst()
                .map(element -> element.attr("value"))
                .orElseThrow();
    }

    public Element findElementByCssSelector(String selector) {
        return html.select(selector).first();
    }
}
