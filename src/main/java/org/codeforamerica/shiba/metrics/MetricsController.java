package org.codeforamerica.shiba.metrics;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.util.Map;

@Controller
public class MetricsController {
    private final ApplicationMetricsRepository metricsRepository;

    public MetricsController(ApplicationMetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }

    @GetMapping("/metrics")
    ModelAndView metrics() {
        int applicationsSubmitted = metricsRepository.count();
        Duration medianTimeToComplete = metricsRepository.getMedianTimeToComplete();
        String formattedDuration = DurationFormatUtils.formatDuration(medianTimeToComplete.toMillis(), "mm'm' ss's'");
        return new ModelAndView("metricsDashboard", Map.of(
                "applicationsSubmitted", applicationsSubmitted,
                "medianTimeToComplete", formattedDuration
        ));
    }
}
