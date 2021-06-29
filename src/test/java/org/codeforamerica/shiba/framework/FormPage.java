package org.codeforamerica.shiba.framework;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class FormPage {
    private final Document html;

    public FormPage(MvcResult response) throws UnsupportedEncodingException {
        html = Jsoup.parse(response.getResponse().getContentAsString());
    }

    public FormPage(ResultActions resultActions) throws UnsupportedEncodingException {
        html = Jsoup.parse(resultActions.andReturn().getResponse().getContentAsString());
    }

    public boolean hasInputError(String inputName) {
        Element element = html.select("input[name='%s[]'] ~ p.text--error".formatted(inputName)).first();
        return element != null;
    }

    public Element getInputError(String inputName) {
        return html.select("input[name='%s[]'] ~ p.text--error".formatted(inputName)).first();
    }

    public String getTitle() {
        return html.title();
    }
}
