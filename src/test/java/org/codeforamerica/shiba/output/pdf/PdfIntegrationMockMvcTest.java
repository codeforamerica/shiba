package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.SessionScopedApplicationDataTestConfiguration;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.config.PageTemplate;
import org.codeforamerica.shiba.pages.config.ReferenceOptionsTemplate;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.ModelAndView;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = MOCK)
@Tag("pdf")
@AutoConfigureMockMvc
@Import({SessionScopedApplicationDataTestConfiguration.class})
public class PdfIntegrationMockMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationData applicationData;

    @MockBean
    private Clock clock;

    @MockBean
    private LocationClient locationClient;

    @MockBean
    private FeatureFlagConfiguration featureFlagConfiguration;

    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        session = new MockHttpSession();

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(locationClient.validateAddress(any())).thenReturn(Optional.empty());
        when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("county-anoka")).thenReturn(FeatureFlag.OFF);

        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postWithData("languagePreferences", Map.of(
                "writtenLanguage", List.of("ENGLISH"),
                "spokenLanguage", List.of("ENGLISH"))
        );

        postWithData("addHouseholdMembers", "addHouseholdMembers", "false");
    }

    @AfterEach
    void cleanup() {
        resetApplicationData(applicationData);
    }

    @Test
    void shouldAnswerEnergyAssistanceQuestion() throws Exception {
        selectPrograms("CASH");

        postWithData("energyAssistance", "energyAssistance", "true");
        postWithData("energyAssistanceMoreThan20", "energyAssistanceMoreThan20", "false");

        var caf = submitAndDownloadCaf();
        assertPdfFieldEquals("RECEIVED_LIHEAP", "No", caf);
    }

    @Test
    void shouldMapEnergyAssistanceWhenUserReceivedNoAssistance() throws Exception {
        selectPrograms("CASH");

        postWithData("energyAssistance", "energyAssistance", "false");

        var caf = submitAndDownloadCaf();
        assertPdfFieldEquals("RECEIVED_LIHEAP", "No", caf);
    }

    @Test
    void shouldMapChildrenNeedingChildcareFullNames() throws Exception {
        selectPrograms("CCAP");
        addHouseholdMembers();

        String jimHalpertId = getFirstHouseholdMemberId();
        postWithData("childrenInNeedOfCare",
                     "whoNeedsChildCare",
                     List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId)
        );

        postWithData("whoHasParentNotAtHome",
                     "whoHasAParentNotLivingAtHome",
                     List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId)
        );
        postWithData("parentNotAtHomeNames", Map.of(
                "whatAreTheParentsNames", List.of("", "Jim's Parent"),
                "childIdMap", List.of("applicant", jimHalpertId)
        ));

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_0", "Dwight Schrute", ccap);
        assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_1", "Jim Halpert", ccap);
        assertPdfFieldEquals("CHILD_FULL_NAME_0", "Dwight Schrute", ccap);
        assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_0", ccap);
        assertPdfFieldEquals("CHILD_FULL_NAME_1", "Jim Halpert", ccap);
        assertPdfFieldEquals("PARENT_NOT_LIVING_AT_HOME_1", "Jim's Parent", ccap);
        assertPdfFieldIsEmpty("CHILD_FULL_NAME_2", ccap);
        assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_2", ccap);
    }

    @Test
    void shouldNotMapParentsLivingOutsideOfHomeIfNoneSelected() throws Exception {
        selectPrograms("CCAP");

        addHouseholdMembers();

        postWithData("childrenInNeedOfCare",
                     "whoNeedsChildCare",
                     List.of("Dwight Schrute applicant", getJimFullNameAndId())
        );

        postWithData("whoHasParentNotAtHome", "whoHasAParentNotLivingAtHome", "NONE_OF_THE_ABOVE");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_0", "Dwight Schrute", ccap);
        assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_1", "Jim Halpert", ccap);
        assertPdfFieldIsEmpty("CHILD_FULL_NAME_0", ccap);
        assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_0", ccap);
        assertPdfFieldIsEmpty("CHILD_FULL_NAME_1", ccap);
        assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_1", ccap);
        assertPdfFieldIsEmpty("CHILD_FULL_NAME_2", ccap);
        assertPdfFieldIsEmpty("PARENT_NOT_LIVING_AT_HOME_2", ccap);
    }

    @Test
    void shouldDefaultToNoForMillionDollarQuestionWhenQuestionPageIsNotShown() throws Exception {
        selectPrograms("CCAP");

        postWithData("energyAssistance", "energyAssistance", "false");
        postWithData("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE");
        postWithData("supportAndCare", "supportAndCare", "false");
        postWithData("vehicle", "haveVehicle", "false");
        postWithData("realEstate", "ownRealEstate", "false");
        postWithData("investments", "haveInvestments", "false");
        postWithData("savings", "haveSavings", "false");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("HAVE_MILLION_DOLLARS", "No", ccap);
    }

    @Test
    void shouldMarkYesForMillionDollarQuestionWhenChoiceIsYes() throws Exception {
        selectPrograms("CCAP");

        postWithData("energyAssistance", "energyAssistance", "false");
        postWithData("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE");
        postWithData("supportAndCare", "supportAndCare", "false");
        postWithData("vehicle", "haveVehicle", "false");
        postWithData("realEstate", "ownRealEstate", "false");
        postWithData("investments", "haveInvestments", "true");
        postWithData("savings", "haveSavings", "false");
        postWithData("millionDollar", "haveMillionDollars", "true");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("HAVE_MILLION_DOLLARS", "Yes", ccap);
    }

    @Test
    void shouldNotMapUnearnedIncomeCcapWhenNoneOfTheAboveIsSelected() throws Exception {
        fillInRequiredPages();
        postWithData("unearnedIncomeCcap", "unearnedIncomeCcap", "NO_UNEARNED_INCOME_CCAP_SELECTED");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("BENEFITS", "No", ccap);
        assertPdfFieldEquals("INSURANCE_PAYMENTS", "No", ccap);
        assertPdfFieldEquals("CONTRACT_FOR_DEED", "No", ccap);
        assertPdfFieldEquals("TRUST_MONEY", "No", ccap);
        assertPdfFieldEquals("HEALTH_CARE_REIMBURSEMENT", "No", ccap);
        assertPdfFieldEquals("INTEREST_DIVIDENDS", "No", ccap);
        assertPdfFieldEquals("OTHER_SOURCES", "No", ccap);
    }

    @Test
    void shouldMapAdultsInHouseholdRequestingChildcareAssistance() throws Exception {
        selectPrograms("CCAP");

        addHouseholdMembers();

        String jim = getJimFullNameAndId();
        postWithData("childrenInNeedOfCare", "whoNeedsChildCare", jim);

        postWithData("jobSearch", "currentlyLookingForJob", "true");
        String pam = getPamFullNameAndId();
        postWithData("whoIsLookingForAJob", "whoIsLookingForAJob", List.of(jim, pam));

        String me = "Dwight Schrute applicant";
        postWithData("whoIsGoingToSchool", "whoIsGoingToSchool", List.of(me, jim));

        // Add a job for Jim
        postWithQueryParam("incomeByJob", "option", "0");
        addJob(jim, "Jim's Employer");

        // Add a job for Pam
        postWithQueryParam("jobBuilder", "option", "0");
        addJob(pam, "Pam's Employer");

        var ccap = submitAndDownloadCcap();
        assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_0", "Pam Beesly", ccap);
        assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_0", "Dwight Schrute", ccap);
        assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_0", "Pam Beesly", ccap);
        assertPdfFieldEquals("ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_0", "Pam's Employer", ccap);
        assertPdfFieldIsEmpty("ADULT_REQUESTING_CHILDCARE_LOOKING_FOR_JOB_FULL_NAME_1", ccap);
        assertPdfFieldIsEmpty("ADULT_REQUESTING_CHILDCARE_GOING_TO_SCHOOL_FULL_NAME_1", ccap);
        assertPdfFieldIsEmpty("ADULT_REQUESTING_CHILDCARE_WORKING_FULL_NAME_1", ccap);
        assertPdfFieldIsEmpty("ADULT_REQUESTING_CHILDCARE_WORKING_EMPLOYERS_NAME_1", ccap);
    }

    @NotNull
    private String getPamFullNameAndId() throws Exception {
        return "Pam Beesly " + getSecondHouseholdMemberId();
    }

    @NotNull
    private String getJimFullNameAndId() throws Exception {
        return "Jim Halpert " + getFirstHouseholdMemberId();
    }

    private void addJob(String householdMemberFullNameAndId, String employersName) throws Exception {
        postWithData("householdSelectionForIncome", "whoseJobIsIt", householdMemberFullNameAndId);
        postWithData("employersName", "employersName", employersName);
        postWithData("selfEmployment", "selfEmployment", "false");
        postWithData("paidByTheHour", "paidByTheHour", "false");
        postWithData("payPeriod", "payPeriod", "EVERY_WEEK");
        postWithData("incomePerPayPeriod", "incomePerPayPeriod", "1");
    }

    private void postWithQueryParam(String pageName, String queryParam, String value) throws Exception {
        mockMvc.perform(
                post("/pages/" + pageName).session(session).with(csrf()).queryParam(queryParam, value)
        ).andExpect(redirectedUrl("/pages/" + pageName + "/navigation"));
    }


    private void addHouseholdMembers() throws Exception {
        postWithData("personalInfo", Map.of(
                "firstName", List.of("Dwight"),
                "lastName", List.of("Schrute"),
                "dateOfBirth", List.of("01", "12", "1928")
        ));
        postWithData("addHouseholdMembers", "addHouseholdMembers", "true");
        postWithData("householdMemberInfo", Map.of(
                "firstName", List.of("Jim"),
                "lastName", List.of("Halpert"),
                "programs", List.of("CCAP")
        ));
        postWithData("householdMemberInfo", Map.of(
                "firstName", List.of("Pam"),
                "lastName", List.of("Beesly"),
                "programs", List.of("CCAP")
        ));
    }

    private String getFirstHouseholdMemberId() throws Exception {
        return getHouseholdMemberIdAtIndex(0);
    }

    private String getSecondHouseholdMemberId() throws Exception {
        return getHouseholdMemberIdAtIndex(1);
    }

    private String getHouseholdMemberIdAtIndex(int index) throws Exception {
        ModelAndView modelAndView = Objects.requireNonNull(
                mockMvc.perform(get("/pages/childrenInNeedOfCare").session(session)).andReturn().getModelAndView());
        PageTemplate pageTemplate = (PageTemplate) modelAndView.getModel().get("page");
        ReferenceOptionsTemplate options = (ReferenceOptionsTemplate) pageTemplate.getInputs().get(0).getOptions();
        return options.getSubworkflows().get("household").get(index).getId().toString();
    }

    private PDAcroForm submitAndDownloadCaf() throws Exception {
        submitApplication();
        return downloadCaf();
    }

    private PDAcroForm submitAndDownloadCcap() throws Exception {
        submitApplication();
        return downloadCcap();
    }

    private PDAcroForm downloadCaf() throws Exception {
        var cafBytes = mockMvc.perform(get("/download").session(session))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        return PDDocument.load(cafBytes).getDocumentCatalog().getAcroForm();
    }

    private PDAcroForm downloadCcap() throws Exception {
        var ccapBytes = mockMvc.perform(get("/download-ccap").session(session))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        return PDDocument.load(ccapBytes).getDocumentCatalog().getAcroForm();
    }

    private void fillInRequiredPages() throws Exception {
        postWithData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false");
        postWithData("utilities", "payForUtilities", "COOLING");
    }

    private void submitApplication() throws Exception {
        postWithData("/submit",
                     "/pages/signThisApplication/navigation",
                     Map.of("applicantSignature", List.of("Human McPerson")));
    }

    private void selectPrograms(String... programs) throws Exception {
        postWithData("choosePrograms", "programs", Arrays.stream(programs).toList());
    }

    // Post to a page with an arbitrary number of multi-value inputs
    private void postWithData(String pageName, Map<String, List<String>> params) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        postWithData(postUrl, postUrl + "/navigation", params);
    }

    // Post to a page with a single input that only accepts a single value
    private void postWithData(String pageName, String inputName, String value) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        var params = Map.of(inputName, List.of(value));
        postWithData(postUrl, postUrl + "/navigation", params);
    }

    // Post to a page with a single input that accepts multiple values
    private void postWithData(String pageName, String inputName, List<String> values) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        postWithData(postUrl, postUrl + "/navigation", Map.of(inputName, values));
    }

    private void postWithData(String postUrl, String redirectUrl, Map<String, List<String>> params) throws Exception {
        Map<String, List<String>> paramsWithProperInputNames = params.entrySet().stream()
                .collect(toMap(e -> e.getKey() + "[]", Map.Entry::getValue));
        mockMvc.perform(
                post(postUrl)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .params(new LinkedMultiValueMap<>(paramsWithProperInputNames))
        ).andExpect(redirectedUrl(redirectUrl));
    }

    private String getUrlForPageName(String pageName) {
        return "/pages/" + pageName;
    }
}
