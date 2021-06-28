package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.SessionScopedApplicationDataTestConfiguration;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;

import static org.codeforamerica.shiba.TestUtils.resetApplicationData;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = MOCK)
@Tag("pdf")
@AutoConfigureMockMvc
@Import({SessionScopedApplicationDataTestConfiguration.class})
public class AbstractPdfIntegrationMockMvcTest {
    @MockBean
    protected Clock clock;

    @MockBean
    protected LocationClient locationClient;

    @MockBean
    protected FeatureFlagConfiguration featureFlagConfiguration;

    @Autowired
    protected ApplicationData applicationData;

    @AfterEach
    void cleanup() {
        resetApplicationData(applicationData);
    }
}
