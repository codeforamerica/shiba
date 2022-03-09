package org.codeforamerica.shiba.pages;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOME_ZIPCODE;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.codeforamerica.shiba.Program;
import org.codeforamerica.shiba.RoutingDestinationMessageService;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.DocumentStatusRepository;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.config.LandmarkPagesConfiguration;
import org.codeforamerica.shiba.pages.config.NextPage;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.config.PageTemplate;
import org.codeforamerica.shiba.pages.config.PageWorkflowConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.DatasourcePages;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.pages.enrichment.ApplicationEnrichment;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.SubworkflowCompletedEvent;
import org.codeforamerica.shiba.pages.events.SubworkflowIterationDeletedEvent;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PageController {

  private static final ZoneId CENTRAL_TIMEZONE = ZoneId.of("America/Chicago");
  private static final int MAX_FILES_UPLOADED = 20;
  private final ApplicationData applicationData;
  private final ApplicationConfiguration applicationConfiguration;
  private final Clock clock;
  private final ApplicationRepository applicationRepository;
  private final ApplicationFactory applicationFactory;
  private final MessageSource messageSource;
  private final PageEventPublisher pageEventPublisher;
  private final ApplicationEnrichment applicationEnrichment;
  private final FeatureFlagConfiguration featureFlags;
  private final UploadDocumentConfiguration uploadDocumentConfiguration;
  private final CityInfoConfiguration cityInfoConfiguration;
  private final SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
  private final CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;
  private final NextStepsContentService nextStepsContentService;
  private final DocRecommendationMessageService docRecommendationMessageService;
  private final RoutingDecisionService routingDecisionService;
  private final DocumentRepository documentRepository;
  private final RoutingDestinationMessageService routingDestinationMessageService;
  private final DocumentStatusRepository documentStatusRepository;

  public PageController(
      ApplicationConfiguration applicationConfiguration,
      ApplicationData applicationData,
      Clock clock,
      ApplicationFactory applicationFactory,
      MessageSource messageSource,
      PageEventPublisher pageEventPublisher,
      ApplicationEnrichment applicationEnrichment,
      FeatureFlagConfiguration featureFlags,
      UploadDocumentConfiguration uploadDocumentConfiguration,
      CityInfoConfiguration cityInfoConfiguration,
      SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider,
      CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider,
      NextStepsContentService nextStepsContentService,
      DocRecommendationMessageService docRecommendationMessageService,
      RoutingDecisionService routingDecisionService,
      DocumentRepository documentRepository,
      ApplicationRepository applicationRepository,
      RoutingDestinationMessageService routingDestinationMessageService,
      DocumentStatusRepository documentStatusRepository) {
    this.applicationData = applicationData;
    this.applicationConfiguration = applicationConfiguration;
    this.clock = clock;
    this.applicationFactory = applicationFactory;
    this.messageSource = messageSource;
    this.pageEventPublisher = pageEventPublisher;
    this.applicationEnrichment = applicationEnrichment;
    this.featureFlags = featureFlags;
    this.uploadDocumentConfiguration = uploadDocumentConfiguration;
    this.cityInfoConfiguration = cityInfoConfiguration;
    this.snapExpeditedEligibilityDecider = snapExpeditedEligibilityDecider;
    this.ccapExpeditedEligibilityDecider = ccapExpeditedEligibilityDecider;
    this.nextStepsContentService = nextStepsContentService;
    this.docRecommendationMessageService = docRecommendationMessageService;
    this.routingDecisionService = routingDecisionService;
    this.documentRepository = documentRepository;
    this.applicationRepository = applicationRepository;
    this.routingDestinationMessageService = routingDestinationMessageService;
    this.documentStatusRepository = documentStatusRepository;
  }

  @GetMapping("/")
  ModelAndView getRoot() {
    return new ModelAndView(
        "forward:/pages/" + applicationConfiguration.getLandmarkPages().getLandingPages()
            .get(0));
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
    PageWorkflowConfiguration currentPage = applicationConfiguration.getPageWorkflow(pageName);
    if (currentPage == null) {
      return new RedirectView("/error");
    }

    PagesData pagesData = applicationData.getPagesData();
    NextPage nextPage = applicationData.getNextPageName(featureFlags, currentPage, option);
    ofNullable(nextPage.getFlow()).ifPresent(applicationData::setFlow);
    PageWorkflowConfiguration nextPageWorkflow = applicationConfiguration
        .getPageWorkflow(nextPage.getPageName());

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
      PagesData pagesData = applicationData.getDatasourceDataForPageIncludingSubworkflows(
          nextPageWorkflow);
      DatasourcePages datasourcePages = new DatasourcePages(pagesData);
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

    if (landmarkPagesConfiguration.isLandingPage(pageName)) {
      httpSession.invalidate();
    }

    if (landmarkPagesConfiguration.isStartTimerPage(pageName)) {
      applicationData.setStartTimeOnce(clock.instant());
      if (!utmSource.isEmpty()) {
        applicationData.setUtmSource(utmSource);
      }
    }

    // Validations and special case redirects
    if (shouldRedirectToTerminalPage(pageName)) {
      return new ModelAndView(
          String.format("redirect:/pages/%s", landmarkPagesConfiguration.getTerminalPage()));
    }

    if (shouldRedirectToLandingPage(pageName)) {
      return new ModelAndView(
          String.format("redirect:/pages/%s",
              landmarkPagesConfiguration.getLandingPages().get(0)));
    }

    var pageWorkflowConfig = applicationConfiguration.getPageWorkflow(pageName);
    if (pageWorkflowConfig == null) {
      return new ModelAndView("redirect:/error");
    }

    if (missingRequiredSubworkflows(pageWorkflowConfig)) {
      return new ModelAndView(
          "redirect:/pages/" + pageWorkflowConfig.getDataMissingRedirect());
    }

    // Update pagesData with data for incomplete subworkflows
    var pagesData = applicationData.getPagesData();
    if (pageWorkflowConfig.getGroupName() != null) { // If page is part of a group
      var dataForIncompleteIteration = getIncompleteIterationPagesData(pageName,
          pageWorkflowConfig);

      if (dataForIncompleteIteration == null) {
        String redirectPageForGroup = applicationConfiguration.getPageGroups()
            .get(pageWorkflowConfig.getGroupName()).getRedirectPage();
        return new ModelAndView("redirect:/pages/" + redirectPageForGroup);
      }
      pagesData = (PagesData) pagesData
          .clone(); // Avoid changing the original applicationData PagesData by cloning the object
      pagesData.putAll(dataForIncompleteIteration);
    }

    // Add extra pagesData if this page workflow specifies that it applies to a group
    if (requestedPageAppliesToGroup(iterationIndex, pageWorkflowConfig)) {
      String groupName = pageWorkflowConfig.getAppliesToGroup();
      if (Integer.parseInt(iterationIndex) < applicationData.getSubworkflows().get(groupName)
          .size()) {
        var dataForGroup = getPagesDataForGroupAndIteration(iterationIndex,
            pageWorkflowConfig,
            groupName);

        pagesData = (PagesData) pagesData.clone();
        pagesData.putAll(dataForGroup);
      } else {
        return new ModelAndView(
            "redirect:/pages/" + applicationConfiguration.getPageGroups().get(groupName)
                .getReviewPage());
      }
    }

    var pageTemplate = pagesData.evaluate(featureFlags, pageWorkflowConfig, applicationData);

    var model = buildModelForThymeleaf(pageName, locale, landmarkPagesConfiguration,
        pageTemplate,
        pageWorkflowConfig, pagesData, iterationIndex);
    var view =
        pageWorkflowConfig.getPageConfiguration().isUsingPageTemplateFragment() ? "pageTemplate"
            : pageName;
    return new ModelAndView(view, model);
  }

  private PagesData getPagesDataForGroupAndIteration(String iterationIndex,
      PageWorkflowConfiguration pageWorkflowConfig, String groupName) {
    return pageWorkflowConfig.getSubworkflows(applicationData)
        .get(groupName)
        .get(Integer.parseInt(iterationIndex))
        .getPagesData();
  }

  private PagesData getIncompleteIterationPagesData(String pageName,
      PageWorkflowConfiguration pageWorkflow) {
    PagesData currentIterationPagesData;
    String groupName = pageWorkflow.getGroupName();
    if (isStartPageForGroup(pageName, groupName)) {
      currentIterationPagesData = applicationData.getIncompleteIterations()
          .getOrDefault(groupName, new PagesData());
    } else {
      currentIterationPagesData = applicationData.getIncompleteIterations().get(groupName);
    }
    return currentIterationPagesData;
  }

  private boolean missingRequiredSubworkflows(PageWorkflowConfiguration pageWorkflow) {
    return pageWorkflow.getPageConfiguration().getInputs().isEmpty() &&
           !applicationData.hasRequiredSubworkflows(pageWorkflow.getDatasources());
  }

  private boolean isStartPageForGroup(@PathVariable String pageName, String groupName) {
    return applicationConfiguration.getPageGroups().get(groupName).getStartPages()
        .contains(pageName);
  }

  @NotNull
  private Map<String, Object> buildModelForThymeleaf(String pageName, Locale locale,
      LandmarkPagesConfiguration landmarkPagesConfiguration, PageTemplate pageTemplate,
      PageWorkflowConfiguration pageWorkflow, PagesData pagesData, String iterationIndex) {
    Map<String, Object> model = new HashMap<>();
    model.put("page", pageTemplate);
    model.put("pageName", pageName);
    model.put("postTo",
        landmarkPagesConfiguration.isSubmitPage(pageName) ? "/submit" : "/pages/" + pageName);
    model.put("applicationId", applicationData.getId());
    model.put("county", CountyParser.parse(applicationData));
    model.put("cityInfo", cityInfoConfiguration.getCityToZipAndCountyMapping());
    model.put("zipCode", getFirstValue(applicationData.getPagesData(), HOME_ZIPCODE));
    model.put("featureFlags", featureFlags);

    var snapExpeditedEligibility = snapExpeditedEligibilityDecider.decide(applicationData);
    model.put("expeditedSnap", snapExpeditedEligibility);
    var ccapExpeditedEligibility = ccapExpeditedEligibilityDecider.decide(applicationData);
    model.put("expeditedCcap", ccapExpeditedEligibility);

    if (pageWorkflow.getPageConfiguration().isStaticPage()) {
      model.put("pageNameContext", pageName);
    }

    Set<String> programs = applicationData.getApplicantAndHouseholdMemberPrograms();
    if (!programs.isEmpty()) {
      model.put("programs", String.join(", ", programs));
    }
    model.put("totalMilestones", programs.contains(Program.CERTAIN_POPS) ? "7" : "6");

    if (landmarkPagesConfiguration.isPostSubmitPage(pageName)) {
      model.put("docRecommendations", docRecommendationMessageService
          .getPageSpecificRecommendationsMessage(applicationData, locale));
      model.put("nextStepSections", nextStepsContentService
          .getNextSteps(new ArrayList<>(programs), snapExpeditedEligibility,
              ccapExpeditedEligibility, locale));
    }

    if (landmarkPagesConfiguration.isTerminalPage(pageName)) {
      Application application = applicationRepository.find(applicationData.getId());
      model.put("documents", DocumentListParser.parse(application.getApplicationData()));
      model.put("hasUploadDocuments", !applicationData.getUploadedDocs().isEmpty());
      model.put("submissionTime",
          application.getCompletedAt().withZoneSameInstant(CENTRAL_TIMEZONE));
      model.put("sentiment", application.getSentiment());
      model.put("feedbackText", application.getFeedback());
      model.put("combinedFormText", applicationData.combinedApplicationProgramsList());
      String inputData = pagesData
          .getPageInputFirstValue("healthcareCoverage", "healthcareCoverage");
      boolean hasHealthcare = "YES".equalsIgnoreCase(inputData);
      model.put("doesNotHaveHealthcare", !hasHealthcare);

      // Get all routing destinations for this application
      Set<RoutingDestination> routingDestinations = new LinkedHashSet<>();
      DocumentListParser.parse(applicationData).forEach(doc -> {
        List<RoutingDestination> routingDestinationsForThisDoc =
            routingDecisionService.getRoutingDestinations(applicationData, doc);
        routingDestinations.addAll(routingDestinationsForThisDoc);
      });

      applicationRepository.save(application);

      // Generate human-readable list of routing destinations for success page
      String finalDestinationList = routingDestinationMessageService.generatePhrase(locale,
          application.getCounty(),
          true,
          new ArrayList<>(routingDestinations));
      model.put("routingDestinationList", finalDestinationList);
    }

    if (landmarkPagesConfiguration.isUploadDocumentsPage(pageName)) {
      record DocWithThumbnail(UploadedDocument doc, String thumbnail) {

      }
      List<DocWithThumbnail> uploadedDocsWithThumbnails = applicationData.getUploadedDocs().stream()
          .parallel()
          .map(doc -> new DocWithThumbnail(doc, documentRepository.getThumbnail(doc)))
          .toList();
      model.put("uploadedDocs", uploadedDocsWithThumbnails);
      model.put("uploadDocMaxFileSize", uploadDocumentConfiguration.getMaxFilesize());
    }

    if (pageWorkflow.getPageConfiguration().isStaticPage() || !pageWorkflow
        .getPageConfiguration()
        .isUsingPageTemplateFragment()) {
      model.put("data", pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources()));
      model.put("applicationData", applicationData);

      if (applicationData.hasRequiredSubworkflows(pageWorkflow.getDatasources())) {
        model.put("subworkflows", pageWorkflow.getSubworkflows(applicationData));
        if (isNotBlank(iterationIndex)) {
          var iterationData = pageWorkflow.getSubworkflows(applicationData)
              .get(pageWorkflow.getAppliesToGroup())
              .get(Integer.parseInt(iterationIndex));
          model.put("iterationData", iterationData);
        }
      }
    } else {
      model.put("pageDatasources",
          pagesData.getDatasourcePagesBy(pageWorkflow.getDatasources())
              .mergeDatasourcePages(
                  pagesData.getDatasourceGroupBy(pageWorkflow.getDatasources(),
                      applicationData.getSubworkflows())));
      model.put("data", pagesData
          .getPageDataOrDefault(pageTemplate.getName(), pageWorkflow.getPageConfiguration()));
    }

    return model;
  }

  private boolean requestedPageAppliesToGroup(String iterationIndex,
      PageWorkflowConfiguration pageWorkflow) {
    return isNotBlank(iterationIndex) && applicationData.getSubworkflows()
        .containsKey(pageWorkflow.getAppliesToGroup());
  }

  private boolean shouldRedirectToLandingPage(@PathVariable String pageName) {
    LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration
        .getLandmarkPages();
    // If they requested landing page or application is unstarted
    boolean unstarted = !landmarkPagesConfiguration.isLandingPage(pageName)
                        && applicationData.getStartTime() == null;
    // If they are restarting the application process after submitting
    boolean restarted =
        applicationData.isSubmitted() && landmarkPagesConfiguration.isStartTimerPage(pageName);
    return unstarted || restarted;
  }

  private boolean shouldRedirectToTerminalPage(@PathVariable String pageName) {
    LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration
        .getLandmarkPages();
    // Application is already submitted and not at the beginning of the application process
    return !landmarkPagesConfiguration.isPostSubmitPage(pageName) &&
           !landmarkPagesConfiguration.isLandingPage(pageName) &&
           !landmarkPagesConfiguration.isStartTimerPage(pageName) &&
           applicationData.isSubmitted();
  }

  @PostMapping("/groups/{groupName}/delete")
  RedirectView deleteGroup(@PathVariable String groupName, HttpSession httpSession) {
    applicationData.getSubworkflows().remove(groupName);
    pageEventPublisher
        .publish(new SubworkflowIterationDeletedEvent(httpSession.getId(), groupName));
    String startPage = applicationConfiguration.getPageGroups().get(groupName).getRestartPage();
    return new RedirectView("/pages/" + startPage);
  }

  @PostMapping("/groups/{groupName}/{iteration}/delete")
  RedirectView deleteIteration(
      @PathVariable String groupName,
      @PathVariable int iteration,
      HttpSession httpSession
  ) {
    String nextPage;
    applicationData.getSubworkflows().get(groupName).remove(iteration);
    pageEventPublisher
        .publish(new SubworkflowIterationDeletedEvent(httpSession.getId(), groupName));

    if (applicationData.getSubworkflows().get(groupName).isEmpty()) {
      applicationData.getSubworkflows().remove(groupName);
      nextPage = applicationConfiguration.getPageGroups().get(groupName).getRestartPage();
    } else {
      nextPage = applicationConfiguration.getPageGroups().get(groupName).getReviewPage();
    }

    PageWorkflowConfiguration nextPageWorkflow = applicationConfiguration
        .getPageWorkflow(nextPage);
    if (shouldSkip(nextPageWorkflow)) {
      return new RedirectView(String.format("/pages/%s/navigation", nextPage));
    } else {
      return new RedirectView(String.format("/pages/%s", nextPage));
    }
  }

  @PostMapping("/groups/{groupName}/{iteration}/deleteWarning")
  ModelAndView deleteIterationWarning(
      @PathVariable String groupName,
      @PathVariable int iteration
  ) {
    String deleteWarningPage = applicationConfiguration.getPageGroups().get(groupName)
        .getDeleteWarningPage();
    return new ModelAndView(
        "redirect:/pages/" + deleteWarningPage + "?iterationIndex=" + iteration);
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
        applicationConfiguration.getPageGroups().get(pageWorkflow.getGroupName())
            .getCompletePages()
            .contains(page.getName())
    ) {
      String groupName = pageWorkflow.getGroupName();
      applicationData.getSubworkflows()
          .addIteration(groupName, incompleteIterations.remove(groupName));
      pageEventPublisher
          .publish(new SubworkflowCompletedEvent(httpSession.getId(), groupName));
    }

    if (pageDataIsValid) {
      if (applicationData.getId() == null) {
        applicationData.setId(applicationRepository.getNextId());
      }
      
      if (pageName != null || !pageName.isEmpty()) {
          applicationData.setLastPageViewed(pageName);
      }

      ofNullable(pageWorkflow.getEnrichment())
          .map(applicationEnrichment::getEnrichment)
          .map(enrichment -> enrichment.process(pagesData))
          .ifPresent(pageData::putAll);

      Application application = applicationFactory.newApplication(applicationData);
      applicationRepository.save(application);
      if (applicationConfiguration.getLandmarkPages().isInProgressStatusPage(pageName)) {
        documentStatusRepository.createOrUpdateAll(application, IN_PROGRESS);
      }
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
    LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration
        .getLandmarkPages();
    String submitPage = landmarkPagesConfiguration.getSubmitPage();
    PageConfiguration page = applicationConfiguration.getPageWorkflow(submitPage)
        .getPageConfiguration();

    PageData pageData = PageData.fillOut(page, model);
    PagesData pagesData = applicationData.getPagesData();
    pagesData.putPage(submitPage, pageData);

    if (pageData.isValid()) {
      if (applicationData.getId() == null) {
        // only happens in framework tests now we think, left in out of an abundance of caution
        applicationData.setId(applicationRepository.getNextId());
      }
      Application application = applicationFactory.newApplication(applicationData);
      application.setCompletedAtTime(clock); // how we mark that the application is complete
      applicationRepository.save(application);
      log.info("Invoking pageEventPublisher for application submission: " + application.getId());
      pageEventPublisher.publish(
          new ApplicationSubmittedEvent(httpSession.getId(), application.getId(),
              application.getFlow(), LocaleContextHolder.getLocale())
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
  public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
      @RequestParam("dataURL") String dataURL,
      @RequestParam("type") String type,
      Locale locale) throws IOException, InterruptedException {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    try {
      documentStatusRepository.createOrUpdateAllForDocumentType(applicationData, IN_PROGRESS, UPLOADED_DOC);

      if (applicationData.getUploadedDocs().size() <= MAX_FILES_UPLOADED &&
          file.getSize() <= uploadDocumentConfiguration.getMaxFilesizeInBytes()) {
        if (type.contains("pdf")) {
          // Return an error response if this is an pdf we can't work with
          try (var pdfFile = PDDocument.load(file.getBytes())) {
            var acroForm = pdfFile.getDocumentCatalog().getAcroForm();
            if (acroForm != null && acroForm.xfaIsDynamic()) {
              return new ResponseEntity<>(
                  lms.getMessage("upload-documents.this-pdf-is-in-an-old-format"),
                  HttpStatus.UNPROCESSABLE_ENTITY);
            }
          } catch (InvalidPasswordException e) {
            return new ResponseEntity<>(
                lms.getMessage("upload-documents.this-pdf-is-password-protected"),
                HttpStatus.UNPROCESSABLE_ENTITY);
          }
        }
        
        var filePath = applicationData.getId() + "/" + UUID.randomUUID();
        var thumbnailFilePath = applicationData.getId() + "/" + UUID.randomUUID();
        
        if(file.getContentType()!=null && file.getContentType().contains("image")) {
          Path paths= Paths.get(file.getOriginalFilename());
          Files.write(paths, file.getBytes());
          BufferedImage bufferedImage = ImageIO.read(paths.toFile());
          BufferedImage outputImage = Scalr.resize(bufferedImage, 300);
    
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          ImageIO.write(outputImage, "png", os);
          dataURL = "data:image/png;base64,"+Base64.getEncoder().encodeToString(os.toByteArray());
          outputImage.flush();
        }
        documentRepository.upload(filePath, file);
        documentRepository.upload(thumbnailFilePath, dataURL);
        applicationData.addUploadedDoc(file, filePath, thumbnailFilePath, type);
      }

      return new ResponseEntity<>(HttpStatus.OK);
    } catch (Exception e) {
      // If there's any uncaught exception, return a default error message
      return new ResponseEntity<>(
          lms.getMessage("upload-documents.there-was-an-issue-on-our-end"),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/submit-documents")
  ModelAndView submitDocuments(HttpSession httpSession) {
    Application application = applicationRepository.find(applicationData.getId());
    application.getApplicationData().setUploadedDocs(applicationData.getUploadedDocs());
    if (applicationData.getFlow() == LATER_DOCS) {
      application.setCompletedAtTime(clock);
    }
    applicationRepository.save(application);
    if (featureFlags.get("submit-via-api").isOn()) {
      log.info("Invoking pageEventPublisher for UPLOADED_DOC submission: " + application.getId());
      documentStatusRepository.createOrUpdateAllForDocumentType(applicationData, SENDING, UPLOADED_DOC);
      pageEventPublisher.publish(
          new UploadedDocumentsSubmittedEvent(httpSession.getId(), application.getId(),
              LocaleContextHolder.getLocale()));
    }
    LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration
        .getLandmarkPages();
    String nextPage = landmarkPagesConfiguration.getNextStepsPage();
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
        .ifPresent(documentRepository::delete);
    applicationData.removeUploadedDoc(filename);

    return new ModelAndView("redirect:/pages/uploadDocuments");
  }
}
