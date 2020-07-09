package org.codeforamerica.shiba.pages;

import lombok.Data;

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
}
