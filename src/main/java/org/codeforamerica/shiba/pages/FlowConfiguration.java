package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FlowConfiguration {
    private String startTimerPage;
    private List<String> landingPages = new ArrayList<>();
    private String terminalPage;

    boolean isLandingPage(String pageName) {
        return this.getLandingPages().contains(pageName);
    }

    boolean isTerminalPage(String pageName) {
        return pageName.equals(this.getTerminalPage());
    }

    boolean isStartTimerPage(String pageName) {
        return pageName.equals(this.getStartTimerPage());
    }
}
