package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.codeforamerica.shiba.metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

@Data
public class FlowConfiguration {
    private String startTimerPage;
    private List<String> landingPages = new ArrayList<>();

    boolean shouldResetData(String pageName) {
        return this.landingPages.contains(pageName);
    }

    boolean shouldStartTimer(String pageName) {
        return pageName.equals(this.startTimerPage);
    }

    boolean shouldRedirectToLandingPage(String pageName, Metrics metrics) {
        return !shouldResetData(pageName) && !shouldStartTimer(pageName) && metrics.getStartTime() == null;
    }
}
