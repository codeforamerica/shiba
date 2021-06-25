package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.SessionScopedApplicationDataTestConfiguration;
import org.codeforamerica.shiba.pages.config.*;
import org.codeforamerica.shiba.pages.enrichment.LocationClient;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.TestUtils.assertPdfFieldEquals;
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
        postWithData("/pages/languagePreferences",
                     Map.of("writtenLanguage", List.of("ENGLISH"), "spokenLanguage", List.of("ENGLISH")));

        postWithData("/pages/addHouseholdMembers", Map.of("addHouseholdMembers", List.of("false")));
    }

    @Test
    void shouldAnswerEnergyAssistanceQuestion() throws Exception {
        selectPrograms(List.of("CASH"));

        postWithData("/pages/energyAssistance", Map.of("energyAssistance", List.of("true")));
        postWithData("/pages/energyAssistanceMoreThan20", Map.of("energyAssistanceMoreThan20", List.of("false")));

        var caf = submitAndDownloadCaf();
        assertThat(caf.getField("RECEIVED_LIHEAP").getValueAsString()).isEqualTo("No");
    }

    @Test
    void shouldMapEnergyAssistanceWhenUserReceivedNoAssistance() throws Exception {
        selectPrograms(List.of("CASH"));
        postWithData("/pages/energyAssistance", Map.of("energyAssistance", List.of("false")));

        var caf = submitAndDownloadCaf();
        assertThat(caf.getField("RECEIVED_LIHEAP").getValueAsString()).isEqualTo("No");
    }

    @Test
    void shouldMapChildrenNeedingChildcareFullNames() throws Exception {
        selectPrograms(List.of("CCAP"));

        postWithData("/pages/personalInfo", Map.of(
                "firstName", List.of("Dwight"),
                "lastName", List.of("Schrute"),
                "dateOfBirth", List.of("01", "12", "1928")
        ));
        postWithData("/pages/addHouseholdMembers", Map.of("addHouseholdMembers", List.of("true")));
        postWithData("/pages/householdMemberInfo", Map.of(
                "firstName", List.of("Jim"),
                "lastName", List.of("Halpert"),
                "programs", List.of("CCAP")
        ));
        postWithData("/pages/householdMemberInfo", Map.of(
                "firstName", List.of("Pam"),
                "lastName", List.of("Beesly"),
                "programs", List.of("CCAP")
        ));

        Object pageTemplate = mockMvc.perform(get("/pages/childrenInNeedOfCare").session(session))
                .andReturn()
                .getModelAndView()
                .getModel()
                .get("page");
        OptionsWithDataSourceTemplate options = ((PageTemplate) pageTemplate).getInputs().get(0).getOptions();
        String jimHalpertId = ((ReferenceOptionsTemplate) options).getSubworkflows()
                .get("household")
                .get(0)
                .getId()
                .toString();
        postWithData("/pages/childrenInNeedOfCare", Map.of(
                "whoNeedsChildCare", List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId)
        ));

        postWithData("/pages/whoHasParentNotAtHome", Map.of(
                "whoHasAParentNotLivingAtHome", List.of("Dwight Schrute applicant", "Jim Halpert " + jimHalpertId)
        ));
        postWithData("/pages/parentNotAtHomeNames", Map.of(
                "whatAreTheParentsNames", List.of("", "Jim's Parent"),
                "childIdMap", List.of("applicant", jimHalpertId)
        ));

        var ccapPdf = submitAndDownloadCcap();
        assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_0", "Dwight Schrute", ccapPdf);
        assertPdfFieldEquals("CHILD_NEEDS_CHILDCARE_FULL_NAME_1", "Jim Halpert", ccapPdf);
        assertPdfFieldEquals("CHILD_FULL_NAME_0", "Dwight Schrute", ccapPdf);
        assertPdfFieldEquals("PARENT_NOT_LIVING_AT_HOME_0", "", ccapPdf);
        assertPdfFieldEquals("CHILD_FULL_NAME_1", "Jim Halpert", ccapPdf);
        assertPdfFieldEquals("PARENT_NOT_LIVING_AT_HOME_1", "Jim's Parent", ccapPdf);
        assertPdfFieldEquals("CHILD_FULL_NAME_2", "", ccapPdf);
        assertPdfFieldEquals("PARENT_NOT_LIVING_AT_HOME_2", "", ccapPdf);
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

    private void submitApplication() throws Exception {
        postWithData("/submit",
                     "/pages/signThisApplication/navigation",
                     Map.of("applicantSignature", List.of("Human McPerson")));
    }

    private void selectPrograms(List<String> programs) throws Exception {
        postWithData("/pages/choosePrograms", Map.of("programs", programs));
    }

    private void postWithData(String postUrl, Map<String, List<String>> params) throws Exception {
        postWithData(postUrl, postUrl + "/navigation", params);
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
}
