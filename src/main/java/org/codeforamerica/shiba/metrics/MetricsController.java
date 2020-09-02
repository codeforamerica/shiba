package org.codeforamerica.shiba.metrics;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.codeforamerica.shiba.County;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;

@Controller
public class MetricsController {
    private final ApplicationMetricsRepository metricsRepository;
    public static final String TIME_TO_COMPLETE_FORMAT = "mm'm' ss's'";
    public static final ZoneId CENTRAL_ZONE_ID = ZoneId.of("America/Chicago");

    public MetricsController(ApplicationMetricsRepository metricsRepository) {
        this.metricsRepository = metricsRepository;
    }

    @GetMapping("/metrics")
    ModelAndView metrics() {
        int applicationsSubmitted = metricsRepository.count();
        Duration medianTimeToComplete = metricsRepository.getMedianTimeToComplete();
        Duration averageTimeToCompleteForWeek = metricsRepository.getAverageTimeToCompleteWeekToDate(CENTRAL_ZONE_ID);
        Duration medianTimeToCompleteForWeek = metricsRepository.getMedianTimeToCompleteWeekToDate(CENTRAL_ZONE_ID);


        return new ModelAndView("metricsDashboard", Map.of(
                "applicationsSubmitted", applicationsSubmitted,
                "medianTimeToComplete", DurationFormatUtils.formatDuration(medianTimeToComplete.toMillis(), TIME_TO_COMPLETE_FORMAT),
                "averageTimeToCompleteForWeek", DurationFormatUtils.formatDuration(averageTimeToCompleteForWeek.toMillis(), TIME_TO_COMPLETE_FORMAT),
                "medianTimeToCompleteForWeek", DurationFormatUtils.formatDuration(medianTimeToCompleteForWeek.toMillis(), TIME_TO_COMPLETE_FORMAT),
                "counties", County.values(),
                "countyTotalSubmission", metricsRepository.countByCounty(),
                "countyTotalSubmissionWeekToDate", metricsRepository.countByCountyWeekToDate(CENTRAL_ZONE_ID)
        ));
    }
}
