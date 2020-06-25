package org.codeforamerica.shiba;

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

import static org.codeforamerica.shiba.FormData.getFormDataFrom;

@Controller
public class PageController {
    private final PagesData pagesData;
    private final PagesConfiguration pagesConfiguration;
    private final Clock clock;
    private final ApplicationMetricsRepository repository;
    private final SessionMetadata sessionMetadata;
    public static final String SIGN_THIS_APPLICATION_PAGE_NAME = "signThisApplication";

    public PageController(
            PagesConfiguration pagesConfiguration,
            PagesData pagesData,
            Clock clock,
            ApplicationMetricsRepository repository,
            SessionMetadata sessionMetadata) {
        this.pagesData = pagesData;
        this.pagesConfiguration = pagesConfiguration;
        this.clock = clock;
        this.repository = repository;
        this.sessionMetadata = sessionMetadata;
    }

    @GetMapping("/pages/{pageName}/navigation")
    RedirectView goBackToPage(
            @PathVariable String pageName,
            @RequestParam(defaultValue = "false") Boolean isBackwards
    ) {
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

        if (pageConfiguration.isStartTimer()) {
            sessionMetadata.setStartTimeOnce(clock.instant());
        }

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
            @PathVariable String pageName
    ) {
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

    @PostMapping("/submit")
    ModelAndView submitApplication(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            Locale locale
    ) {
        PageConfiguration page = pagesConfiguration.get(SIGN_THIS_APPLICATION_PAGE_NAME);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", locale);
        FormData formData = new FormData(Map.of(
                "applicantSignature", new InputData(Validation.NOT_BLANK, model.get("applicantSignature")),
                "submissionTime", new InputData(List.of(dateTimeFormatter.format(ZonedDateTime.ofInstant(clock.instant(), ZoneId.of("UTC")))))
        ));
        pagesData.putPage(SIGN_THIS_APPLICATION_PAGE_NAME, formData);

        ApplicationMetric applicationMetric = new ApplicationMetric(Duration.between(sessionMetadata.getStartTime(), clock.instant()));
        repository.save(applicationMetric);
        return formData.isValid() ?
                new ModelAndView(String.format("redirect:/pages/%s/navigation", SIGN_THIS_APPLICATION_PAGE_NAME)) :
                new ModelAndView(SIGN_THIS_APPLICATION_PAGE_NAME, Map.of(
                        "page", page,
                        "data", formData,
                        "pageName", SIGN_THIS_APPLICATION_PAGE_NAME)
                );
    }
}
