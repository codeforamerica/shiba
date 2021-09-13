package org.codeforamerica.shiba.testutilities;

import static org.codeforamerica.shiba.testutilities.TestUtils.resetApplicationData;

import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.configurations.CityInfoConfiguration;
import org.codeforamerica.shiba.configurations.ClockConfiguration;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.pages.DocRecommendationMessageService;
import org.codeforamerica.shiba.pages.PageController;
import org.codeforamerica.shiba.pages.RoutingDestinationService;
import org.codeforamerica.shiba.pages.NextStepsContentService;
import org.codeforamerica.shiba.pages.config.ApplicationConfigurationFactoryAppConfig;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.ApplicationEnrichment;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(PageController.class)
@WithMockUser(authorities = "admin")
@Import({
    NonSessionScopedApplicationData.class,
    ApplicationConfigurationFactoryAppConfig.class,
    ClockConfiguration.class,
    ApplicationFactory.class,
    NextStepsContentService.class,
    DocRecommendationMessageService.class
})
public class AbstractPageControllerTest {

  @MockBean
  protected ApplicationRepository applicationRepository;
  @MockBean
  protected SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider;
  @MockBean
  protected CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider;
  @MockBean
  protected MonitoringService monitoringService;
  @MockBean
  protected PageEventPublisher pageEventPublisher;
  @MockBean
  protected ApplicationEnrichment applicationEnrichment;
  @MockBean
  protected CountyParser countyParser;
  @MockBean
  protected CityInfoConfiguration cityInfoConfiguration;
  @MockBean
  protected FeatureFlagConfiguration featureFlagConfiguration;
  @MockBean
  protected UploadDocumentConfiguration uploadDocumentConfiguration;
  @MockBean
  protected DocumentRepository documentRepository;
  @MockBean
  protected RoutingDestinationService routingDestinationService;

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ApplicationData applicationData;

  @AfterEach
  void cleanup() {
    resetApplicationData(applicationData);
  }
}
