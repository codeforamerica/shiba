package org.codeforamerica.shiba.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class DocRecommendationMessageService {

  private final MessageSource messageSource;

  private final String proofOfIncome = "proofOfIncome";
  private final String proofOfHousingCost = "proofOfHousingCost";
  private final String proofOfJobLoss = "proofOfJobLoss";
  private final String proofOfMedicalExpenses = "proofOfMedicalExpenses";

  private final String proofOfIncomeTitleShort = "upload-documents.proof-of-income";
  private final String proofOfIncomeTextShort = "upload-documents.a-document-with-employer-and-employee-names";
  private final String proofOfHousingCostTitleShort = "upload-documents.proof-of-housing-costs";
  private final String proofOfHousingCostTextShort = "upload-documents.a-document-showing-total-amount-paid-for-housing";
  private final String proofOfJobLossTitleShort = "upload-documents.proof-of-job-loss";
  private final String proofOfJobLossTextShort = "upload-documents.a-document-with-your-former-employers-name-and-signature";
  private final String proofOfMedicalExpensesTitleShort = "upload-documents.proof-of-medical-expenses";
  private final String proofOfMedicalExpensesTextShort = "upload-documents.documents-showing-medical-expenses-that-you-paid-for";


  private final String proofOfIncomeIconLong = "fragments/icons/icon-income :: icon-income";
  private final String proofOfIncomeTitleLong = "document-recommendation.proof-of-income";
  private final String proofOfIncomeExplanationLong = "document-recommendation.proof-of-income-explanation";
  private final String proofOfIncomeExampleLong = "document-recommendation.proof-of-income-example";
  private final String proofOfHousingCostIconLong = "fragments/icons/icon-home :: icon-home";
  private final String proofOfHousingCostTitleLong = "document-recommendation.proof-of-housing-costs";
  private final String proofOfHousingCostExplanationLong = "document-recommendation.proof-of-housing-costs-explanation";
  private final String proofOfHousingCostExampleLong = "document-recommendation.proof-of-housing-costs-example";
  private final String proofOfJobLossIconLong = "fragments/icons/icon-job-loss :: icon-job-loss";
  private final String proofOfJobLossTitleLong = "document-recommendation.proof-of-job-loss";
  private final String proofOfJobLossExplanationLong = "document-recommendation.proof-of-job-loss-explanation";
  private final String proofOfJobLossExampleLong = "document-recommendation.proof-of-job-loss-example";
  private final String proofOfMedicalExpensesIconLong = "fragments/icons/icon-medical-expenses :: icon-medical-expenses";
  private final String proofOfMedicalExpensesTitleLong = "document-recommendation.proof-of-medical-expenses";
  private final String proofOfMedicalExpensesExplanationLong = "document-recommendation.proof-of-medical-expenses-explanation";
  private final String proofOfMedicalExpensesExampleLong = "document-recommendation.proof-of-medical-expenses-example";


  public DocRecommendationMessageService(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public List<DocumentRecommendation> getPageSpecificRecommendationsMessage(
      ApplicationData applicationData, Locale locale, String pageName) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    List<String> recommendationsToShow = getRecommendationsToShow(applicationData);
    return getLongDocumentRecommendations(recommendationsToShow, lms);
  }

  public List<DocumentRecommendation> getConfirmationEmailDocumentRecommendations(
      ApplicationData applicationData, Locale locale) {
    LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
    List<String> recommendationsToShow = getRecommendationsToShow(applicationData);
    return getShortDocumentRecommendations(recommendationsToShow, lms);
  }

  private List<String> getRecommendationsToShow(ApplicationData applicationData) {
    boolean showProofOfIncomeRecommendation = proofOfIncomeRecommendation(applicationData);
    boolean showProofOfHousingCostRecommendation = proofOfHousingCostRecommendation(
        applicationData);
    boolean showProofOfJobLossRecommendation = proofOfJobLossPrograms(applicationData);
    boolean showMedicalExpensesRecommendation = proofOfMedicalExpenses(applicationData);

    List<String> recommendationsToShow = new ArrayList<>();
    if (showProofOfIncomeRecommendation) {
      recommendationsToShow.add(proofOfIncome);
    }

    if (showProofOfHousingCostRecommendation) {
      recommendationsToShow.add(proofOfHousingCost);
    }

    if (showProofOfJobLossRecommendation) {
      recommendationsToShow.add(proofOfJobLoss);
    }

    if (showMedicalExpensesRecommendation) {
      recommendationsToShow.add(proofOfMedicalExpenses);
    }

    return recommendationsToShow;
  }

  private List<DocumentRecommendation> getShortDocumentRecommendations(List<String> recommendations,
      LocaleSpecificMessageSource lms) {
    List<DocumentRecommendation> recommendationMessages = new ArrayList<>();
    recommendations.forEach(recommendation -> {
      DocumentRecommendation docRec;

      switch (recommendation) {
        case proofOfIncome -> {
          docRec = new DocumentRecommendation(lms.getMessage(proofOfIncomeTitleShort),
              lms.getMessage(proofOfIncomeTextShort));
          recommendationMessages.add(docRec);
        }
        case proofOfHousingCost -> {
          docRec = new DocumentRecommendation(lms.getMessage(proofOfHousingCostTitleShort),
              lms.getMessage(proofOfHousingCostTextShort));
          recommendationMessages.add(docRec);
        }
        case proofOfJobLoss -> {
          docRec = new DocumentRecommendation(lms.getMessage(proofOfJobLossTitleShort),
              lms.getMessage(proofOfJobLossTextShort));
          recommendationMessages.add(docRec);
        }
        case proofOfMedicalExpenses -> {
          docRec = new DocumentRecommendation(lms.getMessage(proofOfMedicalExpensesTitleShort),
              lms.getMessage(proofOfMedicalExpensesTextShort));
          recommendationMessages.add(docRec);
        }
      }
    });

    return recommendationMessages;
  }

  private List<DocumentRecommendation> getLongDocumentRecommendations(List<String> recommendations,
      LocaleSpecificMessageSource lms) {
    List<DocumentRecommendation> recommendationMessages = new ArrayList<>();
    recommendations.forEach(recommendation -> {
      DocumentRecommendation docRec;

      switch (recommendation) {
        case proofOfIncome -> {
          docRec = new DocumentRecommendation(proofOfIncomeIconLong,
              lms.getMessage(proofOfIncomeTitleLong), lms.getMessage(proofOfIncomeExplanationLong),
              lms.getMessage(proofOfIncomeExampleLong));
          recommendationMessages.add(docRec);
        }
        case proofOfHousingCost -> {
          docRec = new DocumentRecommendation(proofOfHousingCostIconLong,
              lms.getMessage(proofOfHousingCostTitleLong),
              lms.getMessage(proofOfHousingCostExplanationLong),
              lms.getMessage(proofOfHousingCostExampleLong));
          recommendationMessages.add(docRec);
        }
        case proofOfJobLoss -> {
          docRec = new DocumentRecommendation(proofOfJobLossIconLong,
              lms.getMessage(proofOfJobLossTitleLong),
              lms.getMessage(proofOfJobLossExplanationLong),
              lms.getMessage(proofOfJobLossExampleLong));
          recommendationMessages.add(docRec);
        }
        case proofOfMedicalExpenses -> {
          docRec = new DocumentRecommendation(proofOfMedicalExpensesIconLong,
              lms.getMessage(proofOfMedicalExpensesTitleLong),
              lms.getMessage(proofOfMedicalExpensesExplanationLong),
              lms.getMessage(proofOfMedicalExpensesExampleLong));
          recommendationMessages.add(docRec);
        }
      }

    });
    return recommendationMessages;
  }

  private boolean proofOfIncomeRecommendation(ApplicationData applicationData) {
    List<String> proofOfIncomePrograms = List.of("SNAP", "CASH", "EA", "GRH", "CCAP");
    boolean employmentStatus = applicationData.getPagesData()
        .safeGetPageInputValue("employmentStatus", "areYouWorking").contains("true");

    return employmentStatus && applicationData.isApplicationWith(proofOfIncomePrograms);
  }

  private boolean proofOfHousingCostRecommendation(ApplicationData applicationData) {
    List<String> proofOfHousingCostPrograms = List.of("SNAP", "CASH", "EA");

    List<String> pageInputValues = applicationData.getPagesData()
        .safeGetPageInputValue("homeExpenses", "homeExpenses");
    boolean hasHousingExpenses =
        !pageInputValues.isEmpty() && !pageInputValues.contains("NONE_OF_THE_ABOVE");

    return hasHousingExpenses && applicationData.isApplicationWith(proofOfHousingCostPrograms);
  }

  private boolean proofOfJobLossPrograms(ApplicationData applicationData) {
    List<String> proofOfJobLossPrograms = List.of("SNAP", "CASH", "GRH");
    boolean hasChangedWorkSituation = applicationData.getPagesData()
        .safeGetPageInputValue("workSituation", "hasWorkSituation").contains("true");

    return hasChangedWorkSituation && applicationData.isApplicationWith(proofOfJobLossPrograms);
  }

  private boolean proofOfMedicalExpenses(ApplicationData applicationData) {
    return applicationData.isCCAPApplication() && applicationData.isMedicalExpensesApplication();
  }


  public static class DocumentRecommendation {

    public String icon;
    public String title;
    public String explanation;
    public String example;

    public DocumentRecommendation(String icon, String title, String explanation, String example) {
      this.icon = icon;
      this.title = title;
      this.explanation = explanation;
      this.example = example;
    }

    public DocumentRecommendation(String title, String explanation) {
      this.title = title;
      this.explanation = explanation;
    }
  }
}
