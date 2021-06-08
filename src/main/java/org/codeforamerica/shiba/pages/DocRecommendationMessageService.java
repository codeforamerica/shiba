package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.application.parsers.PageInputCoordinates;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.codeforamerica.shiba.Program.*;
import static org.codeforamerica.shiba.internationalization.InternationalizationUtils.listToString;

@Service
public class DocRecommendationMessageService {
    private final MessageSource messageSource;

    private final String proofOfIncome = "proofOfIncome";
    private final String proofOfHousingCost = "proofOfHousingCost";
    private final String proofOfJobLoss = "proofOfJobLoss";

    private final String proofOfIncomeTitleShort = "upload-documents.proof-of-income";
    private final String proofOfIncomeTextShort = "upload-documents.a-document-with-employer-and-employee-names";
    private final String proofOfHousingCostTitleShort = "upload-documents.proof-of-housing-costs";
    private final String proofOfHousingCostTextShort = "upload-documents.a-document-showing-total-amount-paid-for-housing";
    private final String proofOfJobLossTitleShort = "upload-documents.proof-of-job-loss";
    private final String proofOfJobLossTextShort = "upload-documents.a-document-with-your-former-employers-name-and-signature";

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


    public DocRecommendationMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public List<DocumentRecommendation> getRecommendationsMessage(ApplicationData applicationData, Locale locale, String pageName) {
        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
        boolean showProofOfIncomeRecommendation = proofOfIncomeRecommendation(applicationData);
        boolean showProofOfHousingCostRecommendation = proofOfHousingCostRecommendation(applicationData);
        boolean showProofOfJobLossRecommendation = proofOfJobLossPrograms(applicationData);

        List<String> recommendationsToShow = new ArrayList<>();
        if(showProofOfIncomeRecommendation){
            recommendationsToShow.add(proofOfIncome);
        }

        if(showProofOfHousingCostRecommendation){
            recommendationsToShow.add(proofOfHousingCost);
        }

        if(showProofOfJobLossRecommendation){
            recommendationsToShow.add(proofOfJobLoss);
        }

        if(pageName.equals("uploadDocuments")){
            return getShortDocumentRecommendations(recommendationsToShow,lms);
        } else if (pageName.equals("documentRecommendation")){
            return getLongDocumentRecommendations(recommendationsToShow, lms);
        }

        return null;

    }

    private List<DocumentRecommendation> getShortDocumentRecommendations (List<String> recommendations, LocaleSpecificMessageSource lms){

        List<DocumentRecommendation> recommendationMessages = new ArrayList<>();
        recommendations.stream().forEach(recommendation -> {
            DocumentRecommendation docRec;

            if(recommendation.equals(proofOfIncome)){
                docRec = new DocumentRecommendation(lms.getMessage(proofOfIncomeTitleShort), lms.getMessage(proofOfIncomeTextShort));
                recommendationMessages.add(docRec);
            }
            if(recommendation.equals(proofOfHousingCost)){
                docRec = new DocumentRecommendation(lms.getMessage(proofOfHousingCostTitleShort), lms.getMessage(proofOfHousingCostTextShort));
                recommendationMessages.add(docRec);
            }
            if(recommendation.equals(proofOfJobLoss)){
                docRec = new DocumentRecommendation(lms.getMessage(proofOfJobLossTitleShort), lms.getMessage(proofOfJobLossTextShort));
                recommendationMessages.add(docRec);
            }
        });

        return recommendationMessages;
    }

    private List<DocumentRecommendation> getLongDocumentRecommendations(List<String> recommendations, LocaleSpecificMessageSource lms){
        List<DocumentRecommendation> recommendationMessages = new ArrayList<>();
        recommendations.stream().forEach(recommendation -> {
            DocumentRecommendation docRec;
            if(recommendation.equals(proofOfIncome)){
                docRec = new DocumentRecommendation(proofOfIncomeIconLong, lms.getMessage(proofOfIncomeTitleLong), lms.getMessage(proofOfIncomeExplanationLong), lms.getMessage(proofOfIncomeExampleLong));
                recommendationMessages.add(docRec);
            }
            if(recommendation.equals(proofOfHousingCost)){
                docRec = new DocumentRecommendation(proofOfHousingCostIconLong, lms.getMessage(proofOfHousingCostTitleLong), lms.getMessage(proofOfHousingCostExplanationLong), lms.getMessage(proofOfHousingCostExampleLong));
                recommendationMessages.add(docRec);
            }
            if(recommendation.equals(proofOfJobLoss)){
                docRec = new DocumentRecommendation(proofOfJobLossIconLong, lms.getMessage(proofOfJobLossTitleLong), lms.getMessage(proofOfJobLossExplanationLong), lms.getMessage(proofOfJobLossExampleLong));
                recommendationMessages.add(docRec);
            }

        });
        return recommendationMessages;
    }

    private boolean proofOfIncomeRecommendation(ApplicationData applicationData){
        List<String> proofOfIncomePrograms = List.of("SNAP", "CASH", "EA", "GRH");
        boolean employmentStatus = applicationData.getPagesData().safeGetPageInputValue("employmentStatus", "areYouWorking").containsAll(List.of("true"));

        return employmentStatus && applicationData.isApplicationWith(proofOfIncomePrograms);
    }

    private boolean proofOfHousingCostRecommendation(ApplicationData applicationData) {
        List<String> proofOfHousingCostPrograms = List.of("SNAP", "CASH", "EA");
        boolean hasHousingExpenses = !applicationData.getPagesData().safeGetPageInputValue("homeExpenses", "homeExpenses").contains("NONE_OF_THE_ABOVE");

        return hasHousingExpenses && applicationData.isApplicationWith(proofOfHousingCostPrograms);
    }

    private boolean proofOfJobLossPrograms(ApplicationData applicationData) {
        List<String> proofOfJobLossPrograms = List.of("SNAP", "CASH", "GRH");
        boolean hasChangedWorkSituation = applicationData.getPagesData().safeGetPageInputValue("workSituation", "hasWorkSituation").containsAll(List.of("true"));

        return hasChangedWorkSituation && applicationData.isApplicationWith(proofOfJobLossPrograms);
    }


    public static class DocumentRecommendation {
        public String icon;
        public String title;
        public String explanation;
        public String example;

        public DocumentRecommendation(String icon, String title, String explanation, String example){
            this.icon=icon;
            this.title=title;
            this.explanation=explanation;
            this.example=example;
        }

        public DocumentRecommendation(String title, String explanation){
            this.title = title;
            this.explanation = explanation;
        }
    }
}
