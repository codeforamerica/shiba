package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.ApplicationStatusUpdater;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.NonSessionScopedApplicationData;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.configurations.ClockConfiguration;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.pages.config.ApplicationConfigurationFactoryAppConfig;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
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

import java.util.ArrayList;
import java.util.HashMap;

@ActiveProfiles("test")
@WebMvcTest(PageController.class)
@WithMockUser(authorities = "admin")
@Import({
        NonSessionScopedApplicationData.class,
        ApplicationConfigurationFactoryAppConfig.class,
        ClockConfiguration.class,
        ApplicationFactory.class,
        SuccessMessageService.class,
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
    protected FeatureFlagConfiguration featureFlagConfiguration;
    @MockBean
    protected UploadDocumentConfiguration uploadDocumentConfiguration;
    @MockBean
    protected DocumentRepositoryService documentRepositoryService;
    @MockBean
    protected ApplicationStatusUpdater applicationStatusUpdater;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ApplicationData applicationData;

    @AfterEach
    void cleanup() {
        applicationData.setId(null);
        applicationData.setUtmSource(null);
        applicationData.setPagesData(new PagesData());
        applicationData.setSubworkflows(new Subworkflows());
        applicationData.setIncompleteIterations(new HashMap<>());
        applicationData.setUploadedDocs(new ArrayList<>());
        applicationData.setFlow(FlowType.UNDETERMINED);
        applicationData.setSubmitted(false);
    }
}
