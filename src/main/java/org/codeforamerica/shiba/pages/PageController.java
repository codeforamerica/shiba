package org.codeforamerica.shiba.pages;

import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.ApplicationStatusUpdater;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.pages.config.*;
import org.codeforamerica.shiba.pages.data.*;
import org.codeforamerica.shiba.pages.enrichment.ApplicationEnrichment;
import org.codeforamerica.shiba.pages.events.*;
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
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;

@Controller
@Slf4j
public class PageController {
    private static final ZoneId CENTRAL_TIMEZONE = ZoneId.of("America/Chicago");
    private final int MAX_FILES_UPLOADED = 20;
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
    private final CountyParser countyParser;
    private final SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
    private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;
    private final SuccessMessageService successMessageService;
    private final DocumentRepositoryService documentRepositoryService;
    private final ApplicationStatusUpdater applicationStatusUpdater;

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
            UploadDocumentConfiguration uploadDocumentConfiguration,
            DocumentRepositoryService documentRepositoryService,
            CountyParser countyParser,
            SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider,
            CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider,
            SuccessMessageService successMessageService, ApplicationStatusUpdater applicationStatusUpdater) {
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
        this.documentRepositoryService = documentRepositoryService;
        this.countyParser = countyParser;
        this.snapExpeditedEligibilityDecider = snapExpeditedEligibilityDecider;
        this.ccapExpeditedEligibilityDecider = ccapExpeditedEligibilityDecider;
        this.successMessageService = successMessageService;
        this.applicationStatusUpdater = applicationStatusUpdater;
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
        PageWorkflowConfiguration pageWorkflow = applicationConfiguration.getPageWorkflow(pageName);
        PagesData pagesData = applicationData.getPagesData();
        NextPage nextPage = applicationData.getNextPageName(featureFlags, pageWorkflow, option);
        ofNullable(nextPage.getFlow()).ifPresent(applicationData::setFlow);
        PageWorkflowConfiguration nextPageWorkflow = applicationConfiguration.getPageWorkflow(nextPage.getPageName());

        if (shouldSkip(nextPageWorkflow)) {
            pagesData.remove(nextPageWorkflow.getPageConfiguration().getName());
            return new RedirectView(String.format("/pages/%s/navigation", nextPage.getPageName()));
        } else {
            return new RedirectView(String.format("/pages/%s", nextPage.getPageName()));
        }
    }

    private boolean shouldSkip(PageWorkflowConfiguration nextPageWorkflow) {
        Condition skipCondition = nextPageWorkflow.getSkipCondition();
        if (skipCondition != null) {
            PagesData pagesData = applicationData.getPagesData();
            Subworkflows subworkflows = applicationData.getSubworkflows();
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
            HttpSession httpSession,
            Locale locale
    ) {
        var landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();

        applicationData.setLastPage(pageName);
        log.info("Last page visited is, ", applicationData.getLastPage());

        // Validations and special case redirects
        if (landmarkPagesConfiguration.isLandingPage(pageName)) {
            httpSession.invalidate();
        }

        if (landmarkPagesConfiguration.isStartTimerPage(pageName)) {
            applicationData.setStartTimeOnce(clock.instant());
            if (!utmSource.isEmpty()) {
                applicationData.setUtmSource(utmSource);
            }
        }

        if (applicationConfiguration.getLandmarkPages().isUploadDocumentsPage(pageName)) {
            applicationStatusUpdater.updateUploadedDocumentsStatus(applicationData.getId(), IN_PROGRESS);
        }

        if (shouldRedirectToTerminalPage(pageName)) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getTerminalPage()));
        }

        if (shouldRedirectToLandingPage(pageName)) {
            return new ModelAndView(String.format("redirect:/pages/%s", landmarkPagesConfiguration.getLandingPages().get(0)));
        }

        response.addHeader("Cache-Control", "no-store");

        if (notFound(pageName)) {
            return new ModelAndView("error/404");
        }

        var pageWorkflowConfig = applicationConfiguration.getPageWorkflow(pageName);
        if (missingRequiredSubworkflows(pageWorkflowConfig)) {
            return new ModelAndView("redirect:/pages/" + pageWorkflowConfig.getDataMissingRedirect());
        }

        // Update pagesData with data for incomplete subworkflows
        var pagesData = applicationData.getPagesData();
        if (pageWorkflowConfig.getGroupName() != null) { // If page is part of a group
            var dataForIncompleteIteration = getIncompleteIterationPagesData(pageName, pageWorkflowConfig);

            if (dataForIncompleteIteration == null) {
                String redirectPageForGroup = applicationConfiguration.getPageGroups().get(pageWorkflowConfig.getGroupName()).getRedirectPage();
                return new ModelAndView("redirect:/pages/" + redirectPageForGroup);
            }
            pagesData = (PagesData) pagesData.clone(); // Avoid changing the original applicationData PagesData by cloning the object
            pagesData.putAll(dataForIncompleteIteration);
        }

        // Add extra pagesData if this page workflow specifies that it applies to a group
        if (requestedPageAppliesToGroup(iterationIndex, pageWorkflowConfig)) {
            String groupName = pageWorkflowConfig.getAppliesToGroup();
            var dataForGroup = getPagesDataForGroupAndIteration(iterationIndex, pageWorkflowConfig, groupName);

            pagesData = (PagesData) pagesData.clone();
            pagesData.putAll(dataForGroup);
        }

        var pageTemplate = pagesData.evaluate(featureFlags, pageWorkflowConfig, applicationData);

        var model = buildModelForThymeleaf(pageName, locale, landmarkPagesConfiguration, pageTemplate, pageWorkflowConfig, pagesData, iterationIndex);
        var view = pageWorkflowConfig.getPageConfiguration().isStaticPage() ? pageName : "formPage";
        return new ModelAndView(view, model);
    }

    private PagesData getPagesDataForGroupAndIteration(String iterationIndex, PageWorkflowConfiguration pageWorkflowConfig, String groupName) {
        return pageWorkflowConfig.getSubworkflows(applicationData)
                .get(groupName)
                .get(Integer.parseInt(iterationIndex))
                .getPagesData();
    }

    private PagesData getIncompleteIterationPagesData(String pageName, PageWorkflowConfiguration pageWorkflow) {
        PagesData currentIterationPagesData;
        String groupName = pageWorkflow.getGroupName();
        if (isStartPageForGroup(pageName, groupName)) {
            currentIterationPagesData = applicationData.getIncompleteIterations().getOrDefault(groupName, new PagesData());
        } else {
            currentIterationPagesData = applicationData.getIncompleteIterations().get(groupName);
        }
        return currentIterationPagesData;
    }

    private boolean missingRequiredSubworkflows(PageWorkflowConfiguration pageWorkflow) {
        return pageWorkflow.getPageConfiguration().isStaticPage() && !applicationData.hasRequiredSubworkflows(pageWorkflow.getDatasources());
    }

    private boolean isStartPageForGroup(@PathVariable String pageName, String groupName) {
        return applicationConfiguration.getPageGroups().get(groupName).getStartPages().contains(pageName);
    }

    @NotNull
    private Map<String, Object> buildModelForThymeleaf(String pageName, Locale locale, LandmarkPagesConfiguration landmarkPagesConfiguration, PageTemplate pageTemplate, PageWorkflowConfiguration pageWorkflow, PagesData pagesData, String iterationIndex) {
        HashMap<String, Object> model = new HashMap<>(Map.of(
                "page", pageTemplate,
                "pageName", pageName,
                "postTo", landmarkPagesConfiguration.isSubmitPage(pageName) ? "/submit" : "/pages/" + pageName
        ));

        model.put("county", countyParser.parse(applicationData));

        List<String> zipCode = applicationData.getPagesData().safeGetPageInputValue("homeAddress", "zipCode");
        if (!zipCode.isEmpty()) {
            model.put("zipCode", zipCode.get(0));
        }

        Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms();
        if (!programs.isEmpty()) {
            model.put("programs", String.join(", ", programs));
        }

        var snapExpeditedEligibility = snapExpeditedEligibilityDecider.decide(applicationData);
        model.put("expeditedSnap", snapExpeditedEligibility);
        var ccapExpeditedEligibility = ccapExpeditedEligibilityDecider.decide(applicationData);
        model.put("expeditedCcap", ccapExpeditedEligibility);

        if (landmarkPagesConfiguration.isTerminalPage(pageName)) {
            Application application = applicationRepository.find(applicationData.getId());
            model.put("applicationId", application.getId());
            model.put("documents", documentListParser.parse(applicationData));
            model.put("hasUploadDocuments", !applicationData.getUploadedDocs().isEmpty());
            model.put("submissionTime", application.getCompletedAt().withZoneSameInstant(CENTRAL_TIMEZONE));
            model.put("county", application.getCounty());
            model.put("sentiment", application.getSentiment());
            model.put("feedbackText", application.getFeedback());
            String inputData = pagesData.getPageInputFirstValue("healthcareCoverage", "healthcareCoverage");
            boolean hasHealthcare = "YES".equalsIgnoreCase(inputData);
            model.put("doesNotHaveHealthcare", !hasHealthcare );
            model.put("successMessage", successMessageService.getSuccessMessage(new ArrayList<>(programs), snapExpeditedEligibility, ccapExpeditedEligibility, locale));
        }

        if (landmarkPagesConfiguration.isLaterDocsTerminalPage(pageName)) {
            model.put("applicationId", applicationData.getId());
        }

        if (landmarkPagesConfiguration.isUploadDocumentsPage(pageName)) {
            model.put("uploadedDocs", applicationData.getUploadedDocs());
            model.put("uploadDocMaxFileSize", uploadDocumentConfiguration.getMaxFilesize());
        }

        if (pageWorkflow.getPageConfiguration().isStaticPage()) {
            model.put("data", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()));
            model.put("applicationData", applicationData);

            if (applicationData.hasRequiredSubworkflows(pageWorkflow.getDatasources())) {
                model.put("subworkflows", pageWorkflow.getSubworkflows(applicationData));
                if (isNotBlank(iterationIndex)) {
                    var iterationData = pageWorkflow.getSubworkflows(applicationData).get(pageWorkflow.getAppliesToGroup()).get(Integer.parseInt(iterationIndex));
                    model.put("iterationData", iterationData);
                }
            }
        } else {
            model.put("pageDatasources", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()).mergeDatasourcePages(pagesData.getDatasourceGroupBy(pageWorkflow.getDatasources(), applicationData.getSubworkflows())));
            model.put("data", pagesData.getPageDataOrDefault(pageTemplate.getName(), pageWorkflow.getPageConfiguration()));
        }

        return model;
    }

    private boolean requestedPageAppliesToGroup(String iterationIndex, PageWorkflowConfiguration pageWorkflow) {
        return isNotBlank(iterationIndex) && applicationData.getSubworkflows().containsKey(pageWorkflow.getAppliesToGroup());
    }

    private boolean notFound(String pageName) {
        return applicationConfiguration.getPageWorkflow(pageName) == null;
    }

    private boolean shouldRedirectToLandingPage(@PathVariable String pageName) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();
        // If they requested landing page or application is unstarted
        return !landmarkPagesConfiguration.isLandingPage(pageName) && applicationData.getStartTime() == null;
    }

    private boolean shouldRedirectToTerminalPage(@PathVariable String pageName) {
        LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();
        // If not on post-submit page and application is already submitted
        return !landmarkPagesConfiguration.isPostSubmitPage(pageName) && applicationData.isSubmitted();
    }

    @PostMapping("/groups/{groupName}/delete")
    RedirectView deleteGroup(@PathVariable String groupName, HttpSession httpSession) {
        applicationData.getSubworkflows().remove(groupName);
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
        applicationData.getSubworkflows().get(groupName).remove(iteration);
        pageEventPublisher.publish(new SubworkflowIterationDeletedEvent(httpSession.getId(), groupName));

        if (applicationData.getSubworkflows().get(groupName).isEmpty()) {
            applicationData.getSubworkflows().remove(groupName);
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
        // go get applicationdata from the db
        // if it's there, use that as application data
        // if not use the one in the session
        // add a big TODO ^^^^^ Delete after next deploy
        PageWorkflowConfiguration pageWorkflow = applicationConfiguration.getPageWorkflow(pageName);

        PageConfiguration page = pageWorkflow.getPageConfiguration();
        PageData pageData = PageData.fillOut(page, model);

        PagesData pagesData;
        Map<String, PagesData> incompleteIterations = applicationData.getIncompleteIterations();
        if (pageWorkflow.getGroupName() != null) {
            String groupName = pageWorkflow.getGroupName();
            if (isStartPageForGroup(page.getName(), groupName)) {
                incompleteIterations.putIfAbsent(groupName, new PagesData());
            }
            pagesData = incompleteIterations.get(groupName);
        } else {
            pagesData = applicationData.getPagesData();
        }

        pagesData.putPage(page.getName(), pageData);

        Boolean pageDataIsValid = pageData.isValid();
        if (pageDataIsValid &&
                pageWorkflow.getGroupName() != null &&
                applicationConfiguration.getPageGroups().get(pageWorkflow.getGroupName()).getCompletePages().contains(page.getName())
        ) {
            String groupName = pageWorkflow.getGroupName();
            applicationData.getSubworkflows()
                    .addIteration(groupName, incompleteIterations.remove(groupName));
            pageEventPublisher.publish(new SubworkflowCompletedEvent(httpSession.getId(), groupName));
        }

        if (applicationConfiguration.getLandmarkPages().isLaterDocsIdPage(pageName)) {
            applicationData.setFlow(LATER_DOCS);
            if (applicationData.getId() == null) {
                applicationData.setId(applicationRepository.getNextId());
            }
            Application application = applicationFactory.newApplication(applicationData);
            applicationRepository.save(application); //upsert already
        }

        if (pageDataIsValid) {
            if (pagesData.containsKey("choosePrograms")) {
                if (applicationData.isCAFApplication()) {
                    applicationStatusUpdater.updateCafApplicationStatus(applicationData.getId(), IN_PROGRESS);
                }
                if (applicationData.isCCAPApplication()) {
                    applicationStatusUpdater.updateCcapApplicationStatus(applicationData.getId(), IN_PROGRESS);
                }
            }
            if (applicationData.getId() == null) {
                applicationData.setId(applicationRepository.getNextId());
            }
            Application application = applicationFactory.newApplication(applicationData);
            applicationRepository.save(application); //upsert already

            // TODO this should happen before we save the thing maybe
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
        LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();
        String submitPage = landmarkPagesConfiguration.getSubmitPage();
        PageConfiguration page = applicationConfiguration.getPageWorkflow(submitPage).getPageConfiguration();

        PageData pageData = PageData.fillOut(page, model);
        PagesData pagesData = applicationData.getPagesData();
        pagesData.putPage(submitPage, pageData);

        if (pageData.isValid()) {
            if (applicationData.getId() == null) {
                // only happens in framework tests now we think, left in out of an abundance of caution
                applicationData.setId(applicationRepository.getNextId());
            }
            Application application = applicationFactory.newApplication(applicationData);
            applicationRepository.save(application);
            pageEventPublisher.publish(
                    new ApplicationSubmittedEvent(httpSession.getId(), application.getId(), application.getFlow(), LocaleContextHolder.getLocale())
            );
            applicationData.setSubmitted(true);
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
        if (application != null) {
            Application updatedApplication = application.addFeedback(feedback);
            applicationRepository.save(updatedApplication);
        }
        return new RedirectView("/pages/" + terminalPage);
    }

    @PostMapping("/document-upload")
    @ResponseStatus(HttpStatus.OK)
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("dataURL") String dataURL,
                       @RequestParam("type") String type) throws IOException, InterruptedException {
        if (applicationData.getUploadedDocs().size() <= MAX_FILES_UPLOADED &&
                file.getSize() <= uploadDocumentConfiguration.getMaxFilesizeInBytes()) {
            String s3FilePath = String.format("%s/%s", applicationData.getId(), UUID.randomUUID());
            documentRepositoryService.upload(s3FilePath, file);
            applicationData.addUploadedDoc(file, s3FilePath, dataURL, type);
        }
    }

    @PostMapping("/submit-documents")
    ModelAndView submitDocuments(HttpSession httpSession) {
        if (featureFlags.get("submit-via-api").isOn()) {
            Application application = applicationRepository.find(applicationData.getId());
            application.getApplicationData().setUploadedDocs(applicationData.getUploadedDocs());
            applicationRepository.save(application);
            pageEventPublisher.publish(new UploadedDocumentsSubmittedEvent(httpSession.getId(), application.getId(), LocaleContextHolder.getLocale()));
        }
        LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();
        String nextPage = landmarkPagesConfiguration.getTerminalPage();
        if (applicationData.getFlow() == LATER_DOCS) {
            nextPage = landmarkPagesConfiguration.getLaterDocsTerminalPage();
        }

        return new ModelAndView(String.format("redirect:/pages/%s", nextPage));
    }

    @SuppressWarnings("SpringMVCViewInspection")
    @PostMapping("/remove-upload/{filename}")
    ModelAndView removeUpload(@PathVariable String filename) {
        applicationData.getUploadedDocs().stream()
                .filter(uploadedDocument -> uploadedDocument.getFilename().equals(filename))
                .map(UploadedDocument::getS3Filepath)
                .findFirst()
                .ifPresent(documentRepositoryService::delete);
        applicationData.removeUploadedDoc(filename);

        return new ModelAndView("redirect:/pages/uploadDocuments");
    }
}
