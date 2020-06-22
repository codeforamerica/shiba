package org.codeforamerica.shiba;

import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.codeforamerica.shiba.FormData.getFormDataFrom;

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

    @GetMapping("/pages/{pageName}/navigation")
    RedirectView goBackToPage(@PathVariable String pageName,
                              @RequestParam(defaultValue = "false") Boolean isBackwards) {
        PageConfiguration currentPageConfiguration = this.pagesConfiguration.get(pageName);

        String adjacentPageName = currentPageConfiguration.getAdjacentPageName(isBackwards);
        PageConfiguration adjacentPage = this.pagesConfiguration.get(adjacentPageName);

        if (adjacentPage.shouldSkip(pagesData)) {
            return new RedirectView(String.format("/pages/%s", adjacentPage.getAdjacentPageName(isBackwards)));
        } else {
            return new RedirectView(String.format("/pages/%s", adjacentPageName));
        }
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getFormPage(@PathVariable String pageName) {
        PageConfiguration pageConfiguration = this.pagesConfiguration.get(pageName);

        HashMap<String, Object> model = new HashMap<>(Map.of(
                "page", pageConfiguration,
                "pageName", pageName
        ));
        String pageToRender;
        if (pageConfiguration.isStaticPage()) {
            pageToRender = pageName;
            Optional.ofNullable(pageConfiguration.getDatasource())
                    .map(datasource -> getFormDataFrom(datasource, this.pagesData))
                    .ifPresent(model::putAll);
        } else {
            pageToRender = "formPage";
            model.put("data", pagesData.getPageOrDefault(pageName, pageConfiguration));
        }
        return new ModelAndView(pageToRender, model);
    }

    @PostMapping("/pages/{pageName}")
    ModelAndView postFormPage(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            @PathVariable String pageName) {
        PageConfiguration page = pagesConfiguration.get(pageName);
        FormData formData = FormData.fillOut(page, model);

        pagesData.putPage(pageName, formData);

        return formData.isValid() ?
                new ModelAndView(String.format("redirect:/pages/%s/navigation", pageName)) :
                new ModelAndView("formPage", Map.of(
                        "page", page,
                        "data", formData,
                        "pageName", pageName)
                );
    }
}
