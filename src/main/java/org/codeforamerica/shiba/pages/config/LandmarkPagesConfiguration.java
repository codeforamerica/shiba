package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LandmarkPagesConfiguration {
    private String startTimerPage;
    private List<String> landingPages = new ArrayList<>();
    private List<String> postSubmitPages = new ArrayList<>();
    private String terminalPage;
    private String submitPage;

    public boolean isLandingPage(String pageName) {
        return this.getLandingPages().contains(pageName);
    }

    public boolean isTerminalPage(String pageName) {
        return pageName.equals(this.getTerminalPage());
    }

    public boolean isPostSubmitPage(String pageName) {
        return this.getPostSubmitPages().contains(pageName);
    }

    public boolean isStartTimerPage(String pageName) {
        return pageName.equals(this.getStartTimerPage());
    }

    public boolean isSubmitPage(String pageName) {
        return pageName.equals(this.getSubmitPage());
    }
}
