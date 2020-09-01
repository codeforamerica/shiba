package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.*;
import org.codeforamerica.shiba.metrics.ApplicationMetric;
import org.codeforamerica.shiba.metrics.ApplicationMetricsRepository;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.output.ApplicationDataConsumer;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest(classes = PageControllerTest.TestPageConfiguration.class, properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class PageControllerTest {

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-pages-controller.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "shiba-configuration-pages-controller")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    ApplicationData applicationData = new ApplicationData();

    MockMvc mockMvc;

    Metrics metrics = new Metrics();

    ConfirmationData confirmationData = new ConfirmationData();

    Clock clock = mock(Clock.class);

    ApplicationMetricsRepository applicationMetricsRepository = mock(ApplicationMetricsRepository.class);

    ApplicationDataConsumer applicationDataConsumer = mock(ApplicationDataConsumer.class);

    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    ApplicationFactory applicationFactory = mock(ApplicationFactory.class);

    ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

    @Autowired
    ApplicationConfiguration applicationConfiguration;

    @BeforeEach
    void setUp() {
        PageController pageController = new PageController(
                applicationConfiguration,
                applicationData,
                clock,
                applicationMetricsRepository,
                metrics,
                applicationRepository,
                applicationFactory,
                confirmationData,
                applicationEventPublisher
        );

        mockMvc = MockMvcBuilders.standaloneSetup(pageController)
                .build();
        when(clock.instant()).thenReturn(Instant.now());
        when(applicationFactory.newApplication(any())).thenReturn(new Application("defaultId", ZonedDateTime.now(), null, null));
    }

    @Test
    void shouldWriteTheInputDataMapForSubmitPage() throws Exception {
        metrics.setStartTimeOnce(Instant.now());
        when(clock.instant()).thenReturn(LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant());

        mockMvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("foo[]", "some value"))
                .andExpect(redirectedUrl("/pages/firstPage/navigation"));

        PageData firstPage = applicationData.getPagesData().getPage("firstPage");
        assertThat(firstPage.get("foo").getValue()).contains("some value");
    }

    @Test
    void shouldStoreCompletedApplicationInRepository() throws Exception {
        Instant submissionTime = LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant();
        metrics.setStartTimeOnce(submissionTime.minus(5, ChronoUnit.MINUTES).minus(30, ChronoUnit.SECONDS));
        when(clock.instant()).thenReturn(submissionTime);

        mockMvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("foo[]", "some value"));

        ArgumentCaptor<ApplicationMetric> argumentCaptor = ArgumentCaptor.forClass(ApplicationMetric.class);
        verify(applicationMetricsRepository).save(argumentCaptor.capture());
        ApplicationMetric applicationMetric = argumentCaptor.getValue();
        assertThat(applicationMetric.getTimeToComplete()).isEqualTo(Duration.ofMinutes(5).plusSeconds(30));
    }

    @Test
    void shouldNotStoreCompletedApplicationInRepositoryWhenNoSignatureIsSent() throws Exception {
        Instant submissionTime = LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant();
        metrics.setStartTimeOnce(submissionTime.minus(5, ChronoUnit.MINUTES).minus(30, ChronoUnit.SECONDS));
        when(clock.instant()).thenReturn(submissionTime);

        mockMvc.perform(post("/submit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verifyNoInteractions(applicationMetricsRepository);
    }

    @Test
    void shouldPublishApplicationSubmittedEvent() throws Exception {
        metrics.setStartTimeOnce(Instant.now());

        String applicationId = "someId";
        Application application = new Application(applicationId, ZonedDateTime.now(), applicationData, null);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(post("/submit")
                .param("foo[]", "some value")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        InOrder inOrder = inOrder(applicationRepository, applicationEventPublisher);
        inOrder.verify(applicationRepository).save(application);
        inOrder.verify(applicationEventPublisher).publishEvent(new ApplicationSubmittedEvent(applicationId));
    }

    @Test
    void shouldNotConsumeApplicationDataIfPageDataIsNotValid() throws Exception {
        metrics.setStartTimeOnce(Instant.now());

        mockMvc.perform(post("/submit")
                .param("foo[]", "")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verifyNoInteractions(applicationDataConsumer);
    }

    @Test
    void shouldSaveApplication() throws Exception {
        metrics.setStartTimeOnce(Instant.now());

        ZonedDateTime completedAt = ZonedDateTime.now();
        Application application = new Application("someId", completedAt, applicationData, null);
        when(applicationFactory.newApplication(applicationData)).thenReturn(application);

        mockMvc.perform(post("/submit")
                .param("foo[]", "some value")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        verify(applicationRepository).save(application);
        assertThat(confirmationData.getId()).isEqualTo("someId");
        assertThat(confirmationData.getCompletedAt()).isEqualTo(completedAt);
    }
}