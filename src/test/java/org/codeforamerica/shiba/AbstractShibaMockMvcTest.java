package org.codeforamerica.shiba;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.ModelAndView;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.TestUtils.resetApplicationData;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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
        postWithData("householdSelectionForIncome", "whoseJobIsIt", householdMemberFullNameAndId);
        postWithData("employersName", "employersName", employersName);
        postWithData("selfEmployment", "selfEmployment", "false");
        postWithData("paidByTheHour", "paidByTheHour", "false");
        postWithData("payPeriod", "payPeriod", "EVERY_WEEK");
        postWithData("incomePerPayPeriod", "incomePerPayPeriod", "1");
    }

    protected void postWithQueryParam(String pageName, String queryParam, String value) throws Exception {
        mockMvc.perform(
                post("/pages/" + pageName).session(session).with(csrf()).queryParam(queryParam, value)
        ).andExpect(redirectedUrl("/pages/" + pageName + "/navigation"));
    }

    protected void getWithQueryParam(String pageName, String queryParam, String value) throws Exception {
        mockMvc.perform(
                get("/pages/" + pageName).session(session).queryParam(queryParam, value)
        ).andExpect(status().isOk());
    }

    protected void addHouseholdMembers() throws Exception {
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
        postWithData("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false");
        postWithData("utilities", "payForUtilities", "COOLING");
    }

    protected void fillOutPersonalInfo() throws Exception {
        postWithData("personalInfo", Map.of(
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
        postWithData("contactInfo", Map.of(
                "phoneNumber", List.of("7234567890"),
                "phoneOrEmail", List.of("TEXT")
        ));
    }

    protected void submitApplication() throws Exception {
        postWithData("/submit",
                     "/pages/signThisApplication/navigation",
                     Map.of("applicantSignature", List.of("Human McPerson")));
    }

    protected void selectPrograms(String... programs) throws Exception {
        postWithData("choosePrograms", "programs", Arrays.stream(programs).toList());
    }

    // Post to a page with an arbitrary number of multi-value inputs
    protected ResultActions postWithData(String pageName, Map<String, List<String>> params) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        return postWithData(postUrl, postUrl + "/navigation", params);
    }

    // Post to a page with a single input that only accepts a single value
    protected ResultActions postWithData(String pageName, String inputName, String value) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        var params = Map.of(inputName, List.of(value));
        return postWithData(postUrl, postUrl + "/navigation", params);
    }

    // Post to a page with a single input that accepts multiple values
    protected ResultActions postWithData(String pageName, String inputName, List<String> values) throws Exception {
        String postUrl = getUrlForPageName(pageName);
        return postWithData(postUrl, postUrl + "/navigation", Map.of(inputName, values));
    }

    protected ResultActions postWithData(String postUrl, String redirectUrl, Map<String, List<String>> params) throws Exception {
        Map<String, List<String>> paramsWithProperInputNames = params.entrySet().stream()
                .collect(toMap(e -> e.getKey() + "[]", Map.Entry::getValue));
        return mockMvc.perform(
                post(postUrl)
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .params(new LinkedMultiValueMap<>(paramsWithProperInputNames))
        ).andExpect(redirectedUrl(redirectUrl));
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

    @NotNull
    protected ResultMatcher pageHasInputError() {
        return content().string(containsString("text--error"));
    }

    @NotNull
    protected ResultMatcher pageDoesNotHaveInputError() {
        return content().string(not(containsString("text--error")));
    }
}
