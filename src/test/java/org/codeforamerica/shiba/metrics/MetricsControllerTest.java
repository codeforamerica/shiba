package org.codeforamerica.shiba.metrics;

import org.codeforamerica.shiba.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class MetricsControllerTest {

    private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    private final MetricsController metricsController = new MetricsController(applicationRepository);

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(metricsController).build();

    @BeforeEach
    void setUp() {
        when(applicationRepository.count()).thenReturn(0);
        when(applicationRepository.getMedianTimeToComplete()).thenReturn(Duration.ZERO);
        when(applicationRepository.getAverageTimeToCompleteWeekToDate(any())).thenReturn(Duration.ZERO);
        when(applicationRepository.getMedianTimeToCompleteWeekToDate(any())).thenReturn(Duration.ZERO);
    }

    @Test
    void shouldIncludeCountOfApplicationMetrics() throws Exception {
        int applicationsSubmitted = 1421;
        when(applicationRepository.count()).thenReturn(applicationsSubmitted);

        mockMvc.perform(get("/metrics"))
                .andExpect(MockMvcResultMatchers.view().name("metricsDashboard"))
                .andExpect(MockMvcResultMatchers.model().attribute("applicationsSubmitted", applicationsSubmitted));
    }

    @Test
    void shouldIncludeMedianTimeToComplete() throws Exception {
        Duration medianTimeToComplete = Duration.of(5, ChronoUnit.MINUTES);
        when(applicationRepository.getMedianTimeToComplete()).thenReturn(medianTimeToComplete);

        mockMvc.perform(get("/metrics"))
                .andExpect(MockMvcResultMatchers.view().name("metricsDashboard"))
                .andExpect(MockMvcResultMatchers.model().attribute("medianTimeToComplete", "05m 00s"));
    }
}