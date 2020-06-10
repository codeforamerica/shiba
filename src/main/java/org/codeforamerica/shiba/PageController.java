package org.codeforamerica.shiba;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Controller
public class PageController {
    private final Map<String, FormData> data;
    private final MessageSource messageSource;
    private final Screens screens;

    public PageController(
            Screens screens,
            Map<String, FormData> data,
            MessageSource messageSource
    ) {
        this.data = data;
        this.messageSource = messageSource;
        this.screens = screens;
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getFormPage(@PathVariable String pageName) {
        Form page = screens.get(pageName);
        if (page.getPath() != null) {
            return new ModelAndView("redirect:" + page.getPath());
        }
        if (page.getInputs().isEmpty()) {
            String dataSource = page.getDataSource();

            HashMap<String, Object> baseModel = new HashMap<>(Map.of("form", page));
            Optional.ofNullable(dataSource).ifPresent(dataPage ->
                    baseModel.putAll(data.get(dataPage)));

            return new ModelAndView(pageName, baseModel);
        }
        return new ModelAndView("formPage",
                Map.of(
                        "form", page,
                        "data", data.getOrDefault(pageName, FormData.create(page)),
                        "postTo", pageName));
    }

    @PostMapping("/pages/{pageName}")
    ModelAndView postFormPage(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            @PathVariable String pageName) {
        Form form = screens.get(pageName);
        FormData formData = FormData.create(form, model);
        data.put(pageName, formData);

        if (formData.isValid()) {
            Form nextPage = screens.get(form.getNextPage());
            if (nextPage.getPath() != null) {
                return new ModelAndView("redirect:" + nextPage.getPath());
            }
            return new ModelAndView(String.format("redirect:/pages/%s", form.getNextPage()));
        } else {
            return new ModelAndView("formPage",
                    Map.of(
                            "form", form,
                            "data", formData,
                            "postTo", pageName));
        }
    }

    @GetMapping("/how-it-works")
    ModelAndView howItWorksPage(Locale locale) {
        FormData formData = data.get("choosePrograms");
        Form form = screens.get("howItWorks");
        // TODO: NPE if we navigate directly here
        if (!formData.isValid()) {
            //noinspection SpringMVCViewInspection
            return new ModelAndView("redirect:/choosePrograms");
        } else {
            return new ModelAndView(
                    "howItWorks",
                    Map.of(
                            "programSelection", new ProgramSelectionPresenter(messageSource, locale, formData.get("programs").getValue()),
                            "form", form));
        }
    }
}
