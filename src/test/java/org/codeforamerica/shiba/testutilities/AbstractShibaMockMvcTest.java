package org.codeforamerica.shiba.testutilities;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.testutilities.TestUtils.ADMIN_EMAIL;
import static org.codeforamerica.shiba.testutilities.TestUtils.getAbsoluteFilepathString;
import static org.codeforamerica.shiba.testutilities.TestUtils.resetApplicationData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.assertj.core.api.Assertions;
import org.codeforamerica.shiba.output.Document;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.ModelAndView;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = MOCK)
@AutoConfigureMockMvc
@Import({SessionScopedApplicationDataTestConfiguration.class})
public class AbstractShibaMockMvcTest {

  private static final String UPLOADED_JPG_FILE_NAME = "shiba+file.jpg";

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
    when(clock.instant()).thenReturn(
        LocalDateTime.of(2020, 1, 1, 10, 10).atOffset(ZoneOffset.UTC).toInstant(),
        LocalDateTime.of(2020, 1, 1, 10, 15, 30).atOffset(ZoneOffset.UTC).toInstant()
    );
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(locationClient.validateAddress(any())).thenReturn(Optional.empty());
    when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
    when(featureFlagConfiguration.get("word-to-pdf")).thenReturn(FeatureFlag.OFF);
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

  protected void addJob(String householdMemberFullNameAndId, String employersName)
      throws Exception {
    postExpectingSuccess("householdSelectionForIncome", "whoseJobIsIt",
        householdMemberFullNameAndId);
    postExpectingSuccess("employersName", "employersName", employersName);
    postExpectingSuccess("selfEmployment", "selfEmployment", "false");
    postExpectingSuccess("paidByTheHour", "paidByTheHour", "false");
    postExpectingSuccess("payPeriod", "payPeriod", "EVERY_WEEK");
    postExpectingSuccess("incomePerPayPeriod", "incomePerPayPeriod", "1");
  }

  protected void addSelfEmployedJob(String householdMemberFullNameAndId, String employersName)
      throws Exception {
    postExpectingSuccess("householdSelectionForIncome", "whoseJobIsIt",
        householdMemberFullNameAndId);
    postExpectingSuccess("employersName", "employersName", employersName);
    postExpectingSuccess("selfEmployment", "selfEmployment", "true");
    postExpectingSuccess("paidByTheHour", "paidByTheHour", "true");
    postExpectingSuccess("hourlyWage", "hourlyWage", "12");
    postExpectingSuccess("hoursAWeek", "hoursAWeek", "10");
  }

  protected void postWithQueryParam(String pageName, String queryParam, String value)
      throws Exception {
    mockMvc.perform(
            post("/pages/" + pageName).session(session).with(csrf()).queryParam(queryParam, value))
        .andExpect(redirectedUrl("/pages/" + pageName + "/navigation"));
  }

  protected ResultActions getWithQueryParam(String pageName, String queryParam, String value)
      throws Exception {
    return mockMvc.perform(get("/pages/" + pageName).session(session).queryParam(queryParam, value))
        .andExpect(status().isOk());
  }

  protected ResultActions getWithQueryParamAndExpectRedirect(String pageName, String queryParam,
      String value,
      String expectedRedirectPageName) throws Exception {
    return mockMvc.perform(get("/pages/" + pageName).session(session).queryParam(queryParam, value))
        .andExpect(redirectedUrl("/pages/" + expectedRedirectPageName));
  }

  protected void getNavigationPageWithQueryParamAndExpectRedirect(String pageName,
      String queryParam, String value,
      String expectedPageName) throws Exception {
    var request = get("/pages/" + pageName + "/navigation").session(session)
        .queryParam(queryParam, value);
    var navigationPageUrl = mockMvc.perform(request)
        .andExpect(status().is3xxRedirection())
        .andReturn()
        .getResponse()
        .getRedirectedUrl();
    String nextPage = followRedirectsForUrl(navigationPageUrl);
    assertThat(nextPage).isEqualTo("/pages/" + expectedPageName);
  }

