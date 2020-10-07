package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.pages.config.*;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.enrichment.ApplicationEnrichment;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.SubworkflowCompletedEvent;
import org.codeforamerica.shiba.pages.events.SubworkflowIterationDeletedEvent;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.Clock;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Controller
public class PageController {
    private static final ZoneId CENTRAL_TIMEZONE = ZoneId.of("America/Chicago");
    private final ApplicationData applicationData;
    private final ApplicationConfiguration applicationConfiguration;
    private final Clock clock;
    private final ApplicationRepository applicationRepository;
    private final ApplicationFactory applicationFactory;
    private final MessageSource messageSource;
    private final PageEventPublisher pageEventPublisher;
    private final ApplicationEnrichment applicationEnrichment;

    public PageController(
            ApplicationConfiguration applicationConfiguration,
            ApplicationData applicationData,
            Clock clock,
            ApplicationRepository applicationRepository,
            ApplicationFactory applicationFactory,
            MessageSource messageSource,
            PageEventPublisher pageEventPublisher,
            ApplicationEnrichment applicationEnrichment) {
        this.applicationData = applicationData;
        this.applicationConfiguration = applicationConfiguration;
        this.clock = clock;
        this.applicationRepository = applicationRepository;
        this.applicationFactory = applicationFactory;
        this.messageSource = messageSource;
        this.pageEventPublisher = pageEventPublisher;
        this.applicationEnrichment = applicationEnrichment;
    }

    @GetMapping("/")
    ModelAndView getRoot() {
        return new ModelAndView("forward:/pages/" + applicationConfiguration.getLandmarkPages().getLandingPages().get(0));
    }

    @GetMapping("/privacy")
    String getPrivacyPolicy() {
        return "privacyPolicy";
    }

