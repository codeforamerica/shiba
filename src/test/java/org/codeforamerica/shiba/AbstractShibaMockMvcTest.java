package org.codeforamerica.shiba;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.framework.FormPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.config.PageTemplate;
import org.codeforamerica.shiba.pages.config.ReferenceOptionsTemplate;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.TestUtils.resetApplicationData;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @BeforeEach
    protected void setUp() throws Exception {
        session = new MockHttpSession();

        when(clock.instant()).thenReturn(Instant.now());
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

    protected void postExpectingSuccessAndAssertRedirectPageTitleIsCorrect(String pageName,
                                                                           String inputName,
                                                                           String value,
                                                                           String nextPageTitle) throws Exception {
        var nextPage = postExpectingSuccessAndFollowRedirect(pageName, inputName, value);
        assertThat(nextPage.getTitle()).isEqualTo(nextPageTitle);
    }

    protected void postExpectingSuccessAndAssertRedirectPageTitleIsCorrect(String pageName,
                                                                           String inputName,
                                                                           List<String> values,
                                                                           String nextPageTitle) throws Exception {
        var nextPage = postExpectingSuccessAndFollowRedirect(pageName, inputName, values);
        assertThat(nextPage.getTitle()).isEqualTo(nextPageTitle);
    }

    protected void postExpectingSuccessAndAssertRedirectPageNameIsCorrect(String pageName, String inputName,
                                                                          String value, String expectedNextPageName) throws Exception {
        postExpectingSuccess(pageName, inputName, value);
        assertNavigationRedirectsToCorrectNextPage(pageName, expectedNextPageName);
    }

    protected void assertNavigationRedirectsToCorrectNextPage(String pageName, String expectedNextPageName) throws Exception {
        getPage(pageName + "/navigation").andExpect(redirectedUrl("/pages/" + expectedNextPageName));
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
        String redirectedUrl = Objects.requireNonNull(getPage(currentPageName + "/navigation").andExpect(status().is3xxRedirection())
                                                              .andReturn()
                                                              .getResponse()
                                                              .getRedirectedUrl());

        ResultActions nextPage = mockMvc.perform(get((redirectedUrl)).session(session));
        return new FormPage(nextPage);
    }

    protected FormPage postExpectingSuccessAndFollowRedirect(String pageName, String inputName, String value) throws
            Exception {
        postExpectingSuccess(pageName, inputName, value);
        return getNextPageAsFormPage(pageName);
    }

    protected FormPage postExpectingSuccessAndFollowRedirect(String pageName, String inputName,
                                                             List<String> values) throws
            Exception {
        postExpectingSuccess(pageName, inputName, values);
        return getNextPageAsFormPage(pageName);
    }
}
