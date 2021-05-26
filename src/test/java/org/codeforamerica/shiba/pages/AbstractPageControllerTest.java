package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.ApplicationStatusUpdater;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.NonSessionScopedApplicationData;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.configurations.ClockConfiguration;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.pages.config.ApplicationConfigurationFactoryAppConfig;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.enrichment.ApplicationEnrichment;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@WebMvcTest(PageController.class)
@WithMockUser(authorities = "admin")
@Import({
        NonSessionScopedApplicationData.class,
        ApplicationConfigurationFactoryAppConfig.class,
        ClockConfiguration.class,
        ApplicationFactory.class,
        SuccessMessageService.class
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
    protected DocumentListParser documentListParser;
    @MockBean
    protected UploadDocumentConfiguration uploadDocumentConfiguration;
    @MockBean
    protected DocumentRepositoryService documentRepositoryService;
    @MockBean
    protected ApplicationStatusUpdater applicationStatusUpdater;
}
