package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PageControllerTest {
    @TestConfiguration
    static class PageControllerTestConfiguration {
        @Bean
        public CustomScopeConfigurer customScopeConfigurer() {
            CustomScopeConfigurer configurer = new CustomScopeConfigurer();
            configurer.addScope(WebApplicationContext.SCOPE_SESSION, new SimpleThreadScope());
            return configurer;
        }
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PagesData pagesData;

    @Autowired
    SessionMetadata sessionMetadata;

    @MockBean
    Clock clock;

    @MockBean
    ApplicationMetricsRepository applicationMetricsRepository;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    void shouldWriteTheFormDataForSignThisApplicationPageAndCaptureSubmissionTime() throws Exception {
        sessionMetadata.setStartTimeOnce(Instant.now());
        when(clock.instant()).thenReturn(LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant());

        mockMvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("applicantSignature", "some signature"))
                .andExpect(redirectedUrl("/pages/signThisApplication/navigation"));

        FormData signThisApplication = pagesData.getPage("signThisApplication");
        assertThat(signThisApplication.get("applicantSignature").getValue()).contains("some signature");
        assertThat(signThisApplication.get("submissionTime").getValue()).contains("January 1, 2020");
    }

    @Test
    void shouldStoreCompletedApplicationInRepository() throws Exception {
        Instant submissionTime = LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant();
        sessionMetadata.setStartTimeOnce(submissionTime.minus(5, ChronoUnit.MINUTES).minus(30, ChronoUnit.SECONDS));
        when(clock.instant()).thenReturn(submissionTime);

        mockMvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("applicantSignature", "some signature"));

        ArgumentCaptor<ApplicationMetric> argumentCaptor = ArgumentCaptor.forClass(ApplicationMetric.class);
        verify(applicationMetricsRepository).save(argumentCaptor.capture());
        ApplicationMetric applicationMetric = argumentCaptor.getValue();
        assertThat(applicationMetric.getTimeToComplete()).isEqualTo(Duration.ofMinutes(5).plusSeconds(30));
    }

    @Test
    void shouldCaptureStartTimeWhenUserNavigateToLanguageSelectionPage() throws Exception {
        Instant startTime = Instant.ofEpochSecond(1345362);
        when(clock.instant()).thenReturn(startTime);

        mockMvc.perform(get("/pages/languagePreferences"));

        assertThat(sessionMetadata.getStartTime()).isEqualTo(startTime);
    }

    @Test
    void shouldNotCaptureStartTimeWhenNavigatingToPagesOtherThanLanguageSelection() throws Exception {
        mockMvc.perform(get("/pages/prepareToApply"));

        assertThat(sessionMetadata.getStartTime()).isNull();
    }

    @Test
    void shouldNeverResetStartTime() throws Exception {
        Instant startTime = Instant.ofEpochSecond(1345362);
        when(clock.instant()).thenReturn(startTime);

        mockMvc.perform(get("/pages/languagePreferences"));

        when(clock.instant()).thenReturn(Instant.now());

        mockMvc.perform(get("/pages/languagePreferences"));

        assertThat(sessionMetadata.getStartTime()).isEqualTo(startTime);
    }
}