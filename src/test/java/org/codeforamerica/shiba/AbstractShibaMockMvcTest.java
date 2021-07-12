package org.codeforamerica.shiba;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.framework.FormPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.config.PageTemplate;
import org.codeforamerica.shiba.pages.config.ReferenceOptionsTemplate;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.ModelAndView;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.TestUtils.resetApplicationData;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = MOCK)
@AutoConfigureMockMvc
@Import({SessionScopedApplicationDataTestConfiguration.class})
public class AbstractShibaMockMvcTest {
    @MockBean
    protected Clock clock;

    @MockBean
    protected LocationClient locationClient;

    @MockBean
    protected FeatureFlagConfiguration featureFlagConfiguration;

    @Autowired
    protected ApplicationData applicationData;

    @Autowired
    protected MockMvc mockMvc;

    protected MockHttpSession session;

    @Value("${shiba-username}:${shiba-password}")
    protected String authParams;

    @BeforeEach
    protected void setUp() throws Exception {
        session = new MockHttpSession();
        when(clock.instant()).thenReturn(
                LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(),
                LocalDateTime.of(2020, 1, 1, 10, 15, 30).atOffset(ZoneOffset.UTC).toInstant()
        );
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(locationClient.validateAddress(any())).thenReturn(Optional.empty());
        when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("county-anoka")).thenReturn(FeatureFlag.OFF);
    }

    @AfterEach
    void cleanup() {
        resetApplicationData(applicationData);
    }

    @NotNull
    protected String getApplicantFullNameAndId() {
        return "Dwight Schrute applicant";
    }

    @NotNull
    protected String getPamFullNameAndId() throws Exception {
        return "Pam Beesly " + getSecondHouseholdMemberId();
    }

    @NotNull
    protected String getJimFullNameAndId() throws Exception {
        return "Jim Halpert " + getFirstHouseholdMemberId();
    }

    protected void addJob(String householdMemberFullNameAndId, String employersName) throws Exception {
        postExpectingSuccess("householdSelectionForIncome", "whoseJobIsIt", householdMemberFullNameAndId);
        postExpectingSuccess("employersName", "employersName", employersName);
        postExpectingSuccess("selfEmployment", "selfEmployment", "false");
        postExpectingSuccess("paidByTheHour", "paidByTheHour", "false");
        postExpectingSuccess("payPeriod", "payPeriod", "EVERY_WEEK");
        postExpectingSuccess("incomePerPayPeriod", "incomePerPayPeriod", "1");
    }

    protected void postWithQueryParam(String pageName, String queryParam, String value) throws Exception {
        mockMvc.perform(
                post("/pages/" + pageName).session(session).with(csrf()).queryParam(queryParam, value)
        ).andExpect(redirectedUrl("/pages/" + pageName + "/navigation"));
    }

    protected ResultActions getWithQueryParam(String pageName, String queryParam, String value) throws Exception {
        return mockMvc.perform(
                get("/pages/" + pageName).session(session).queryParam(queryParam, value)
        ).andExpect(status().isOk());
    }

    protected ResultActions getPageWithAuth(String pageName) throws Exception {
        return mockMvc.perform(
                get(String.format("http://%s@localhost/%s", authParams, pageName)).session(session)
        ).andExpect(status().isOk());
    }

    protected void getWithQueryParamAndExpectRedirect(String pageName, String queryParam, String value,
                                                      String expectedPageName) throws Exception {
        var navigationPageUrl = mockMvc.perform(get("/pages/" + pageName + "/navigation").session(session)
                                                        .queryParam(queryParam, value))
                .andExpect(status().is3xxRedirection())
                .andReturn()
                .getResponse()
                .getRedirectedUrl();
        String nextPage = followRedirectsForUrl(navigationPageUrl);
        assertThat(nextPage).isEqualTo("/pages/" + expectedPageName);
    }

    protected void addHouseholdMembers() throws Exception {
        postExpectingSuccess("personalInfo", Map.of(
                "firstName", List.of("Dwight"),
                "lastName", List.of("Schrute"),
                "dateOfBirth", List.of("01", "12", "1928")
        ));
        postExpectingSuccess("addHouseholdMembers", "addHouseholdMembers", "true");
        postExpectingSuccess("householdMemberInfo", Map.of(
                "firstName", List.of("Jim"),
                "lastName", List.of("Halpert"),
                "programs", List.of("CCAP")
        ));
        postExpectingSuccess("householdMemberInfo", Map.of(
                "firstName", List.of("Pam"),
                "lastName", List.of("Beesly"),
                "programs", List.of("CCAP")
        ));
    }

    protected void fillOutHousemateInfo(String programSelection) throws Exception {
        Map<String, List<String>> householdMemberInfo = new HashMap<>();
        householdMemberInfo.put("firstName", List.of("householdMemberFirstName"));
        householdMemberInfo.put("lastName", List.of("householdMemberLastName"));
        householdMemberInfo.put("otherName", List.of("houseHoldyMcMemberson"));
        householdMemberInfo.put("programs", List.of(programSelection));
        householdMemberInfo.put("relationship", List.of("housemate"));
        householdMemberInfo.put("dateOfBirth", List.of("09", "14", "1950"));
        householdMemberInfo.put("ssn", List.of("987654321"));
        householdMemberInfo.put("maritalStatus", List.of("Never married"));
        householdMemberInfo.put("sex", List.of("Male"));
        householdMemberInfo.put("livedInMnWholeLife", List.of("Yes"));
        householdMemberInfo.put("moveToMnDate", List.of("02/18/1950"));
        householdMemberInfo.put("moveToMnPreviousState", List.of("Illinois"));
        postExpectingRedirect("householdMemberInfo", householdMemberInfo, "householdList");
    }

    protected void fillOutHousemateInfoWithNoProgramsSelected() throws Exception {
        Map<String, List<String>> householdMemberInfo = new HashMap<>();
        householdMemberInfo.put("firstName", List.of("householdMemberFirstName"));
        householdMemberInfo.put("lastName", List.of("householdMemberLastName"));
        householdMemberInfo.put("otherName", List.of("houseHoldyMcMemberson"));
        householdMemberInfo.put("programs", List.of("NONE"));
        householdMemberInfo.put("relationship", List.of("housemate"));
        householdMemberInfo.put("dateOfBirth", List.of("09", "14", "1950"));
        householdMemberInfo.put("ssn", List.of("987654321"));
        householdMemberInfo.put("maritalStatus", List.of("Never married"));
        householdMemberInfo.put("sex", List.of("Male"));
        householdMemberInfo.put("livedInMnWholeLife", List.of("Yes"));
        householdMemberInfo.put("moveToMnDate", List.of("02/18/1950"));
        householdMemberInfo.put("moveToMnPreviousState", List.of("Illinois"));
        postExpectingRedirect("householdMemberInfo", householdMemberInfo, "householdList");
    }

    protected String getFirstHouseholdMemberId() throws Exception {
        return getHouseholdMemberIdAtIndex(0);
    }

    protected String getSecondHouseholdMemberId() throws Exception {
        return getHouseholdMemberIdAtIndex(1);
    }

    protected String getHouseholdMemberIdAtIndex(int index) throws Exception {
        ModelAndView modelAndView = Objects.requireNonNull(
                mockMvc.perform(get("/pages/childrenInNeedOfCare").session(session)).andReturn().getModelAndView());
        PageTemplate pageTemplate = (PageTemplate) modelAndView.getModel().get("page");
        ReferenceOptionsTemplate options = (ReferenceOptionsTemplate) pageTemplate.getInputs().get(0).getOptions();
        return options.getSubworkflows().get("household").get(index).getId().toString();
    }

    protected PDAcroForm submitAndDownloadCaf() throws Exception {
        submitApplication();
        return downloadCaf();
    }

    protected PDAcroForm submitAndDownloadCcap() throws Exception {
        submitApplication();
        return downloadCcap();
    }

    protected PDAcroForm downloadCaf() throws Exception {
        var cafBytes = mockMvc.perform(get("/download").session(session))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        return PDDocument.load(cafBytes).getDocumentCatalog().getAcroForm();
    }

    protected PDAcroForm downloadCcap() throws Exception {
        var ccapBytes = mockMvc.perform(get("/download-ccap").session(session))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        return PDDocument.load(ccapBytes).getDocumentCatalog().getAcroForm();
    }

    protected void fillInRequiredPages() throws Exception {
        postExpectingSuccess("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false");
        postExpectingSuccess("utilities", "payForUtilities", "COOLING");
    }

    protected void fillOutPersonalInfo() throws Exception {
        postExpectingSuccess("personalInfo", Map.of(
                "firstName", List.of("defaultFirstName"),
                "lastName", List.of("defaultLastName"),
                "otherName", List.of("defaultOtherName"),
                "dateOfBirth", List.of("01", "12", "1928"),
                "ssn", List.of("123456789"),
                "maritalStatus", List.of("NEVER_MARRIED"),
                "sex", List.of("FEMALE"),
                "livedInMnWholeLife", List.of("true"),
                "moveToMnDate", List.of("02", "18", "1776"),
                "moveToMnPreviousCity", List.of("Chicago")
        ));
    }

    protected void fillOutContactInfo() throws Exception {
        postExpectingSuccess("contactInfo", Map.of(
                "phoneNumber", List.of("7234567890"),
                "email", List.of("some@email.com"),
                "phoneOrEmail", List.of("TEXT")
        ));
    }

    protected void submitApplication() throws Exception {
        postExpectingSuccess("/submit",
                             "/pages/signThisApplication/navigation",
                             Map.of("applicantSignature", List.of("Human McPerson")));
    }

    protected void selectPrograms(String... programs) throws Exception {
        postExpectingSuccess("choosePrograms", "programs", Arrays.stream(programs).toList());
    }

    protected ResultActions postExpectingSuccess(String pageName) throws Exception {
        return postWithoutData(pageName).andExpect(redirectedUrl(getUrlForPageName(pageName) + "/navigation"));
    }

    // Post to a page with an arbitrary number of multi-value inputs
    protected ResultActions postExpectingSuccess(String pageName, Map<String, List<String>> params) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        return postExpectingSuccess(postUrl, postUrl + "/navigation", params);
    }

    // Post to a page with a single input that only accepts a single value
    protected ResultActions postExpectingSuccess(String pageName, String inputName, String value) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        var params = Map.of(inputName, List.of(value));
        return postExpectingSuccess(postUrl, postUrl + "/navigation", params);
    }

    // Post to a page with a single input that accepts multiple values
    protected ResultActions postExpectingSuccess(String pageName, String inputName,
                                                 List<String> values) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        return postExpectingSuccess(postUrl, postUrl + "/navigation", Map.of(inputName, values));
    }

    protected ResultActions postExpectingSuccess(String postUrl, String redirectUrl,
                                                 Map<String, List<String>> params) throws
            Exception {
        Map<String, List<String>> paramsWithProperInputNames = fixInputNamesForParams(params);
        return mockMvc.perform(
                post(postUrl)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .params(new LinkedMultiValueMap<>(paramsWithProperInputNames))
        ).andExpect(redirectedUrl(redirectUrl));
    }

    protected void postExpectingNextPageElementText(String pageName,
                                                    String inputName,
                                                    String value,
                                                    String elementId,
                                                    String expectedText) throws Exception {
        var nextPage = postAndFollowRedirect(pageName, inputName, value);
        assertThat(nextPage.findElementTextById(elementId)).isEqualTo(expectedText);
    }

    protected void assertPageHasElementWithId(String pageName, String elementId) throws Exception {
        var page = new FormPage(getPage(pageName));
        assertThat(page.getElementById(elementId)).isNotNull();
    }

    protected void assertPageDoesNotHaveElementWithId(String pageName, String elementId) throws Exception {
        var page = new FormPage(getPage(pageName));
        assertThat(page.getElementById(elementId)).isNull();
    }

    protected void postExpectingNextPageTitle(String pageName, String nextPageTitle) throws Exception {
        var nextPage = postAndFollowRedirect(pageName);
        assertThat(nextPage.getTitle()).isEqualTo(nextPageTitle);
    }

    protected void postExpectingNextPageTitle(String pageName,
                                              String inputName,
                                              String value,
                                              String nextPageTitle) throws Exception {
        var nextPage = postAndFollowRedirect(pageName, inputName, value);
        assertThat(nextPage.getTitle()).isEqualTo(nextPageTitle);
    }

    protected void postExpectingNextPageTitle(String pageName,
                                              String inputName,
                                              List<String> values,
                                              String nextPageTitle) throws Exception {
        var nextPage = postAndFollowRedirect(pageName, inputName, values);
        assertThat(nextPage.getTitle()).isEqualTo(nextPageTitle);
    }

    protected void postExpectingNextPageTitle(String pageName,
                                              Map<String, List<String>> params,
                                              String nextPageTitle) throws Exception {
        var nextPage = postAndFollowRedirect(pageName, params);
        assertThat(nextPage.getTitle()).isEqualTo(nextPageTitle);
    }

    protected void postExpectingRedirect(String pageName, String inputName,
                                         String value, String expectedNextPageName) throws Exception {
        postExpectingSuccess(pageName, inputName, value);
        assertNavigationRedirectsToCorrectNextPage(pageName, expectedNextPageName);
    }

    protected void postExpectingRedirect(String pageName, String expectedNextPageName) throws Exception {
        postExpectingSuccess(pageName);
        assertNavigationRedirectsToCorrectNextPage(pageName, expectedNextPageName);
    }

    protected void postExpectingRedirect(String pageName, Map<String, List<String>> params,
                                         String expectedNextPageName) throws Exception {
        postExpectingSuccess(pageName, params);
        assertNavigationRedirectsToCorrectNextPage(pageName, expectedNextPageName);
    }

    protected void assertNavigationRedirectsToCorrectNextPage(String pageName,
                                                              String expectedNextPageName) throws Exception {
        String nextPage = followRedirectsForPageName(pageName);
        assertThat(nextPage).isEqualTo("/pages/" + expectedNextPageName);
    }

    protected ResultActions postExpectingFailure(String pageName, String inputName, String value) throws Exception {
        return postExpectingFailure(pageName, Map.of(inputName, List.of(value)));
    }

    protected ResultActions postExpectingFailure(String pageName, String inputName,
                                                 List<String> values) throws Exception {
        return postExpectingFailure(pageName, Map.of(inputName, values));
    }

    protected ResultActions postExpectingFailure(String pageName, Map<String, List<String>> params) throws Exception {
        Map<String, List<String>> paramsWithProperInputNames = fixInputNamesForParams(params);
        String postUrl = getUrlForPageName(pageName);
        return mockMvc.perform(
                post(postUrl)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .params(new LinkedMultiValueMap<>(paramsWithProperInputNames))
        ).andExpect(redirectedUrl(postUrl));
    }

    protected void postExpectingFailureAndAssertErrorDisplaysForThatInput(String pageName, String inputName,
                                                                          String value) throws Exception {
        postExpectingFailure(pageName, inputName, value);
        assertPageHasInputError(pageName, inputName);
    }

    protected void postExpectingFailureAndAssertErrorDisplaysForThatInput(String pageName, String inputName,
                                                                          List<String> values) throws Exception {
        postExpectingFailure(pageName, inputName, values);
        assertPageHasInputError(pageName, inputName);
    }


    protected void postExpectingFailureAndAssertErrorDisplaysOnDifferentInput(String pageName, String inputName,
                                                                              String value,
                                                                              String inputNameWithError) throws Exception {
        postExpectingFailure(pageName, inputName, value);
        assertPageHasInputError(pageName, inputNameWithError);
    }

    @NotNull
    private Map<String, List<String>> fixInputNamesForParams(Map<String, List<String>> params) {
        return params.entrySet().stream()
                .collect(toMap(e -> e.getKey() + "[]", Map.Entry::getValue));
    }

    protected ResultActions postWithoutData(String pageName) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        return mockMvc.perform(
                post(postUrl)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        );
    }

    protected String getUrlForPageName(String pageName) {
        return "/pages/" + pageName;
    }

    protected void assertPageHasInputError(String pageName, String inputName) throws Exception {
        var page = new FormPage(getPage(pageName));
        assertTrue(page.hasInputError(inputName));
    }

    protected void assertPageDoesNotHaveInputError(String pageName, String inputName) throws Exception {
        var page = new FormPage(getPage(pageName));
        assertFalse(page.hasInputError(inputName));
    }

    protected void assertPageHasWarningMessage(String pageName, String warningMessage) throws Exception {
        var page = new FormPage(getPage(pageName));
        assertEquals(page.getWarningMessage(), warningMessage);
    }

    @NotNull
    protected ResultActions getPage(String pageName) throws Exception {
        return mockMvc.perform(get("/pages/" + pageName).session(session));
    }

    /**
     * Accepts the page you are on and follows the redirects to get the next page
     *
     * @param currentPageName the page
     * @return a form page that can be asserted against
     */
    protected FormPage getNextPageAsFormPage(String currentPageName) throws Exception {
        String nextPage = followRedirectsForPageName(currentPageName);
        return new FormPage(mockMvc.perform(get((nextPage)).session(session)));
    }

    @NotNull
    private String followRedirectsForPageName(String currentPageName) throws Exception {
        var nextPage = "/pages/" + currentPageName + "/navigation";
        while (nextPage.contains("/navigation")) {
            // follow redirects
            nextPage = mockMvc.perform(get(nextPage).session(session))
                    .andExpect(status().is3xxRedirection()).andReturn()
                    .getResponse()
                    .getRedirectedUrl();
        }
        return nextPage;
    }

    private String followRedirectsForUrl(String currentPageUrl) throws Exception {
        var nextPage = currentPageUrl;
        while (nextPage.contains("/navigation")) {
            // follow redirects
            nextPage = mockMvc.perform(get(nextPage).session(session))
                    .andExpect(status().is3xxRedirection()).andReturn()
                    .getResponse()
                    .getRedirectedUrl();
        }
        return nextPage;
    }

    protected FormPage postAndFollowRedirect(String pageName, String inputName, String value) throws
            Exception {
        postExpectingSuccess(pageName, inputName, value);
        return getNextPageAsFormPage(pageName);
    }

    protected FormPage postAndFollowRedirect(String pageName, Map<String, List<String>> params) throws
            Exception {
        postExpectingSuccess(pageName, params);
        return getNextPageAsFormPage(pageName);
    }

    protected FormPage postAndFollowRedirect(String pageName) throws
            Exception {
        postExpectingSuccess(pageName);
        return getNextPageAsFormPage(pageName);
    }

    protected FormPage postAndFollowRedirect(String pageName, String inputName,
                                             List<String> values) throws
            Exception {
        postExpectingSuccess(pageName, inputName, values);
        return getNextPageAsFormPage(pageName);
    }

    protected void getPageAndExpectRedirect(String getPageName, String redirectPageName) throws Exception {
        getPage(getPageName).andExpect(redirectedUrl("/pages/" + redirectPageName));
    }

    protected void assertCorrectPageTitle(String pageName, String pageTitle) throws Exception {
        assertThat(new FormPage(getPage(pageName)).getTitle()).isEqualTo(pageTitle);
    }

    protected void fillFutureIncomeToHaveVehicle() throws Exception {
        postExpectingRedirect("futureIncome", "earnLessMoneyThisMonth", "false", "startExpenses");
        assertNavigationRedirectsToCorrectNextPage("startExpenses", "homeExpenses");
        postExpectingRedirect("homeExpenses", "homeExpenses", "NONE_OF_THE_ABOVE", "utilities");
        postExpectingRedirect("utilities", "payForUtilities", "NONE_OF_THE_ABOVE", "energyAssistance");
        postExpectingRedirect("energyAssistance", "energyAssistance", "false", "medicalExpenses");
        postExpectingRedirect("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE", "supportAndCare");
        postExpectingRedirect("supportAndCare", "supportAndCare", "false", "vehicle");
        postExpectingSuccess("vehicle", "haveVehicle", "false");
    }

    protected void completeFlowFromLandingPageThroughReviewInfo(String... programSelections) throws Exception {
        completeFlowFromLandingPageThroughContactInfo(programSelections);
    }

    protected void completeFlowFromLandingPageThroughContactInfo(String... programSelections) throws Exception {
        getToPersonalInfoScreen(programSelections);
        fillOutPersonalInfo();
        fillOutContactInfo();
        fillOutHomeAddress();
        postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "false");
        fillOutMailingAddress();
        postExpectingNextPageElementText("verifyMailingAddress",
                "useEnrichedAddress",
                "true",
                "mailingAddress-address_street",
                "smarty street");
    }

    protected void getToPersonalInfoScreen(String... programSelections) throws Exception {
        selectPrograms(programSelections);
    }

    protected void fillOutHomeAddress() throws Exception {
        postExpectingSuccess("homeAddress", Map.of(
                "streetAddress", List.of("someStreetAddress"),
                "apartmentNumber", List.of("someApartmentNumber"),
                "city", List.of("someCity"),
                "zipCode", List.of("12345"),
                "state", List.of("MN"),
                "sameMailingAddress", List.of("false")
        ));
    }

    protected void fillOutMailingAddress() throws Exception {
        when(locationClient.validateAddress(any())).thenReturn(
                Optional.of(new Address("smarty street", "City", "CA", "03104", "", "someCounty"))
        );
        postExpectingSuccess("mailingAddress", Map.of(
                "streetAddress", List.of("someStreetAddress"),
                "apartmentNumber", List.of("someApartmentNumber"),
                "city", List.of("someCity"),
                "zipCode", List.of("12345"),
                "state", List.of("IL")
        ));
    }


    protected FormPage nonExpeditedFlowToSuccessPage(boolean hasHousehold, boolean isWorking) throws Exception {
        return nonExpeditedFlowToSuccessPage(hasHousehold, isWorking, false, false);
    }

    private FormPage nonExpeditedFlowToSuccessPage(boolean hasHousehold, boolean isWorking, boolean helpWithBenefits,
                                                   boolean hasHealthcareCoverage) throws Exception {
        completeFlowFromLandingPageThroughReviewInfo("CCAP", "CASH");
        var me = "defaultFirstName defaultLastName applicant";
        if (hasHousehold) {
            postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "true", "startHousehold");

            fillOutHousemateInfo("CCAP");

            postExpectingNextPageTitle("childrenInNeedOfCare",
                                       "whoNeedsChildCare",
                                       "householdMemberFirstName householdMemberLastName" + getFirstHouseholdMemberId(),
                                       "Who are the children that have a parent not living in the home?"
            );
            postExpectingRedirect("whoHasParentNotAtHome",
                                  "whoHasAParentNotLivingAtHome",
                                  "NONE_OF_THE_ABOVE",
                                  "livingSituation");

            postExpectingRedirect("livingSituation", "livingSituation", "UNKNOWN", "goingToSchool");
            postExpectingRedirect("goingToSchool", "goingToSchool", "false", "pregnant");
            postExpectingRedirect("pregnant", "isPregnant", "true", "whoIsPregnant");
            postExpectingRedirect("whoIsPregnant", "whoIsPregnant", me, "migrantFarmWorker");

        } else {
            postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "false", "introPersonalDetails");
            assertNavigationRedirectsToCorrectNextPage("introPersonalDetails", "livingSituation");
            postExpectingRedirect("livingSituation", "livingSituation", "UNKNOWN", "goingToSchool");
            postExpectingRedirect("goingToSchool", "goingToSchool", "false", "pregnant");
            postExpectingRedirect("pregnant", "isPregnant", "false", "migrantFarmWorker");
        }

        postExpectingRedirect("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false", "usCitizen");

        if (hasHousehold) {
            postExpectingRedirect("usCitizen", "isUsCitizen", "false", "whoIsNonCitizen");
            postExpectingRedirect("whoIsNonCitizen", "whoIsNonCitizen", me, "disability");
        } else {
            postExpectingRedirect("usCitizen", "isUsCitizen", "true", "disability");
        }

        postExpectingRedirect("disability", "hasDisability", "false", "workSituation");
        postExpectingRedirect("workSituation", "hasWorkSituation", "false", "introIncome");
        assertNavigationRedirectsToCorrectNextPage("introIncome", "employmentStatus");
        if (isWorking) {
            postExpectingRedirect("employmentStatus", "areYouWorking", "true", "incomeByJob");

            postWithQueryParam("incomeByJob", "option", "0");
            if (hasHousehold) {
                postExpectingRedirect("householdSelectionForIncome",
                                      "whoseJobIsIt",
                                      "householdMemberFirstName householdMemberLastName" + getFirstHouseholdMemberId(),
                                      "employersName");
            }
            postExpectingRedirect("employersName", "employersName", "some employer", "selfEmployment");
            postExpectingRedirect("selfEmployment", "selfEmployment", "", "paidByTheHour");
            postExpectingRedirect("paidByTheHour", "paidByTheHour", "true", "hourlyWage");
            postExpectingRedirect("hourlyWage", "hourlyWage", "1", "hoursAWeek");
            postExpectingRedirect("hoursAWeek", "hoursAWeek", "30", "jobBuilder");

            postExpectingSuccess("jobSearch", "currentlyLookingForJob", "false");

        } else {
            postExpectingRedirect("employmentStatus", "areYouWorking", "false", "incomeByJob");
            postExpectingSuccess("jobSearch", "currentlyLookingForJob", "true");

            if (hasHousehold) {
                assertNavigationRedirectsToCorrectNextPage("jobSearch", "whoIsLookingForAJob");
                String householdMemberId = getHouseholdMemberIdAtIndex(0);

                postExpectingRedirect("whoIsLookingForAJob",
                                      "whoIsLookingForAJob",
                                      "householdMemberFirstName householdMemberLastName" + householdMemberId,
                                      "incomeUpNext");
            } else {
                assertNavigationRedirectsToCorrectNextPage("jobSearch", "incomeUpNext");
            }
        }
        assertNavigationRedirectsToCorrectNextPage("incomeUpNext", "unearnedIncome");
        postExpectingRedirect("unearnedIncome", "unearnedIncome", "SOCIAL_SECURITY", "unearnedIncomeSources");
        postExpectingRedirect("unearnedIncomeSources", "socialSecurityAmount", "200", "unearnedIncomeCcap");
        postExpectingRedirect("unearnedIncomeCcap", "unearnedIncomeCcap", "TRUST_MONEY", "unearnedIncomeSourcesCcap");
        postExpectingRedirect("unearnedIncomeSourcesCcap", "trustMoneyAmount", "200", "futureIncome");
        postExpectingRedirect("futureIncome", "earnLessMoneyThisMonth", "true", "startExpenses");
        assertNavigationRedirectsToCorrectNextPage("startExpenses", "homeExpenses");
        postExpectingRedirect("homeExpenses", "homeExpenses", "RENT", "homeExpensesAmount");
        postExpectingRedirect("homeExpensesAmount", "homeExpensesAmount", "123321", "utilities");
        postExpectingRedirect("utilities", "payForUtilities", "HEATING", "energyAssistance");
        postExpectingRedirect("energyAssistance", "energyAssistance", "true", "energyAssistanceMoreThan20");
        postExpectingRedirect("energyAssistanceMoreThan20", "energyAssistanceMoreThan20", "true", "medicalExpenses");
        postExpectingRedirect("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE", "supportAndCare");
        postExpectingRedirect("supportAndCare", "supportAndCare", "true", "vehicle");
        postExpectingRedirect("vehicle", "haveVehicle", "false", "realEstate");
        postExpectingRedirect("realEstate", "ownRealEstate", "true", "investments");
        postExpectingRedirect("investments", "haveInvestments", "false", "savings");

        postExpectingRedirect("savings", "haveSavings", "true", "savingsAmount");
        postExpectingRedirect("savingsAmount", "liquidAssets", "1234", "millionDollar");
        postExpectingRedirect("millionDollar", "haveMillionDollars", "false", "soldAssets");
        postExpectingRedirect("soldAssets", "haveSoldAssets", "false", "submittingApplication");
        assertNavigationRedirectsToCorrectNextPage("submittingApplication", "registerToVote");
        postExpectingRedirect("registerToVote", "registerToVote", "YES", "healthcareCoverage");
        postExpectingRedirect("healthcareCoverage", "healthcareCoverage",
                              hasHealthcareCoverage ? "YES" : "NO", "helper");

        completeHelperWorkflow(helpWithBenefits);
        postExpectingRedirect("additionalInfo",
                              "additionalInfo",
                              "Some additional information about my application",
                              "legalStuff");
        postExpectingRedirect("legalStuff",
                              Map.of("agreeToTerms", List.of("true"), "drugFelony", List.of("false")),
                              "signThisApplication");
        submitApplication();
        return new FormPage(getPage("success"));
    }

    protected void completeHelperWorkflow(boolean helpWithBenefits) throws Exception {
        if (helpWithBenefits) {
            postExpectingRedirect("helper", "helpWithBenefits", "true", "authorizedRep");
            postExpectingRedirect("authorizedRep", "communicateOnYourBehalf", "true", "speakToCounty");
            postExpectingRedirect("speakToCounty", "getMailNotices", "true", "spendOnYourBehalf");
            postExpectingRedirect("spendOnYourBehalf", "spendOnYourBehalf", "true", "helperContactInfo");

            fillOutHelperInfo();
        } else {
            postExpectingRedirect("helper", "helpWithBenefits", "false", "additionalInfo");
        }
    }

    protected void fillOutHelperInfo() throws Exception {
        postExpectingRedirect("helperContactInfo", Map.of(
                "helpersFullName", List.of("defaultFirstName defaultLastName"),
                "helpersStreetAddress", List.of("someStreetAddress"),
                "helpersCity", List.of("someCity"),
                "helpersZipCode", List.of("12345"),
                "helpersPhoneNumber", List.of("7234567890")
        ), "additionalInfo");
    }
}
