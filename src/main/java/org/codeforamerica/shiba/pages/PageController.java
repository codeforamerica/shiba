package org.codeforamerica.shiba.pages;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.output.CompositeCondition;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.pages.config.*;
import org.codeforamerica.shiba.pages.data.*;
import org.codeforamerica.shiba.pages.enrichment.ApplicationEnrichment;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.SubworkflowCompletedEvent;
import org.codeforamerica.shiba.pages.events.SubworkflowIterationDeletedEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Clock;
import java.time.ZoneId;
import java.util.*;

import static java.util.Optional.ofNullable;

@Controller
@Slf4j
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
    private final ApplicationDataParser<List<Document>> documentListParser;
    private final FeatureFlagConfiguration featureFlags;
    private final UploadDocumentConfiguration uploadDocumentConfiguration;

    private final DocumentRepositoryService documentRepositoryService;

    public PageController(
            ApplicationConfiguration applicationConfiguration,
            ApplicationData applicationData,
            Clock clock,
            ApplicationRepository applicationRepository,
            ApplicationFactory applicationFactory,
            MessageSource messageSource,
            PageEventPublisher pageEventPublisher,
            ApplicationEnrichment applicationEnrichment,
            ApplicationDataParser<List<Document>> documentListParser,
            FeatureFlagConfiguration featureFlags,
            UploadDocumentConfiguration uploadDocumentConfiguration, DocumentRepositoryService documentRepositoryService) {
        this.applicationData = applicationData;
        this.applicationConfiguration = applicationConfiguration;
        this.clock = clock;
        this.applicationRepository = applicationRepository;
        this.applicationFactory = applicationFactory;
        this.messageSource = messageSource;
        this.pageEventPublisher = pageEventPublisher;
        this.applicationEnrichment = applicationEnrichment;
        this.documentListParser = documentListParser;
        this.featureFlags = featureFlags;
        this.uploadDocumentConfiguration = uploadDocumentConfiguration;
        this.documentRepositoryService= documentRepositoryService;
    }

    @GetMapping("/")
    ModelAndView getRoot() {
        return new ModelAndView("forward:/pages/" + applicationConfiguration.getLandmarkPages().getLandingPages().get(0));
    }

    @GetMapping("/privacy")
    String getPrivacyPolicy() {
        return "privacyPolicy";
    }

    @GetMapping("/faq")
    String getFaq() {
        return "faq";
    }

    @GetMapping("/pages/{pageName}/navigation")
    RedirectView navigation(
            @PathVariable String pageName,
            @RequestParam(required = false, defaultValue = "0") Integer option
    ) {
        PageWorkflowConfiguration pageWorkflow = this.applicationConfiguration.getPageWorkflow(pageName);
        PagesData pagesData = this.applicationData.getPagesData();
        NextPage nextPage = applicationData.getNextPageName(featureFlags, pageWorkflow, option);
        ofNullable(nextPage.getFlow()).ifPresent(applicationData::setFlow);
        PageWorkflowConfiguration nextPageWorkflow = this.applicationConfiguration.getPageWorkflow(nextPage.getPageName());

        if (shouldSkip(nextPageWorkflow)) {
            pagesData.remove(nextPageWorkflow.getPageConfiguration().getName());
            return new RedirectView(String.format("/pages/%s/navigation", nextPage.getPageName()));
        } else {
            return new RedirectView(String.format("/pages/%s", nextPage.getPageName()));
        }
    }

    private boolean shouldSkip(PageWorkflowConfiguration nextPageWorkflow) {
        CompositeCondition skipCondition = nextPageWorkflow.getSkipCondition();
        if (skipCondition != null) {
            PagesData pagesData = this.applicationData.getPagesData();
            Subworkflows subworkflows = this.applicationData.getSubworkflows();
            Map<String, PageData> pages = new HashMap<>();
            nextPageWorkflow.getDatasources().stream()
                    .filter(datasource -> datasource.getPageName() != null)
                    .forEach(datasource -> {
                        String key = datasource.getPageName();
                        PageData value = new PageData();
                        if (datasource.getGroupName() == null) {
                            value.mergeInputDataValues(pagesData.get(datasource.getPageName()));
                        } else if (subworkflows.containsKey(datasource.getGroupName())) {
                            subworkflows.get(datasource.getGroupName()).stream()
                                    .map(iteration -> iteration.getPagesData().getPage(datasource.getPageName()))
                                    .forEach(value::mergeInputDataValues);
                        }

                        pages.put(key, value);
                    });
            @NotNull DatasourcePages datasourcePages = new DatasourcePages(new PagesData(pages));

            return datasourcePages.satisfies(skipCondition);
        }
        return false;
    }

    @GetMapping("/pages/{pageName}")
    ModelAndView getPage(
            @PathVariable String pageName,
            @RequestParam(required = false, defaultValue = "") String iterationIndex,
            @RequestParam(name = "utm_source", defaultValue = "", required = false) String utmSource,
            HttpServletResponse response,
            HttpSession httpSession
    ) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();

        if (landmarkPagesConfiguration.isLandingPage(pageName)) {
            httpSession.invalidate();
        } else if (landmarkPagesConfiguration.isStartTimerPage(pageName)) {
            this.applicationData.setStartTimeOnce(clock.instant());
            if (!utmSource.isEmpty()) {
                this.applicationData.setUtmSource(utmSource);
            }
        }

        if (!landmarkPagesConfiguration.isPostSubmitPage(pageName) && applicationData.getId() != null) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getTerminalPage()));
        } else if (!landmarkPagesConfiguration.isLandingPage(pageName) && applicationData.getStartTime() == null) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getLandingPages().get(0)));
        }

        response.addHeader("Cache-Control", "no-store");

        if (this.applicationConfiguration.getPageWorkflow(pageName) == null) {
            return new ModelAndView("error/404");
        }
        PageWorkflowConfiguration pageWorkflow = this.applicationConfiguration.getPageWorkflow(pageName);

        PageConfiguration pageConfiguration = pageWorkflow.getPageConfiguration();

        PagesData pagesData = applicationData.getPagesData();
        if (pageWorkflow.getGroupName() != null) {
            PagesData currentIterationPagesData;
            String groupName = pageWorkflow.getGroupName();
            if (applicationConfiguration.getPageGroups().get(groupName).getStartPages().contains(pageName)) {
                currentIterationPagesData = applicationData.getIncompleteIterations().getOrDefault(groupName, new PagesData());
            } else {
                currentIterationPagesData = applicationData.getIncompleteIterations().get(groupName);
            }

            if (currentIterationPagesData == null) {
                String redirectPage = applicationConfiguration.getPageGroups().get(pageWorkflow.getGroupName()).getRedirectPage();
                return new ModelAndView(String.format("redirect:/pages/%s", redirectPage));
            }
            // Avoid changing the original applicationData PagesData by cloning the object
            pagesData = (PagesData) pagesData.clone();
            pagesData.putAll(currentIterationPagesData);
        }

        if (iterationIndex != null && !iterationIndex.isBlank() && applicationData.getSubworkflows().containsKey(pageWorkflow.getAppliesToGroup())) {

            PagesData iterationData = pageWorkflow.getSubworkflows(applicationData)
                    .get(pageWorkflow.getAppliesToGroup()).get(Integer.parseInt(iterationIndex)).getPagesData();

            pagesData = (PagesData) pagesData.clone();
            pagesData.putAll(iterationData);
        }

        PageTemplate pageTemplate = pagesData.evaluate(pageWorkflow, applicationData);

        HashMap<String, Object> model = new HashMap<>(Map.of(
                "page", pageTemplate,
                "pageName", pageName,
                "postTo", landmarkPagesConfiguration.isSubmitPage(pageName) ? "/submit" : "/pages/" + pageName
        ));

        if (landmarkPagesConfiguration.isTerminalPage(pageName)) {
            Application application = applicationRepository.find(applicationData.getId());
            model.put("applicationId", application.getId());
            model.put("documents", documentListParser.parse(applicationData));
            model.put("submissionTime", application.getCompletedAt().withZoneSameInstant(CENTRAL_TIMEZONE));
            model.put("county", application.getCounty());
            model.put("sentiment", application.getSentiment());
            model.put("feedbackText", application.getFeedback());
        }


        String pageToRender;
        if (pageConfiguration.isStaticPage()) {
            pageToRender = pageName;
            model.put("data", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()));
            model.put("applicationData", applicationData);

            if (landmarkPagesConfiguration.isUploadDocumentsPage(pageName)) {
                model.put("uploadedDocs", applicationData.getUploadedDocs());
                model.put("uploadDocMaxFileSize", uploadDocumentConfiguration.getMaxFilesize());
            }

            if (applicationData.hasRequiredSubworkflows(pageWorkflow.getDatasources())) {
                model.put("subworkflows", pageWorkflow.getSubworkflows(applicationData));
                if (iterationIndex != null && !iterationIndex.isBlank()) {
                    model.put("iterationData", pageWorkflow.getSubworkflows(applicationData)
                            .get(pageWorkflow.getAppliesToGroup()).get(Integer.parseInt(iterationIndex)));
                }
            } else {
                return new ModelAndView("redirect:/pages/" + pageWorkflow.getDataMissingRedirect());
            }
        } else {
            pageToRender = "formPage";
            model.put("pageDatasources", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()).mergeDatasourcePages(pagesData.getDatasourceGroupBy(pageWorkflow.getDatasources(), applicationData.getSubworkflows())));
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
            HttpSession httpSession
    ) {
        String nextPage;
        this.applicationData.getSubworkflows().get(groupName).remove(iteration);
        pageEventPublisher.publish(new SubworkflowIterationDeletedEvent(httpSession.getId(), groupName));

        if (this.applicationData.getSubworkflows().get(groupName).size() == 0) {
            this.applicationData.getSubworkflows().remove(groupName);
            nextPage = applicationConfiguration.getPageGroups().get(groupName).getRestartPage();
        } else {
            nextPage = applicationConfiguration.getPageGroups().get(groupName).getReviewPage();
        }

        return new ModelAndView(String.format("redirect:/pages/%s", nextPage));
    }

    @PostMapping("/groups/{groupName}/{iteration}/deleteWarning")
    ModelAndView deleteIterationWarning(
            @PathVariable String groupName,
            @PathVariable int iteration
    ) {
        String deleteWarningPage = applicationConfiguration.getPageGroups().get(groupName).getDeleteWarningPage();
        return new ModelAndView("redirect:/pages/" + deleteWarningPage + "?iterationIndex=" + iteration);
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
        Map<String, PagesData> incompleteIterations = applicationData.getIncompleteIterations();
        if (pageWorkflow.getGroupName() != null) {
            String groupName = pageWorkflow.getGroupName();
            if (applicationConfiguration.getPageGroups().get(groupName).getStartPages().contains(page.getName())) {
                incompleteIterations.putIfAbsent(groupName, new PagesData());
            }
            pagesData = incompleteIterations.get(groupName);
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
                    .addIteration(groupName, incompleteIterations.remove(groupName));
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
            applicationData.setId(applicationRepository.getNextId());
            Application application = applicationFactory.newApplication(applicationData);
            applicationRepository.save(application);
            pageEventPublisher.publish(
                    new ApplicationSubmittedEvent(httpSession.getId(), application.getId(), application.getFlow(), LocaleContextHolder.getLocale())
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

    @PostMapping("/document-upload")
    @ResponseStatus(HttpStatus.OK)
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("dataURL") String dataURL,
                       @RequestParam("type") String type) throws IOException, InterruptedException {
        if (this.applicationData.getUploadedDocs().size() <= 20 &&
                file.getSize() <= uploadDocumentConfiguration.getMaxFilesizeInBytes()) {
            String s3FilePath = String.format("%s/%s", applicationData.getId(), UUID.randomUUID());
            documentRepositoryService.upload(s3FilePath, file);
            this.applicationData.addUploadedDoc(file, s3FilePath, dataURL, type);
        }
    }

}
