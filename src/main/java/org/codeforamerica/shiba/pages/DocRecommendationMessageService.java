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

    //TODO: Add string variables to hold the message resource keys for long
    



    public DocRecommendationMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public List<DocumentRecommendation> getShortRecommendationsMessage(ApplicationData applicationData, Locale locale, String pageName) {
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
            return getUploadDocumentsRecommendations(recommendationsToShow,lms);
        } else if (pageName.equals("documentRecommendation")){

        }

        return null;

    }

    private List<DocumentRecommendation> getUploadDocumentsRecommendations (List<String> recommendations, LocaleSpecificMessageSource lms){

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

    private boolean proofOfIncomeRecommendation(ApplicationData applicationData){
        List<String> proofOfIncomePrograms = List.of("SNAP", "CASH", "EA", "GRH");
        boolean employmentStatus = applicationData.getPagesData().safeGetPageInputValue("employmentStatus", "areYouWorking").containsAll(List.of("true"));

        return employmentStatus && applicationData.isApplicationWith(proofOfIncomePrograms);
    }

    private boolean proofOfHousingCostRecommendation(ApplicationData applicationData) {
        List<String> proofOfHousingCostPrograms = List.of("SNAP", "CASH", "EA");
        boolean hasHousingExpenses = applicationData.getPagesData().safeGetPageInputValue("homeExpenses", "homeExpenses").containsAll(List.of("NONE_OF_THE_ABOVE"));

        return hasHousingExpenses && applicationData.isApplicationWith(proofOfHousingCostPrograms);
    }

    private boolean proofOfJobLossPrograms(ApplicationData applicationData) {
        List<String> proofOfJobLossPrograms = List.of("SNAP", "CASH", "GRH");
        boolean hasChangedWorkSituation = applicationData.getPagesData().safeGetPageInputValue("workSituation", "hasWorkSituation").containsAll(List.of("true"));

        return hasChangedWorkSituation && applicationData.isApplicationWith(proofOfJobLossPrograms);
    }


    public class DocumentRecommendation {
        String icon;
        String title;
        String explanation;
        String example;

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