  protected void finishAddingHouseholdMembers(String expectedNextPageName) throws Exception {
    getNavigationPageWithQueryParamAndExpectRedirect("householdList", "option", "0",
        expectedNextPageName);
  }

  protected void addHouseholdMembersWithProgram(String program) throws Exception {
    postExpectingSuccess("addHouseholdMembers", "addHouseholdMembers", "true");
    postExpectingSuccess("householdMemberInfo", Map.of(
        "firstName", List.of("Jim"),
        "lastName", List.of("Halpert"),
        "programs", List.of(program),
        "relationship", List.of("spouse")
    ));
    postExpectingSuccess("householdMemberInfo", Map.of(
        "firstName", List.of("Pam"),
        "lastName", List.of("Beesly"),
        "programs", List.of(program),
        "relationship", List.of("child")
    ));
  }


  protected void fillOutHousemateInfo(String... programSelections) throws Exception {
    Map<String, List<String>> householdMemberInfo = new HashMap<>();
    householdMemberInfo.put("firstName", List.of("householdMemberFirstName"));
    householdMemberInfo.put("lastName", List.of("householdMemberLastName"));
    householdMemberInfo.put("otherName", List.of("houseHoldyMcMemberson"));
    householdMemberInfo.put("programs", List.of(programSelections));
    householdMemberInfo.put("relationship", List.of("housemate"));
    householdMemberInfo.put("dateOfBirth", List.of("09", "14", "1950"));
    householdMemberInfo.put("ssn", List.of("987654321"));
    householdMemberInfo.put("maritalStatus", List.of("Never married"));
    householdMemberInfo.put("sex", List.of("Male"));
    householdMemberInfo.put("livedInMnWholeLife", List.of("Yes"));
    householdMemberInfo.put("moveToMnDate", List.of("02", "18", "1950"));
    householdMemberInfo.put("moveToMnPreviousState", List.of("Illinois"));
    postExpectingRedirect("householdMemberInfo", householdMemberInfo, "householdList");
  }
  
