package org.codeforamerica.shiba.pages.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class LandmarkPagesConfiguration {

  private List<String> startTimerPages = new ArrayList<>();
  private List<String> landingPages = new ArrayList<>();
  private List<String> postSubmitPages = new ArrayList<>();
  private String nextStepsPage;
  private String terminalPage;
  private String submitPage;
  private String uploadDocumentsPage;
  private String laterDocsTerminalPage;

  public boolean isLandingPage(String pageName) {
    return landingPages.contains(pageName);
  }

  public boolean isTerminalPage(String pageName) {
    return pageName.equals(terminalPage);
  }

  public boolean isPostSubmitPage(String pageName) {
    return postSubmitPages.contains(pageName);
  }

  public boolean isStartTimerPage(String pageName) {
    return startTimerPages.contains(pageName);
  }

  public boolean isSubmitPage(String pageName) {
    return pageName.equals(submitPage);
  }

  public boolean isLaterDocsTerminalPage(String pageName) {
    return pageName.equals(laterDocsTerminalPage);
  }

  public boolean isUploadDocumentsPage(String pageName) {
    return pageName.equals(uploadDocumentsPage);
  }
}
