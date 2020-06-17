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
    private final PageConfiguration pageConfiguration;

    public PageController(
            PageConfiguration pageConfiguration,
            Map<String, FormData> data
    ) {
        this.data = data;
        this.pageConfiguration = pageConfiguration;
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getFormPage(@PathVariable String pageName) {
        Page page = pageConfiguration.get(pageName);
        if (page.getInputs().isEmpty()) {
            HashMap<String, Object> baseModel = new HashMap<>(Map.of("page", page));
            Optional.ofNullable(page.getDatasource())
                    .map(datasource -> FormData.create(datasource, data))
                    .ifPresent(baseModel::putAll);

            return new ModelAndView(pageName, baseModel);
        }
        FormData defaultFormData = Optional.ofNullable(page.getDatasource())
                .map(datasource -> FormData.create(page, datasource, data))
                .orElse(FormData.create(page));
        return new ModelAndView("formPage",
                Map.of(
                        "page", page,
                        "data", data.getOrDefault(pageName, defaultFormData),
                        "postTo", pageName));
    }

    @PostMapping("/pages/{pageName}")
    ModelAndView postFormPage(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            @PathVariable String pageName) {
        Page page = pageConfiguration.get(pageName);
        FormData formData = FormData.create(page, model);
        data.put(pageName, formData);

        if (formData.isValid()) {
            return new ModelAndView(String.format("redirect:/pages/%s", page.getNextPage()));
        } else {
            return new ModelAndView("formPage",
                    Map.of(
                            "page", page,
                            "data", formData,
                            "postTo", pageName));
        }
    }
}
