package org.codeforamerica.shiba.pages.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LandmarkPagesConfiguration {
    private List<String> startTimerPage = new ArrayList<>();
    private List<String> landingPages = new ArrayList<>();
    private List<String> postSubmitPages = new ArrayList<>();
    private String nextStepsPage;
    private String terminalPage;
    private String submitPage;
    private String uploadDocumentsPage;
    private String laterDocsIdPage;
    private String laterDocsTerminalPage;

    public boolean isLandingPage(String pageName) {
        return getLandingPages().contains(pageName);
    }

    public boolean isTerminalPage(String pageName) {
        return pageName.equals(getTerminalPage());
    }

    public boolean isPostSubmitPage(String pageName) {
        return getPostSubmitPages().contains(pageName);
    }

    public boolean isStartTimerPage(String pageName) {
        return getStartTimerPage().contains(pageName);
    }

    public boolean isSubmitPage(String pageName) {
        return pageName.equals(getSubmitPage());
    }

    public boolean isLaterDocsIdPage(String pageName) {
        return pageName.equals(laterDocsIdPage);
    }

    public boolean isLaterDocsTerminalPage(String pageName) {
        return pageName.equals(laterDocsTerminalPage);
    }

    public boolean isUploadDocumentsPage(String pageName) {
        return pageName.equals(uploadDocumentsPage);
    }
}
