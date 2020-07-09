package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.metrics.ApplicationMetric;
import org.codeforamerica.shiba.metrics.ApplicationMetricsRepository;
import org.codeforamerica.shiba.metrics.Metrics;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.codeforamerica.shiba.pages.FormData.getFormDataFrom;

@Controller
public class PageController {
    private final PagesData pagesData;
    private final PagesConfiguration pagesConfiguration;
    private final Clock clock;
    private final ApplicationMetricsRepository repository;
    private final Metrics metrics;
    public static final String SIGN_THIS_APPLICATION_PAGE_NAME = "signThisApplication";

    public PageController(
            PagesConfiguration pagesConfiguration,
            PagesData pagesData,
            Clock clock,
            ApplicationMetricsRepository repository,
            Metrics metrics) {
        this.pagesData = pagesData;
        this.pagesConfiguration = pagesConfiguration;
        this.clock = clock;
        this.repository = repository;
        this.metrics = metrics;
    }

    @GetMapping("/pages/{pageName}/navigation")
    RedirectView goBackToPage(
            @PathVariable String pageName,
            @RequestParam(defaultValue = "false") Boolean isBackwards,
            @RequestParam(required = false, defaultValue = "0") Integer option
    ) {
        PageConfiguration currentPageConfiguration = this.pagesConfiguration.getPages().get(pageName);

        String adjacentPageName = currentPageConfiguration.getAdjacentPageName(isBackwards, option);
        PageConfiguration adjacentPage = this.pagesConfiguration.getPages().get(adjacentPageName);

        if (adjacentPage.shouldSkip(pagesData)) {
            return new RedirectView(String.format("/pages/%s", adjacentPage.getAdjacentPageName(isBackwards)));
        } else {
            return new RedirectView(String.format("/pages/%s", adjacentPageName));
        }
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getFormPage(@PathVariable String pageName) {
        PageConfiguration pageConfiguration = this.pagesConfiguration.getPages().get(pageName);

        FlowConfiguration flowConfiguration = pagesConfiguration.getFlow();
        if (flowConfiguration.shouldResetData(pageName)) {
            this.pagesData.getData().clear();
            metrics.clear();
        }

        if (flowConfiguration.shouldStartTimer(pageName)) {
            metrics.setStartTimeOnce(clock.instant());
        }

        HashMap<String, Object> model = new HashMap<>(Map.of(
                "page", pageConfiguration,
                "pageName", pageName,
                "pageTitle", pageConfiguration.resolve(pagesData, PageConfiguration::getPageTitle),
                "headerKey", pageConfiguration.resolve(pagesData, PageConfiguration::getHeaderKey)
        ));

        String pageToRender;
        if (pageConfiguration.isStaticPage()) {
            pageToRender = pageName;
            Optional.ofNullable(pageConfiguration.getDatasources())
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
            @PathVariable String pageName
    ) {
        PageConfiguration page = pagesConfiguration.getPages().get(pageName);
        FormData formData = FormData.fillOut(page, model);

        pagesData.putPage(pageName, formData);

        return formData.isValid() ?
                new ModelAndView(String.format("redirect:/pages/%s/navigation", pageName)) :
                new ModelAndView("formPage", Map.of(
                        "page", page,
                        "data", formData,
                        "pageName", pageName,
                        "pageTitle", page.resolve(pagesData, PageConfiguration::getPageTitle),
                        "headerKey", page.resolve(pagesData, PageConfiguration::getHeaderKey)
                ));
    }

    @PostMapping("/submit")
    ModelAndView submitApplication(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            Locale locale
    ) {
        PageConfiguration page = pagesConfiguration.getPages().get(SIGN_THIS_APPLICATION_PAGE_NAME);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", locale);
        FormData formData = new FormData(Map.of(
                "applicantSignature", new InputData(Validation.NOT_BLANK, model.get("applicantSignature")),
                "submissionTime", new InputData(List.of(dateTimeFormatter.format(ZonedDateTime.ofInstant(clock.instant(), ZoneId.of("UTC")))))
        ));
        pagesData.putPage(SIGN_THIS_APPLICATION_PAGE_NAME, formData);

        ApplicationMetric applicationMetric = new ApplicationMetric(Duration.between(metrics.getStartTime(), clock.instant()));
        repository.save(applicationMetric);
        return formData.isValid() ?
                new ModelAndView(String.format("redirect:/pages/%s/navigation", SIGN_THIS_APPLICATION_PAGE_NAME)) :
                new ModelAndView(SIGN_THIS_APPLICATION_PAGE_NAME, Map.of(
                        "page", page,
                        "data", formData,
                        "pageName", SIGN_THIS_APPLICATION_PAGE_NAME,
                        "pageTitle", page.resolve(pagesData, PageConfiguration::getPageTitle),
                        "headerKey", page.resolve(pagesData, PageConfiguration::getHeaderKey)
                ));
    }
}
