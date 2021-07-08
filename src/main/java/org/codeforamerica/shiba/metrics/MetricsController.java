package org.codeforamerica.shiba.metrics;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class MetricsController {
    private final ApplicationRepository applicationRepository;
    public static final String TIME_TO_COMPLETE_FORMAT = "mm'm' ss's'";
    public static final ZoneId CENTRAL_ZONE_ID = ZoneId.of("America/Chicago");

    public MetricsController(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @GetMapping("/metrics")
    ModelAndView metrics() {
        int applicationsSubmitted = applicationRepository.count();
        Duration medianTimeToComplete = applicationRepository.getMedianTimeToComplete();
        log.info("******MEDIAN TIME TO COMPLETE: " + medianTimeToComplete);
        Duration averageTimeToCompleteForWeek = applicationRepository.getAverageTimeToCompleteWeekToDate(CENTRAL_ZONE_ID);
        Duration medianTimeToCompleteForWeek = applicationRepository.getMedianTimeToCompleteWeekToDate(CENTRAL_ZONE_ID);

        return new ModelAndView("metricsDashboard", Map.of(
                "applicationsSubmitted", applicationsSubmitted,
                "medianTimeToComplete", DurationFormatUtils.formatDuration(medianTimeToComplete.toMillis(), TIME_TO_COMPLETE_FORMAT),
                "averageTimeToCompleteForWeek", DurationFormatUtils.formatDuration(averageTimeToCompleteForWeek.toMillis(), TIME_TO_COMPLETE_FORMAT),
                "medianTimeToCompleteForWeek", DurationFormatUtils.formatDuration(medianTimeToCompleteForWeek.toMillis(), TIME_TO_COMPLETE_FORMAT),
                "counties", County.values(),
                "countyTotalSubmission", applicationRepository.countByCounty(),
                "countyTotalSubmissionWeekToDate", applicationRepository.countByCountyWeekToDate(CENTRAL_ZONE_ID),
                "sentimentDistribution", applicationRepository.getSentimentDistribution().entrySet().stream().map(
                        entry -> Map.entry(entry.getKey(), new DecimalFormat("#.##").format(entry.getValue() * 100))
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        ));
    }
}
