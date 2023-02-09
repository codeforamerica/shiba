package org.codeforamerica.shiba.journeys;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.testutilities.AbstractBasePageTest;
import org.codeforamerica.shiba.testutilities.SuccessPage;
import org.codeforamerica.shiba.testutilities.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

abstract class JourneyTest extends AbstractBasePageTest {

  protected PDAcroForm caf;
  protected PDAcroForm ccap;
  protected PDAcroForm certainPops;
  protected String applicationId;

  @MockBean
  protected Clock clock;
  @MockBean
  protected SmartyStreetClient smartyStreetClient;
  @SpyBean
  protected DocumentRepository documentRepository;
  @MockBean
  private ClientRegistrationRepository springSecurityFilterChain;
  @MockBean
  protected PageEventPublisher pageEventPublisher;
  @MockBean
  protected MailGunEmailClient mailGunEmailClient;
  @MockBean
  protected FeatureFlagConfiguration featureFlagConfiguration;
  @SpyBean
  protected UploadDocumentConfiguration uploadDocumentConfiguration;

  @Override
  @BeforeEach
  protected void setUp() throws IOException {
    super.setUp();
    driver.navigate().to(baseUrl);
    when(clock.instant()).thenReturn(Instant.now());
    when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    when(smartyStreetClient.validateAddress(any())).thenReturn(Optional.empty());

    when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
    when(featureFlagConfiguration.get("clamav")).thenReturn(FeatureFlag.ON);
    caf = null;
    ccap = null;
    certainPops = null;
  }

  @AfterEach
  void tearDown() {
    if (applicationId != null) {
      Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
          .filter(file -> file.getName().contains(applicationId))
          .forEach(File::delete);
    }
  }

  protected void assertCafFieldEquals(String fieldName, String expectedVal) {
    TestUtils.assertPdfFieldEquals(fieldName, expectedVal, caf);
  }

  protected void assertCcapFieldEquals(String fieldName, String expectedVal) {
    TestUtils.assertPdfFieldEquals(fieldName, expectedVal, ccap);
  }
  
  protected void assertCertainPopsFieldEquals(String fieldName, String expectedVal) {
	    TestUtils.assertPdfFieldEquals(fieldName, expectedVal, certainPops);
  }

  protected String signApplicationAndDownloadApplicationZipFiles(String signature) {
    testPage.enter("applicantSignature", signature);
    testPage.clickButton("Submit");
    testPage.clickContinue();
    testPage.clickContinue();

    // No document upload
    testPage.clickButton("I'll do this later");
    testPage.clickButton("Finish application");

    // Next steps screen
    testPage.clickContinue();
    return downloadPdfs();
  }

  protected String downloadPdfs() {
    // Download CAF
    SuccessPage successPage = new SuccessPage(driver);
    successPage.downloadPdfZipFile();
    await().until(zipDownloadCompletes(successPage));
    unzipFiles();
     var pdfs = getAllFiles(); 
     caf = pdfs.getOrDefault(CAF, null); 
     ccap = pdfs.getOrDefault(CCAP,null);
     certainPops = pdfs.getOrDefault(CERTAIN_POPS,null);
     
    return getApplicationId();
  }

  private String getApplicationId() {
    // Retrieves the application id from the filename of a downloaded PDF
    return Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
        .filter(file -> file.getName().endsWith(".pdf"))
        .sorted((f1,f2)-> Long.compare(f2.lastModified(), f1.lastModified()))
        .map(File::getName)
        .findFirst()
        .orElseThrow()
        .split("_")[4];
  }

  @NotNull
  protected Callable<Boolean> zipDownloadCompletes(SuccessPage successPage) {
       return () -> getZipFile().size() == successPage.countDownloadLinks();
  }
  
  protected void goBackToPage(String pageName) {
	    while(!testPage.getTitle().equalsIgnoreCase(pageName)){
	    	 testPage.goBack();
	    }
  }

  protected void goToPageBeforeSelectPrograms(String county) {
	    // Landing page
	    testPage.clickButton("Apply now");

	    // Select county
	    testPage.enter("county", county);
	    testPage.clickContinue();

	    // Informational pages
	    testPage.clickContinue();
	    testPage.clickContinue();

	    // Language Preferences
	    testPage.enter("writtenLanguage", "English");
	    testPage.enter("spokenLanguage", "English");
	    testPage.enter("needInterpreter", "Yes");
	    testPage.clickContinue();
  }
  
