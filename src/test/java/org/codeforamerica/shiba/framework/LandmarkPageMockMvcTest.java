package org.codeforamerica.shiba.framework;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedListener;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.*;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(LandmarkPageMockMvcTest.LandmarkPageTestController.class)
@SpringBootTest(properties = {"pagesConfig=pages-config/test-landmark-pages.yaml"})
public class LandmarkPageMockMvcTest extends AbstractStaticMessageSourceFrameworkTest {
    private String firstPageTitle = "first page title";
    private String secondPageTitle = "second page title";
    private String fourthPageTitle = "fourth page title";

    @MockBean
    private PageEventPublisher pageEventPublisher;

    @MockBean
    private ApplicationSubmittedListener applicationSubmittedListener;

    private static ApplicationData applicationData;

    @Controller
    static class LandmarkPageTestController {
        private final ApplicationData applicationData;

        public LandmarkPageTestController(ApplicationData applicationData) {
            this.applicationData = applicationData;
        }

        @GetMapping("/testPath2")
        String testEndpoint() {
            ApplicationData applicationDataClone = new ApplicationData();
            applicationDataClone.setPagesData(this.applicationData.getPagesData());
            applicationDataClone.setSubworkflows(this.applicationData.getSubworkflows());
            applicationDataClone.setIncompleteIterations(this.applicationData.getIncompleteIterations());
            applicationDataClone.setId(this.applicationData.getId());
            applicationDataClone.setStartTimeOnce(this.applicationData.getStartTime());
            LandmarkPageMockMvcTest.applicationData = applicationDataClone;
            return "testTerminalPage";
        }
    }

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("first-page-title", ENGLISH, firstPageTitle);
        staticMessageSource.addMessage("second-page-title", ENGLISH, secondPageTitle);
        staticMessageSource.addMessage("third-page-title", ENGLISH, "third page title");
        staticMessageSource.addMessage("fourth-page-title", ENGLISH, fourthPageTitle);
    }

    @Test
    void shouldRenderTheFirstLandingPageFromTheRoot() throws Exception {
        mockMvc.perform(get("/").session(session)).andExpect(forwardedUrl("/pages/testStaticLandingPage"));
        assertThat(getFormPage("testStaticLandingPage").getTitle()).isEqualTo(firstPageTitle);
    }

    @Test
    void shouldRedirectToFirstLandingPageWhenNavigateToAMidFlowPageDirectly() throws Exception {
        getPageAndExpectRedirect("fourthPage", "testStaticLandingPage");
    }

    @Test
    void shouldNotRedirectToFirstLandingPageWhenNavigateToAMidFlowPageAfterStartTimerPage() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        getPage("thirdPage").andExpect(status().isOk()); // start timer page
//        postExpectingNextPageTitle("thirdPage", fourthPageTitle);
        mockMvc.perform(post("/success").session(session).with(csrf()))
                .andExpect(redirectedUrl("/pages/fourthPage"));
    }
}
