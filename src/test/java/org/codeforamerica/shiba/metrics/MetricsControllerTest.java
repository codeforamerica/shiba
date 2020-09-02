package org.codeforamerica.shiba.metrics;

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

    private final ApplicationMetricsRepository metricsRepository = mock(ApplicationMetricsRepository.class);

    private final MetricsController metricsController = new MetricsController(metricsRepository);

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(metricsController).build();

    @BeforeEach
    void setUp() {
        when(metricsRepository.count()).thenReturn(0);
        when(metricsRepository.getMedianTimeToComplete()).thenReturn(Duration.ZERO);
        when(metricsRepository.getAverageTimeToCompleteWeekToDate(any())).thenReturn(Duration.ZERO);
        when(metricsRepository.getMedianTimeToCompleteWeekToDate(any())).thenReturn(Duration.ZERO);
    }

    @Test
    void shouldIncludeCountOfApplicationMetrics() throws Exception {
        int applicationsSubmitted = 1421;
        when(metricsRepository.count()).thenReturn(applicationsSubmitted);

        mockMvc.perform(get("/metrics"))
                .andExpect(MockMvcResultMatchers.view().name("metricsDashboard"))
                .andExpect(MockMvcResultMatchers.model().attribute("applicationsSubmitted", applicationsSubmitted));
    }

    @Test
    void shouldIncludeMedianTimeToComplete() throws Exception {
        Duration medianTimeToComplete = Duration.of(5, ChronoUnit.MINUTES);
        when(metricsRepository.getMedianTimeToComplete()).thenReturn(medianTimeToComplete);

        mockMvc.perform(get("/metrics"))
                .andExpect(MockMvcResultMatchers.view().name("metricsDashboard"))
                .andExpect(MockMvcResultMatchers.model().attribute("medianTimeToComplete", "05m 00s"));
    }
}