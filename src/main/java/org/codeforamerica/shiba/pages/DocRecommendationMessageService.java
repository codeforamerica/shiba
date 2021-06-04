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

    private final String showProofOfIncomeTitleShort = "upload-documents.proof-of-income";
    private final String showProofOfIncomeTextShort = "upload-documents.a-document-with-employer-and-employee-names";
    private final String showProofOfHousingCostTitleShort = "upload-documents.proof-of-housing-costs";
    private final String showProofOfHousingCostTextShort = "upload-documents.a-document-showing-total-amount-paid-for-housing";
    private final String showProofOfJobLossTitleShort = "upload-documents.proof-of-job-loss";
    private final String showProofOfJobLossTextShort = "upload-documents.a-document-with-your-former-employers-name-and-signature";

    //TODO: Add string variables to hold the message resource keys for long
    

    //TODO: Create objects for recommendations for both short and long version
    private ShortDocumentRecommendation proofOfIncomeShort = new ShortDocumentRecommendation(showProofOfIncomeTitleShort, showProofOfIncomeTextShort);
    private ShortDocumentRecommendation proofOfHousingCostShort = new ShortDocumentRecommendation(showProofOfHousingCostTitleShort, showProofOfHousingCostTextShort);
    private ShortDocumentRecommendation proofOfJobLossShort = new ShortDocumentRecommendation(showProofOfJobLossTitleShort, showProofOfJobLossTextShort);


    public DocRecommendationMessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public List<Object> getSuccessMessage(ApplicationData applicationData, boolean medicalExpenses, Locale locale, String pageName) {
        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(locale, messageSource);
        boolean showProofOfIncomeRecommendation = proofOfIncomeRecommendation(applicationData);
        boolean showProofOfHousingCostRecommendation = proofOfHousingCostRecommendation(applicationData);
        boolean showProofOfJobLossRecommendation = proofOfJobLossPrograms(applicationData);

        //TODO: Add booleans and keys to map
        Map<String, Boolean> recommendationsToShow = new HashMap<>();


        return null;
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

    public class ShortDocumentRecommendation{
        String title;
        String text;

        public ShortDocumentRecommendation(String title, String text){
            this.title= title;
            this.text=text;
        }

    }

    public class LongDocumentRecommendation {
        String icon;
        String title;
        String explanation;
        String example;

        public LongDocumentRecommendation(String icon, String title, String explanation, String example){
            this.icon=icon;
            this.title=title;
            this.explanation=explanation;
            this.example=example;
        }
    }
}
