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
    private final PagesData pagesData;
    private final PagesConfiguration pagesConfiguration;

    public PageController(
            PagesConfiguration pagesConfiguration,
            PagesData pagesData
    ) {
        this.pagesData = pagesData;
        this.pagesConfiguration = pagesConfiguration;
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getFormPage(@PathVariable String pageName) {
        PageConfiguration pageConfiguration = this.pagesConfiguration.get(pageName);

        if (pageConfiguration.isStaticPage()) {
            HashMap<String, Object> model = new HashMap<>(Map.of("page", pageConfiguration));
            Optional.ofNullable(pageConfiguration.getDatasource())
                    .map(datasource -> FormData.getFormDataFrom(datasource, this.pagesData))
                    .ifPresent(model::putAll);

            return new ModelAndView(pageName, model);
        } else {
            return new ModelAndView("formPage",
                    Map.of(
                            "page", pageConfiguration,
                            "data", pagesData.getPageOrDefault(pageName, pageConfiguration),
                            "postTo", pageName));
        }
    }

    @PostMapping("/pages/{pageName}")
    ModelAndView postFormPage(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            @PathVariable String pageName) {
        PageConfiguration page = pagesConfiguration.get(pageName);
        FormData formData = FormData.fillOut(page, model);

        pagesData.putPage(pageName, formData);

        return formData.isValid() ?
                new ModelAndView(String.format("redirect:/pages/%s", page.getNextPage())) :
                new ModelAndView("formPage", Map.of(
                        "page", page,
                        "data", formData,
                        "postTo", pageName)
                );
    }
}
