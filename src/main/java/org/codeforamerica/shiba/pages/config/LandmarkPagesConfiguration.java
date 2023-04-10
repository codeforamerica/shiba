package org.codeforamerica.shiba.pages.config;

import java.util.ArrayList;
import java.util.HashMap;
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
  private List<String> uploadDocumentsPage = new ArrayList<>();
  private List<String> submitUploadedDocumentsPage = new ArrayList<>();
  private String laterDocsTerminalPage;
  private String healthcareRenewalTerminalPage;

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
  
  public boolean isHealthcareRenewalTerminalPage(String pageName) {
    return pageName.equals(healthcareRenewalTerminalPage);
  }

  public boolean isUploadDocumentsPage(String pageName) {
    return uploadDocumentsPage.contains(pageName);
  }

  public boolean isSubmitUploadedDocumentsPage(String pageName) {
    return submitUploadedDocumentsPage.contains(pageName);
  }
  
  public String getCorrectUploadDocumentPage(String pageName) {
	HashMap<String, String> map = new HashMap<>();
	for(int i=0; i< submitUploadedDocumentsPage.size();i++) {
		map.put(submitUploadedDocumentsPage.get(i), uploadDocumentsPage.get(i) );
	}
	return map.get(pageName);
  }

  public boolean isNextStepsPage(String pageName) {
    return pageName.equals(nextStepsPage);
  }

}