  protected void fillOutHousemateInfoMoreThanFiveLessThanTen(int HHCount) throws Exception {
    Map<String, List<String>> householdMemberInfo = new HashMap<>();
    for(int i=0; i<=HHCount; i++) {
      householdMemberInfo.put("firstName", List.of("householdMemberFirstName"+i));
      householdMemberInfo.put("lastName", List.of("householdMemberLastName"+i));
      householdMemberInfo.put("otherName", List.of("houseHoldyMcMemberson"+i));
      householdMemberInfo.put("programs", List.of("SNAP"));
      householdMemberInfo.put("relationship", List.of("housemate"));
      householdMemberInfo.put("dateOfBirth", List.of("09", "14", "1950"));
      householdMemberInfo.put("ssn", List.of("987654321"));
      householdMemberInfo.put("maritalStatus", List.of("NEVER_MARRIED"));
      householdMemberInfo.put("sex", List.of("MALE"));
      householdMemberInfo.put("livedInMnWholeLife", List.of("Yes"));
      householdMemberInfo.put("moveToMnDate", List.of("02", "18", "1950"));
      householdMemberInfo.put("moveToMnPreviousState", List.of("Illinois"));
      postExpectingRedirect("householdMemberInfo", householdMemberInfo, "householdList");
    }
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
    householdMemberInfo.put("moveToMnDate", List.of("02", "18", "1950"));
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
        mockMvc.perform(get("/pages/childrenInNeedOfCare").session(session)).andReturn()
            .getModelAndView());
    PageTemplate pageTemplate = (PageTemplate) modelAndView.getModel().get("page");
    ReferenceOptionsTemplate options = (ReferenceOptionsTemplate) pageTemplate.getInputs().get(0)
        .getOptions();
    return options.getSubworkflows().get("household").get(index).getId().toString();
  }

  protected PDAcroForm submitAndDownloadCaf() throws Exception {
    submitApplication();
    return downloadCafClientPDF();
  }

  protected PDAcroForm submitAndDownloadCcap() throws Exception {
    submitApplication();
    return downloadCcapClientPDF();
  }

  protected PDAcroForm downloadCafClientPDF() throws Exception {
    var zipBytes = mockMvc.perform(get("/download")
            .with(oauth2Login()
                .attributes(attrs -> attrs.put("email", ADMIN_EMAIL)))
            .session(session))
        .andReturn()
        .getResponse()
        .getContentAsByteArray();
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
    ZipInputStream zipFile = new ZipInputStream(byteArrayInputStream);
    List<File> zippedFiles = unzip(zipFile);
    File cafFile = zippedFiles.stream().filter(file -> getDocumentType(file).equals(CAF)).toList().get(0);
    return PDDocument.load(FileUtils.readFileToByteArray(cafFile)).getDocumentCatalog().getAcroForm();
  }

  protected PDAcroForm downloadCcapClientPDF() throws Exception {
    List<File> zippedFiles = getZippedFiles();
    File ccapFile = zippedFiles.stream().filter(file -> getDocumentType(file).equals(CCAP)).toList().get(0);
    return PDDocument.load(FileUtils.readFileToByteArray(ccapFile)).getDocumentCatalog().getAcroForm();
  }
  
  protected PDAcroForm downloadCertainPopsClientPDF() throws Exception {
    List<File> zippedFiles = getZippedFiles();
    File certainPopsFile = zippedFiles.stream().filter(file -> getDocumentType(file).equals(CERTAIN_POPS)).toList().get(0);
    return PDDocument.load(FileUtils.readFileToByteArray(certainPopsFile)).getDocumentCatalog().getAcroForm();
  }

  private List<File> getZippedFiles() throws Exception {
    var zipBytes = mockMvc.perform(get("/download")
            .with(oauth2Login()
                .attributes(attrs -> attrs.put("email", ADMIN_EMAIL)))
            .session(session))
        .andReturn()
        .getResponse()
        .getContentAsByteArray();
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
    ZipInputStream zipFile = new ZipInputStream(byteArrayInputStream);
    return unzip(zipFile);
  }

  protected List<File> downloadAllClientPDFs() throws Exception {
    var zipBytes = mockMvc.perform(get("/download")
            .with(oauth2Login()
                .attributes(attrs -> attrs.put("email", ADMIN_EMAIL)))
            .session(session))
        .andReturn()
        .getResponse()
        .getContentAsByteArray();
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
    ZipInputStream zipFile = new ZipInputStream(byteArrayInputStream);
    return unzip(zipFile);
  }

  protected PDAcroForm downloadCertainPopsCaseWorkerPDF(String applicationId) throws Exception {
    var zipBytes = mockMvc.perform(get("/download/" + applicationId)
            .with(oauth2Login().attributes(attrs -> attrs.put("email", ADMIN_EMAIL)))
            .session(session))
        .andReturn()
        .getResponse()
        .getContentAsByteArray();
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
    ZipInputStream zipFile = new ZipInputStream(byteArrayInputStream);
    List<File> zippedFiles = unzip(zipFile);
    File certainPopsFile = zippedFiles.stream().filter(file -> getDocumentType(file).equals(CERTAIN_POPS)).toList().get(0);
    return PDDocument.load(FileUtils.readFileToByteArray(certainPopsFile)).getDocumentCatalog().getAcroForm();
  }

  protected Document getDocumentType(File file) {
    String fileName = file.getName();
    if (fileName.contains("_CAF")) {
      return Document.CAF;
    } else if (fileName.contains("_CCAP")) {
      return Document.CCAP;
    } else if (fileName.contains("_CERTAIN_POPS")) {
      return Document.CERTAIN_POPS;
    } else {
      return Document.CAF;
    }
  }

  protected List<File> unzip(ZipInputStream zipStream) {
    List<File> fileList = new ArrayList<>();
    try {
      ZipEntry zEntry;
      Path destination = Files.createTempDirectory("");
      while ((zEntry = zipStream.getNextEntry()) != null) {
          if (!zEntry.isDirectory()) {
            File files = new File(String.valueOf(destination), zEntry.getName());
            FileOutputStream fout = new FileOutputStream(files);
            BufferedOutputStream bufout = new BufferedOutputStream(fout);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = zipStream.read(buffer)) != -1) {
              bufout.write(buffer, 0, read);
            }
            zipStream.closeEntry();//This will delete zip folder after extraction
            bufout.close();
            fout.close();
            fileList.add(files);
          }
      }
      zipStream.close();//This will delete zip folder after extraction
    } catch (Exception e) {
      System.out.println("Unzipping failed");
      e.printStackTrace();
    }
    return fileList;
  }

  protected void fillInRequiredPages() throws Exception {
    postExpectingSuccess("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false");
    postExpectingSuccess("utilities", "payForUtilities", "COOLING");
  }

  protected void fillOutPersonalInfo() throws Exception {
    postExpectingSuccess("personalInfo", Map.of(
        "firstName", List.of("Dwight"),
        "lastName", List.of("Schrute"),
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
        "email", List.of("some@example.com"),
        "phoneOrEmail", List.of("TEXT")
    ));
  }

  protected void submitApplication() throws Exception {
    postToUrlExpectingSuccess("/submit",
        "/pages/signThisApplication/navigation",
        Map.of("applicantSignature", List.of("Human McPerson")));
  }

  protected void selectPrograms(String... programs) throws Exception {
    postExpectingSuccess("choosePrograms", "programs", Arrays.stream(programs).toList());
  }

  protected ResultActions postExpectingSuccess(String pageName) throws Exception {
    return postWithoutData(pageName)
        .andExpect(redirectedUrl(getUrlForPageName(pageName) + "/navigation"));
  }

  // Post to a page with an arbitrary number of multi-value inputs
  protected ResultActions postExpectingSuccess(String pageName, Map<String, List<String>> params)
      throws Exception {
    String postUrl = getUrlForPageName(pageName);
    return postToUrlExpectingSuccess(postUrl, postUrl + "/navigation", params);
  }

  // Post to a page with a single input that only accepts a single value
  protected ResultActions postExpectingSuccess(String pageName, String inputName, String value)
      throws Exception {
    String postUrl = getUrlForPageName(pageName);
    var params = Map.of(inputName, List.of(value));
    return postToUrlExpectingSuccess(postUrl, postUrl + "/navigation", params);
  }

  // Post to a page with a single input that accepts multiple values
  protected ResultActions postExpectingSuccess(String pageName, String inputName,
      List<String> values) throws Exception {
    String postUrl = getUrlForPageName(pageName);
    return postToUrlExpectingSuccess(postUrl, postUrl + "/navigation", Map.of(inputName, values));
  }

  protected ResultActions postToUrlExpectingSuccess(String postUrl, String redirectUrl,
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
    Assertions.assertThat(nextPage.getElementTextById(elementId)).isEqualTo(expectedText);
  }

  protected void assertPageHasElementWithId(String pageName, String elementId) throws Exception {
    var page = new FormPage(getPage(pageName));
    assertThat(page.getElementById(elementId)).isNotNull();
  }

  protected void assertPageDoesNotHaveElementWithId(String pageName, String elementId)
      throws Exception {
    var page = new FormPage(getPage(pageName));
    assertThat(page.getElementById(elementId)).isNull();
  }

  protected void postExpectingNextPageTitle(String pageName, String nextPageTitle)
      throws Exception {
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

  protected void postExpectingRedirect(String pageName, String inputName, List<String> values,
      String expectedNextPageName) throws Exception {
    postExpectingSuccess(pageName, inputName, values);
    assertNavigationRedirectsToCorrectNextPage(pageName, expectedNextPageName);
  }

  protected void postExpectingRedirect(String pageName, String expectedNextPageName)
      throws Exception {
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
  
  protected void assertNavigationRedirectsToCorrectNextPageWithOption(String pageName,String option,
      String expectedNextPageName) throws Exception {
    String nextPage = followRedirectsForPageNameWithOption(pageName, option.equals("false")?"1":"0");
    assertThat(nextPage).isEqualTo("/pages/" + expectedNextPageName);
  }

  protected ResultActions postExpectingFailure(String pageName, String inputName, String value)
      throws Exception {
    return postExpectingFailure(pageName, Map.of(inputName, List.of(value)));
  }

  protected ResultActions postExpectingFailure(String pageName, String inputName,
      List<String> values) throws Exception {
    return postExpectingFailure(pageName, Map.of(inputName, values));
  }

  protected ResultActions postExpectingFailure(String pageName, Map<String, List<String>> params)
      throws Exception {
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

  protected void postExpectingFailureAndAssertErrorDisplaysForThatInput(String pageName,
      String inputName,
      String value) throws Exception {
    postExpectingFailure(pageName, inputName, value);
    assertPageHasInputError(pageName, inputName);
  }


  protected void postExpectingFailureAndAssertErrorDisplaysForThatInput(String pageName,
      String inputName,
      String value, String errorMessage) throws Exception {
    postExpectingFailure(pageName, inputName, value);
    assertPageHasInputError(pageName, inputName, errorMessage);

  }

  protected void postExpectingFailureAndAssertErrorDisplaysForThatDateInput(String pageName,
      String inputName,
      List<String> values) throws Exception {
    postExpectingFailure(pageName, inputName, values);
    assertPageHasDateInputError(pageName, inputName);
  }


  protected void postExpectingFailureAndAssertErrorDisplaysOnDifferentInput(String pageName,
      String inputName,
      String value,
      String inputNameWithError) throws Exception {
    postExpectingFailure(pageName, inputName, value);
    assertPageHasInputError(pageName, inputNameWithError);
  }

  protected void postExpectingFailureAndAssertErrorDisplaysOnDifferentInput(String pageName,
      String inputName,
      List<String> values,
      String inputNameWithError) throws Exception {
    postExpectingFailure(pageName, inputName, values);
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

  protected void assertPageHasInputError(String pageName, String inputName, String errorMessage)
      throws Exception {
    var page = new FormPage(getPage(pageName));
    assertEquals(errorMessage, page.getInputError(inputName).text());

  }

  protected void assertPageHasDateInputError(String pageName, String inputName) throws Exception {
    var page = new FormPage(getPage(pageName));
    assertTrue(page.hasDateInputError());
  }

  protected void assertPageDoesNotHaveInputError(String pageName, String inputName)
      throws Exception {
    var page = new FormPage(getPage(pageName));
    assertFalse(page.hasInputError(inputName));
  }

  protected void assertPageHasWarningMessage(String pageName, String warningMessage)
      throws Exception {
    var page = new FormPage(getPage(pageName));
    assertEquals(page.getWarningMessage(), warningMessage);
  }

  @NotNull
  protected ResultActions getPage(String pageName) throws Exception {
    return mockMvc.perform(get("/pages/" + pageName).session(session));
  }

  @NotNull
  protected ResultActions getPageExpectingSuccess(String pageName) throws Exception {
    return getPage(pageName).andExpect(status().isOk());
  }

  /**
   * Accepts the page you are on and follows the redirects to get the next page
   *
   * @param currentPageName the page
   * @return a form page that can be asserted against
   */
  protected FormPage getNextPageAsFormPage(String currentPageName) throws Exception {
    String nextPage = followRedirectsForPageName(currentPageName);
    return new FormPage(mockMvc.perform(get(nextPage).session(session)));
  }

  @NotNull
  private String followRedirectsForPageName(String currentPageName) throws Exception {
    var nextPage = "/pages/" + currentPageName + "/navigation";
    while (Objects.requireNonNull(nextPage).contains("/navigation")) {
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
    while (Objects.requireNonNull(nextPage).contains("/navigation")) {
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

  protected void getPageAndExpectRedirect(String getPageName, String redirectPageName)
      throws Exception {
    getPage(getPageName).andExpect(redirectedUrl("/pages/" + redirectPageName));
  }

  protected void assertCorrectPageTitle(String pageName, String pageTitle) throws Exception {
    assertThat(new FormPage(getPage(pageName)).getTitle()).isEqualTo(pageTitle);
  }

  protected void fillAdditionalIncomeInfo(String... programs) throws Exception {
    postExpectingRedirect("futureIncome", "additionalIncomeInfo",
        "one more thing you need to know is...", "startExpenses");
    
    if (Arrays.stream(programs).allMatch(onlyCCAP -> onlyCCAP.contains("CCAP"))) {
    	assertNavigationRedirectsToCorrectNextPage("startExpenses", "medicalExpenses");
    }
    else {
    	assertNavigationRedirectsToCorrectNextPage("startExpenses", "homeExpenses");
	    postExpectingRedirect("homeExpenses", "homeExpenses", "NONE_OF_THE_ABOVE", "utilities");
	    postExpectingRedirect("utilities", "payForUtilities", "NONE_OF_THE_ABOVE", "energyAssistance");
	    postExpectingRedirect("energyAssistance", "energyAssistance", "false", "medicalExpenses");
    }
    
    postExpectingRedirect("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE",
        "supportAndCare");
  }

  protected void completeFlowFromLandingPageThroughReviewInfo(String... programSelections)
      throws Exception {
    completeFlowFromLandingPageThroughContactInfo(programSelections);
  }

  protected void completeFlowFromLandingPageThroughContactInfo(String... programSelections)
      throws Exception {
    getToPersonalInfoScreen(programSelections);
    fillInPersonalInfoAndContactInfoAndAddress();
  }

  protected void fillInPersonalInfoAndContactInfoAndAddress() throws Exception {
    fillOutPersonalInfo();
    fillOutContactInfo();
    fillOutHomeAddress();
    postExpectingSuccess("verifyHomeAddress", "useEnrichedAddress", "false");
    fillOutMailingAddress();
    
    postExpectingSuccess("verifyMailingAddress", "useEnrichedAddress", "true");
    
    var returnPage = new FormPage(getPage("reviewInfo"));
    assertThat(returnPage.getElementTextById("mailingAddress-address_street")).isEqualTo("smarty street");
    
  }

  protected void completeFlowFromIsPregnantThroughTribalNations(boolean hasHousehold, String... programs)
      throws Exception {
    postExpectingRedirect("pregnant", "isPregnant", "false", "migrantFarmWorker");
    postExpectingRedirect("migrantFarmWorker", "migrantOrSeasonalFarmWorker", "false", "usCitizen");
    
    if (Arrays.stream(programs).allMatch(onlyCCAP -> onlyCCAP.contains("CCAP"))) {
    	postExpectingRedirect("usCitizen", "isUsCitizen", "true", "workSituation");
    }
    else {
    	postExpectingRedirect("usCitizen", "isUsCitizen", "true", "disability");
    	postExpectingRedirect("disability", "hasDisability", "false", "workSituation");
    }
    
    if (hasHousehold) {
      postExpectingRedirect("workSituation", "hasWorkSituation", "false", "tribalNationMember");
      postExpectingRedirect("tribalNationMember", "isTribalNationMember", "false",
          "introIncome");
    } else {
      postExpectingRedirect("workSituation", "hasWorkSituation", "false", "introIncome");
    }
  }

  protected void getToPersonalInfoScreen(String... programSelections) throws Exception {
    postExpectingSuccess("identifyCountyBeforeApplying", "county", List.of("Hennepin"));
    selectPrograms(programSelections);
  }

  protected void fillOutHomeAddress() throws Exception {
    postExpectingSuccess("homeAddress", Map.of(
        "streetAddress", List.of("someStreetAddress"),
        "apartmentNumber", List.of("someApartmentNumber"),
        "city", List.of("someCity"),
        "zipCode", List.of("12345"),
        "state", List.of("MN")
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
        "state", List.of("IL"),
        "sameMailingAddress", List.of()
    ));
  }

 
  protected FormPage nonExpeditedFlowToSuccessPage(boolean hasHousehold, boolean isWorking)
      throws Exception {
    return nonExpeditedFlowToSuccessPage(hasHousehold, isWorking, false, false);
  }

  protected FormPage nonExpeditedFlowToSuccessPage(boolean hasHousehold, boolean isWorking,
      boolean helpWithBenefits,
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
      postExpectingRedirect("addHouseholdMembers", "addHouseholdMembers", "false",
          "addChildrenConfirmation");
      assertNavigationRedirectsToCorrectNextPageWithOption("addChildrenConfirmation","false","introPersonalDetails");
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
    if (hasHousehold) {
      postExpectingRedirect("workSituation", "hasWorkSituation", "false", "tribalNationMember");
    } else {
      postExpectingRedirect("workSituation", "hasWorkSituation", "false", "introIncome");
    }
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
      postExpectingRedirect("employmentStatus", "areYouWorking", "false", "jobSearch");
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
    postExpectingRedirect("unearnedIncome", "unearnedIncome", "SOCIAL_SECURITY",
        "unearnedIncomeSources");
    postExpectingRedirect("unearnedIncomeSources", "socialSecurityAmount", "200",
        "otherUnearnedIncome");
    postExpectingRedirect("otherUnearnedIncome", "otherUnearnedIncome", "NO_OTHER_UNEARNED_INCOME_SELECTED",
        "futureIncome");
    postExpectingRedirect("futureIncome", "earnLessMoneyThisMonth", "true", "startExpenses");
    assertNavigationRedirectsToCorrectNextPage("startExpenses", "homeExpenses");
    postExpectingRedirect("homeExpenses", "homeExpenses", "RENT", "homeExpensesAmount");
    postExpectingRedirect("homeExpensesAmount", "homeExpensesAmount", "123321", "utilities");
    postExpectingRedirect("utilities", "payForUtilities", "HEATING", "energyAssistance");
    postExpectingRedirect("energyAssistance", "energyAssistance", "true",
        "energyAssistanceMoreThan20");
    postExpectingRedirect("energyAssistanceMoreThan20", "energyAssistanceMoreThan20", "true",
        "medicalExpenses");
    postExpectingRedirect("medicalExpenses", "medicalExpenses", "NONE_OF_THE_ABOVE",
        "supportAndCare");
    postExpectingRedirect("supportAndCare", "supportAndCare", "false", "assets");
    postExpectingSuccess("assets", "assets", "REAL_ESTATE");
    assertNavigationRedirectsToCorrectNextPage("assets", "savings");
    
    postExpectingRedirect("savings", "haveSavings", "true", "liquidAssetsSingle");
    postExpectingRedirect("liquidAssetsSingle", "liquidAssets", "1234", "soldAssets");
    postExpectingRedirect("soldAssets", "haveSoldAssets", "false", "submittingApplication");
    assertNavigationRedirectsToCorrectNextPage("submittingApplication", "registerToVote");
    postExpectingRedirect("registerToVote", "registerToVote", "YES", "healthcareCoverage");
    postExpectingRedirect("healthcareCoverage", "healthcareCoverage",
        hasHealthcareCoverage ? "YES" : "NO", "authorizedRep");

    completeHelperWorkflow(helpWithBenefits);
    postExpectingRedirect("additionalInfo",
        "additionalInfo",
        "Some additional information about my application",
        "canWeAsk");
    postWithQueryParam("canWeAsk", "option", "0");
    postExpectingRedirect("raceAndEthnicity", "raceAndEthnicity",
        List.of("ASIAN", "BLACK_OR_AFRICAN_AMERICANS"), "legalStuff");
    postExpectingRedirect("legalStuff",
        Map.of("agreeToTerms", List.of("true"), "drugFelony", List.of("false")),
        "signThisApplication");
    submitApplication();
    return new FormPage(getPage("success"));
  }

  protected void completeHelperWorkflow(boolean helpWithBenefits) throws Exception {
    if (helpWithBenefits) {
      postExpectingRedirect("authorizedRep", "helpWithBenefits", "true", "authorizedRepCommunicate");
      postExpectingRedirect("authorizedRepCommunicate", "communicateOnYourBehalf", "true", "authorizedRepSpeakToCounty");
      postExpectingRedirect("authorizedRepSpeakToCounty", "getMailNotices", "true", "authorizedRepSpendOnYourBehalf");
      postExpectingRedirect("authorizedRepSpendOnYourBehalf", "spendOnYourBehalf", "true", "authorizedRepContactInfo");
      postExpectingRedirect("authorizedRepContactInfo", Map.of(
          "authorizedRepFullName", List.of("My Helpful Friend"),
          "authorizedRepStreetAddress", List.of("helperStreetAddress"),
          "authorizedRepCity", List.of("helperCity"),
          "authorizedRepZipCode", List.of("54321"),
          "authorizedRepPhoneNumber", List.of("7234561111")
      ), "additionalInfo");
    } else {
      postExpectingRedirect("authorizedRep", "helpWithBenefits", "false", "additionalInfo");
    }
  }

  protected void getToDocumentUploadScreen() throws Exception {
    getToDocumentRecommendationScreen();
    clickContinueOnInfoPage("documentRecommendation", "Add documents now", "howToAddDocuments");
  }

  protected void clickContinueOnInfoPage(String pageName, String continueButtonText,
      String expectedNextPageName) throws Exception {
    FormPage page = new FormPage(getPage(pageName));
    page.assertLinkWithTextHasCorrectUrl(continueButtonText,
        "/pages/%s/navigation?option=0".formatted(pageName));
    assertNavigationRedirectsToCorrectNextPage(pageName, expectedNextPageName);
  }

  protected void getToDocumentRecommendationScreen() throws Exception {
    completeFlowFromLandingPageThroughReviewInfo("EA");
    submitApplication();
  }

  protected void completeDocumentUploadFlow() throws Exception {
    var jpgFile = new MockMultipartFile(UPLOADED_JPG_FILE_NAME,
        new FileInputStream(getAbsoluteFilepathString(UPLOADED_JPG_FILE_NAME)));
    mockMvc.perform(multipart("/submit-documents").file(jpgFile).session(session).with(csrf()))
        .andExpect(redirectedUrl("/pages/nextSteps"));
  }

  protected void deleteOnlyHouseholdMember() throws Exception {
    getWithQueryParam("householdDeleteWarningPage", "iterationIndex", "0");
    mockMvc.perform(post("/groups/household/0/delete").with(csrf()).session(session))
        .andExpect(redirectedUrl("/pages/addHouseholdMembers"));
  }

  protected FormPage getFormPage(String pageName) throws Exception {
    return new FormPage(getPageExpectingSuccess(pageName));
  }
  
  @NotNull
  private String followRedirectsForPageNameWithOption(String currentPageName, String option) throws Exception {
    var nextPage = "/pages/" + currentPageName + "/navigation?option="+option;
    while (Objects.requireNonNull(nextPage).contains("/navigation")) {
      // follow redirects
      nextPage = mockMvc.perform(get(nextPage).session(session))
          .andExpect(status().is3xxRedirection()).andReturn()
          .getResponse()
          .getRedirectedUrl();
    }
    return nextPage;
  }
}
