package org.codeforamerica.shiba;

import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class PageController {
    private final Map<String, FormData> data;
    private final Screens screens;

    public PageController(
            Screens screens,
            Map<String, FormData> data
    ) {
        this.data = data;
        this.screens = screens;
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getFormPage(@PathVariable String pageName) {
        Form page = screens.get(pageName);
        if (page.getInputs().isEmpty()) {
            HashMap<String, Object> baseModel = new HashMap<>(Map.of("form", page));
            Optional.ofNullable(page.getDataSource())
                    .map(datasource -> FormData.create(datasource, data))
                    .ifPresent(baseModel::putAll);

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
            return new ModelAndView(String.format("redirect:/pages/%s", form.getNextPage()));
        } else {
            return new ModelAndView("formPage",
                    Map.of(
                            "form", form,
                            "data", formData,
                            "postTo", pageName));
        }
    }
}
