package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.metrics.ApplicationMetric;
import org.codeforamerica.shiba.metrics.ApplicationMetricsRepository;
import org.codeforamerica.shiba.metrics.Metrics;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.codeforamerica.shiba.pages.FormData.getFormDataFrom;

@Controller
public class PageController {
    private final ApplicationData applicationData;
    private final PagesConfiguration pagesConfiguration;
    private final Clock clock;
    private final ApplicationMetricsRepository repository;
    private final Metrics metrics;

    public PageController(
            PagesConfiguration pagesConfiguration,
            ApplicationData applicationData,
            Clock clock,
            ApplicationMetricsRepository repository,
            Metrics metrics) {
        this.applicationData = applicationData;
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
        Map<String, PageConfiguration> pages = this.pagesConfiguration.getPages();
        PageConfiguration currentPageConfiguration = pages.get(pageName);

        String adjacentPageName = currentPageConfiguration.getAdjacentPageName(isBackwards, option);
        PageConfiguration adjacentPage = pages.get(adjacentPageName);

        if (adjacentPage.shouldSkip(applicationData.getPagesData())) {
            return new RedirectView(String.format("/pages/%s", adjacentPage.getAdjacentPageName(isBackwards)));
        } else {
            return new RedirectView(String.format("/pages/%s", adjacentPageName));
        }
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getFormPage(@PathVariable String pageName,
                             HttpServletResponse response) {
        FlowConfiguration flowConfiguration = pagesConfiguration.getFlow();

        if (flowConfiguration.isLandingPage(pageName)) {
            this.applicationData.clear();
            this.metrics.clear();
        } else if (flowConfiguration.isStartTimerPage(pageName)) {
            this.metrics.setStartTimeOnce(clock.instant());
        }

        if (!flowConfiguration.isTerminalPage(pageName) && this.applicationData.isSubmitted()) {
            return new ModelAndView(String.format("redirect:/pages/%s", flowConfiguration.getTerminalPage()));
        } else if (!flowConfiguration.isLandingPage(pageName) && metrics.getStartTime() == null) {
            return new ModelAndView(String.format("redirect:/pages/%s", flowConfiguration.getLandingPages().get(0)));
        }

        response.addHeader("Cache-Control", "no-store");

        PageConfiguration pageConfiguration = this.pagesConfiguration.getPages().get(pageName);

        PagesData pagesData = applicationData.getPagesData();
        HashMap<String, Object> model = new HashMap<>(Map.of(
                "page", pageConfiguration,
                "pageName", pageName,
                "postTo", flowConfiguration.isSubmitPage(pageName) ? "/submit" : "/pages/" + pageName,
                "pageTitle", pageConfiguration.resolve(pagesData, PageConfiguration::getPageTitle),
                "headerKey", pageConfiguration.resolve(pagesData, PageConfiguration::getHeaderKey)
        ));

        if (flowConfiguration.isTerminalPage(pageName)) {
            model.put("submissionTime", this.applicationData.getSubmissionTime());
        }

        String pageToRender;
        if (pageConfiguration.isStaticPage()) {
            pageToRender = pageName;
            Optional.ofNullable(pageConfiguration.getDatasources())
                    .map(datasource -> getFormDataFrom(datasource, this.applicationData.getPagesData()))
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

        PagesData pagesData = applicationData.getPagesData();
        pagesData.putPage(pageName, formData);

        return formData.isValid() ?
                new ModelAndView(String.format("redirect:/pages/%s/navigation", pageName)) :
                new ModelAndView("formPage", Map.of(
                        "page", page,
                        "data", formData,
                        "pageName", pageName,
                        "postTo", this.pagesConfiguration.getFlow().isSubmitPage(pageName) ? "/submit" : "/pages/" + pageName,
                        "pageTitle", page.resolve(pagesData, PageConfiguration::getPageTitle),
                        "headerKey", page.resolve(pagesData, PageConfiguration::getHeaderKey)
                ));
    }

    @PostMapping("/submit")
    ModelAndView submitApplication(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            Locale locale
    ) {
        FlowConfiguration flowConfiguration = this.pagesConfiguration.getFlow();
        String submitPage = flowConfiguration.getSubmitPage();
        PageConfiguration page = pagesConfiguration.getPages().get(submitPage);

        FormData formData = FormData.fillOut(page, model);
        PagesData pagesData = applicationData.getPagesData();
        pagesData.putPage(submitPage, formData);

        if (formData.isValid()) {
            ApplicationMetric applicationMetric = new ApplicationMetric(Duration.between(metrics.getStartTime(), clock.instant()));
            repository.save(applicationMetric);

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", locale);
            this.applicationData.setSubmissionTime(dateTimeFormatter.format(ZonedDateTime.ofInstant(clock.instant(), ZoneId.of("UTC"))));
            return new ModelAndView(String.format("redirect:/pages/%s/navigation", submitPage));
        } else {
            return new ModelAndView("formPage", Map.of(
                    "page", page,
                    "data", formData,
                    "pageName", submitPage,
                    "postTo", "/submit",
                    "pageTitle", page.resolve(pagesData, PageConfiguration::getPageTitle),
                    "headerKey", page.resolve(pagesData, PageConfiguration::getHeaderKey)
            ));
        }
    }
}
