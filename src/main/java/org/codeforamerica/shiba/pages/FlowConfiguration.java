package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class FlowConfiguration {
    private String startTimerPage;
    private List<String> landingPages = new ArrayList<>();
    private String terminalPage;
    private String submitPage;
    private Map<String, NavigationConfiguration> navigation;

    boolean isLandingPage(String pageName) {
        return this.getLandingPages().contains(pageName);
    }

    boolean isTerminalPage(String pageName) {
        return pageName.equals(this.getTerminalPage());
    }

    boolean isStartTimerPage(String pageName) {
        return pageName.equals(this.getStartTimerPage());
    }

    public boolean isSubmitPage(String pageName) {
        return pageName.equals(this.getSubmitPage());
    }
}