    @GetMapping("/pages/{pageName}/navigation")
    RedirectView navigation(
            @PathVariable String pageName,
            @RequestParam(required = false, defaultValue = "0") Integer option
    ) {
        PageWorkflowConfiguration pageWorkflow = this.applicationConfiguration.getPageWorkflow(pageName);
        PagesData pagesData = this.applicationData.getPagesData();
        NextPage nextPage = applicationData.getNextPageName(pageWorkflow, option);
        ofNullable(nextPage.getFlow()).ifPresent(applicationData::setFlow);
        PageWorkflowConfiguration nextPageWorkflow = this.applicationConfiguration.getPageWorkflow(nextPage.getPageName());

        if (pagesData.shouldSkip(nextPageWorkflow)) {
            pagesData.remove(nextPageWorkflow.getPageConfiguration().getName());
            return new RedirectView(String.format("/pages/%s/navigation", nextPage.getPageName()));
        } else {
            return new RedirectView(String.format("/pages/%s", nextPage.getPageName()));
        }
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getPage(
            @PathVariable String pageName,
            HttpServletResponse response,
            HttpSession httpSession
    ) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();

        if (landmarkPagesConfiguration.isLandingPage(pageName)) {
            httpSession.invalidate();
        } else if (landmarkPagesConfiguration.isStartTimerPage(pageName)) {
            this.applicationData.setStartTimeOnce(clock.instant());
        }

        if (!landmarkPagesConfiguration.isTerminalPage(pageName) && applicationData.getId() != null) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getTerminalPage()));
        } else if (!landmarkPagesConfiguration.isLandingPage(pageName) && applicationData.getStartTime() == null) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getLandingPages().get(0)));
        }

        response.addHeader("Cache-Control", "no-store");

        PageWorkflowConfiguration pageWorkflow = this.applicationConfiguration.getPageWorkflow(pageName);
        PageConfiguration pageConfiguration = pageWorkflow.getPageConfiguration();

        PagesData pagesData;
        if (pageWorkflow.getGroupName() != null) {
            String groupName = pageWorkflow.getGroupName();
            if (applicationConfiguration.getPageGroups().get(groupName).getStartPage().equals(pageName)) {
                pagesData = applicationData.getIncompleteIterations().getOrDefault(groupName, new PagesData());
            } else {
                pagesData = applicationData.getIncompleteIterations().get(groupName);
            }

            if (pagesData == null) {
                String redirectPage = applicationConfiguration.getPageGroups().get(pageWorkflow.getGroupName()).getRedirectPage();
                return new ModelAndView(String.format("redirect:/pages/%s", redirectPage));
            }
        } else {
            pagesData = applicationData.getPagesData();
        }

        PageTemplate pageTemplate = pagesData.evaluate(pageWorkflow);

        HashMap<String, Object> model = new HashMap<>(Map.of(
                "page", pageTemplate,
                "pageName", pageName,
                "postTo", landmarkPagesConfiguration.isSubmitPage(pageName) ? "/submit" : "/pages/" + pageName
        ));

        if (landmarkPagesConfiguration.isTerminalPage(pageName)) {
            Application application = applicationRepository.find(applicationData.getId());
            model.put("applicationId", application.getId());
            model.put("submissionTime", application.getCompletedAt().withZoneSameInstant(CENTRAL_TIMEZONE));
            model.put("county", application.getCounty());
            model.put("sentiment", application.getSentiment());
            model.put("feedbackText", application.getFeedback());
        }

        String pageToRender;
        if (pageConfiguration.isStaticPage()) {
            pageToRender = pageName;
            model.put("data", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()));
            if (pageWorkflow.hasRequiredSubworkflows(applicationData)) {
                model.put("subworkflows", pageWorkflow.getSubworkflows(applicationData));
            } else {
                return new ModelAndView("redirect:/pages/" + pageWorkflow.getDataMissingRedirect());
            }
        } else {
            pageToRender = "formPage";
            model.put("pageDatasources", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()));
            model.put("data", pagesData.getPageDataOrDefault(pageTemplate.getName(), pageConfiguration));
        }
        return new ModelAndView(pageToRender, model);
    }

    @PostMapping("/groups/{groupName}/delete")
    RedirectView deleteGroup(@PathVariable String groupName, HttpSession httpSession) {
        this.applicationData.getSubworkflows().remove(groupName);
        pageEventPublisher.publish(new SubworkflowIterationDeletedEvent(httpSession.getId(), groupName));
        String startPage = applicationConfiguration.getPageGroups().get(groupName).getRestartPage();
        return new RedirectView("/pages/" + startPage);
    }

    @PostMapping("/groups/{groupName}/{iteration}/delete")
    ModelAndView deleteIteration(
            @PathVariable String groupName,
            @PathVariable int iteration,
            @RequestHeader("referer") String referer,
            HttpSession httpSession
    ) {
        Optional<String> noDataRedirectPage = ofNullable(applicationConfiguration.getPageGroups().get(groupName).getNoDataRedirectPage());
        if (this.applicationData.getSubworkflows().get(groupName).size() == 1 && noDataRedirectPage.isPresent()) {
            return new ModelAndView("redirect:/pages/" + noDataRedirectPage.get());
        } else {
            this.applicationData.getSubworkflows().get(groupName).remove(iteration);
            pageEventPublisher.publish(new SubworkflowIterationDeletedEvent(httpSession.getId(), groupName));
            return new ModelAndView("redirect:" + referer);
        }
    }

    @PostMapping("/pages/{pageName}")
    ModelAndView postFormPage(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            @PathVariable String pageName,
            HttpSession httpSession
    ) {
        PageWorkflowConfiguration pageWorkflow = applicationConfiguration.getPageWorkflow(pageName);

        PageConfiguration page = pageWorkflow.getPageConfiguration();
        PageData pageData = PageData.fillOut(page, model);

        PagesData pagesData;
        if (pageWorkflow.getGroupName() != null) {
            String groupName = pageWorkflow.getGroupName();
            if (applicationConfiguration.getPageGroups().get(groupName).getStartPage().equals(page.getName())) {
                applicationData.getIncompleteIterations().put(groupName, new PagesData());
            }
            pagesData = applicationData.getIncompleteIterations().get(groupName);
        } else {
            pagesData = applicationData.getPagesData();
        }

        pagesData.putPage(page.getName(), pageData);

        if (pageData.isValid() &&
                pageWorkflow.getGroupName() != null &&
                applicationConfiguration.getPageGroups().get(pageWorkflow.getGroupName()).getCompletePages().contains(page.getName())
        ) {
            String groupName = pageWorkflow.getGroupName();
            applicationData.getSubworkflows()
                    .addIteration(groupName, applicationData.getIncompleteIterations().remove(groupName));
            pageEventPublisher.publish(new SubworkflowCompletedEvent(httpSession.getId(), groupName));
        }

        if (pageData.isValid()) {
            ofNullable(pageWorkflow.getEnrichment())
                    .map(applicationEnrichment::getEnrichment)
                    .map(enrichment -> enrichment.process(applicationData))
                    .ifPresent(pageData::putAll);
            return new ModelAndView(String.format("redirect:/pages/%s/navigation", pageName));
        } else {
            return new ModelAndView("redirect:/pages/" + pageName);
        }
    }

    @PostMapping("/submit")
    ModelAndView submitApplication(
            @RequestBody(required = false) MultiValueMap<String, String> model,
            HttpSession httpSession
    ) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = this.applicationConfiguration.getLandmarkPages();
        String submitPage = landmarkPagesConfiguration.getSubmitPage();
        PageConfiguration page = applicationConfiguration.getPageWorkflow(submitPage).getPageConfiguration();

        PageData pageData = PageData.fillOut(page, model);
        PagesData pagesData = applicationData.getPagesData();
        pagesData.putPage(submitPage, pageData);

        if (pageData.isValid()) {
            String id = applicationRepository.getNextId();
            Application application = applicationFactory.newApplication(id, applicationData);
            applicationData.setId(application.getId());
            applicationRepository.save(application);
            pageEventPublisher.publish(
                    new ApplicationSubmittedEvent(httpSession.getId(), application.getId(), application.getFlow())
            );

            return new ModelAndView(String.format("redirect:/pages/%s/navigation", submitPage));
        } else {
            return new ModelAndView("redirect:/pages/" + submitPage);
        }
    }

    @PostMapping("/submit-feedback")
    RedirectView submitFeedback(Feedback feedback,
                                RedirectAttributes redirectAttributes,
                                Locale locale) {
        String terminalPage = applicationConfiguration.getLandmarkPages().getTerminalPage();
        if (applicationData.getId() == null) {
            return new RedirectView("/pages/" + terminalPage);
        }
        String message = messageSource.getMessage(feedback.getMessageKey(), null, locale);
        if (feedback.isInvalid()) {
            redirectAttributes.addFlashAttribute("feedbackFailure", message);
            return new RedirectView("/pages/" + terminalPage);
        }
        redirectAttributes.addFlashAttribute("feedbackSuccess", message);
        Application application = applicationRepository.find(applicationData.getId());
        Application updatedApplication = application.addFeedback(feedback);
        applicationRepository.save(updatedApplication);
        return new RedirectView("/pages/" + terminalPage);
    }

}