  protected void getToHomeAddress(String county, List<String> programSelections) {
    // Landing page
    testPage.clickButton("Apply now");

    // Select county
    testPage.enter("county", county);
    testPage.clickContinue();

    // Informational pages
    testPage.clickContinue();
    testPage.clickContinue();

    // Language Preferences
    testPage.enter("writtenLanguage", "English");
    testPage.enter("spokenLanguage", "English");
    testPage.enter("needInterpreter", "Yes");
    testPage.clickContinue();

    // Program Selection
    programSelections.forEach(program -> testPage.enter("programs", program));
    testPage.clickContinue();

    if (programSelections.contains(PROGRAM_CERTAIN_POPS)) {
      // Test Certain pops offboarding flow first by selecting None of the above
      testPage.enter("basicCriteria", "None of the above");
      testPage.clickContinue();
      assertThat(testPage.getTitle()).isEqualTo("Certain Pops Offboarding");
      testPage.clickContinue();
      assertThat(testPage.getTitle()).isEqualTo("Add other programs");
      testPage.goBack();
      testPage.goBack();

      // Basic Criteria:
      testPage.enter("basicCriteria", "I am 65 years old or older");
      testPage.enter("basicCriteria", "I am blind");
      testPage.enter("basicCriteria", "I currently receive SSI or RSDI for a disability");
      testPage.enter("basicCriteria",
          "I have a disability that has been certified by the Social Security Administration (SSA)");
      testPage.enter("basicCriteria",
          "I have a disability that has been certified by the State Medical Review Team (SMRT)");
      testPage.enter("basicCriteria",
          "I want to apply for Medical Assistance for Employed Persons with Disabilities (MA-EPD)");
      testPage.enter("basicCriteria", "I have Medicare and need help with my costs");
      testPage.clickContinue();
      assertThat(testPage.getTitle()).isEqualTo("Certain Pops Confirmation");
      testPage.clickContinue();
    }
    if(programSelections.contains(PROGRAM_SNAP)) {
      assertThat(testPage.getTitle()).isEqualTo("Expedited Notice");
      testPage.clickContinue();
    }
    // Getting to know you (Personal Info intro page)
    testPage.clickContinue();

    // Personal Info
    testPage.enter("firstName", "Ahmed");
    testPage.enter("lastName", "St. George");
    testPage.enter("otherName", "defaultOtherName");
    //DOB is optional
    testPage.enter("ssn", "123456789");
    if (programSelections.contains(PROGRAM_CERTAIN_POPS)) {
      testPage.enter("noSSNCheck", "I don't have a social security number.");
      assertThat(testPage.getCheckboxValues("noSSNCheck")).contains("I don't have a social security number.", "I don't have a social security number.");
      testPage.enter("appliedForSSN", "Yes");
      testPage.clickContinue();
      //SSN textbox is filled and Checkbox is checked, so page won't advance and error shows 
      assertThat(testPage.getTitle()).contains("Personal Info");
      testPage.enter("noSSNCheck", "I don't have a social security number.");//deselect the SSN checkbox
    }
    testPage.enter("maritalStatus", "Never married");
    testPage.enter("sex", "Female");
    testPage.enter("livedInMnWholeLife", "Yes");
    testPage.enter("moveToMnDate", "10/20/1993");
    testPage.enter("moveToMnPreviousCity", "Chicago");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).isEqualTo("Home Address");
    testPage.goBack();
    testPage.enter("dateOfBirth", "01/12/1928");
    testPage.clickContinue();
  }
  
  protected void fillOutHomeAndMailingAddress(String homeZip, String homeCity,
      String homeStreetAddress, String homeApartmentNumber) {
    testPage.enter("zipCode", homeZip);
    testPage.enter("city", homeCity);
    testPage.enter("streetAddress", homeStreetAddress);
    testPage.enter("apartmentNumber", homeApartmentNumber);
    testPage.clickContinue();

    // Where can the county send your mail? (accept the smarty streets enriched address)
    testPage.enter("zipCode", "23456");
    testPage.enter("city", "someCity");
    testPage.enter("streetAddress", "someStreetAddress");
    testPage.enter("state", "IL");
    testPage.enter("apartmentNumber", "someApartmentNumber");
    when(smartyStreetClient.validateAddress(any())).thenReturn(
        Optional.of(new Address("smarty street", "Cooltown", "CA", "03104", "1b", "someCounty"))
    );
    testPage.clickContinue();
    testPage.clickElementById("enriched-address");
    testPage.clickContinue();
  }
  
  
  protected void fillOutContactAndReview(boolean isReview) {   
    // Check that we get the no phone number confirmation screen if no phone number is entered
    testPage.enter("email", "some@example.com");
    testPage.clickContinue();
    assertThat(testPage.getTitle()).contains("No phone number confirmation");
    testPage.goBack();

    // How can we get in touch with you?
    testPage.enter("phoneNumber", "7234567890");
    testPage.enter("email", "some@example.com");
    assertThat(testPage.getCheckboxValues("phoneOrEmail")).contains("It's okay to text me",
        "It's okay to email me");
    testPage.clickContinue();
    
    if (isReview)
    {
      // Let's review your info
      assertThat(driver.findElement(By.id("mailingAddress-address_street")).getText())
          .isEqualTo("smarty street");           
    }
    
  }
  

  protected void assertApplicationSubmittedEventWasPublished(String applicationId,
      FlowType flowType, int expectedNumberOfEvents) {
    ArgumentCaptor<PageEvent> captor = ArgumentCaptor.forClass(PageEvent.class);
    verify(pageEventPublisher, times(expectedNumberOfEvents)).publish(captor.capture());
    List<PageEvent> allValues = captor.getAllValues();
    ApplicationSubmittedEvent applicationSubmittedEvent = (ApplicationSubmittedEvent) allValues.stream()
        .filter(event -> event instanceof ApplicationSubmittedEvent).findFirst().get();
    assertThat(applicationSubmittedEvent.getFlow()).isEqualTo(flowType);
    assertThat(applicationSubmittedEvent.getApplicationId()).isEqualTo(applicationId);
    assertThat(applicationSubmittedEvent.getLocale()).isEqualTo(ENGLISH);
  }

  protected void deleteAFile() {
    await().until(
		  () -> driver.findElements(By.className("dz-remove")).get(0).getAttribute("innerHTML")
		  .contains("delete"));
    testPage.clickLink("delete");

    assertThat(testPage.getTitle()).isEqualTo("Delete a file");
    testPage.clickButton("Yes, delete the file");
  }

  protected void waitForErrorMessage() {
    WebElement errorMessage = driver.findElement(By.className("text--error"));
    await().until(() -> !errorMessage.getText().isEmpty());
  }
}
