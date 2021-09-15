package org.codeforamerica.shiba.journeys;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
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
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.testutilities.AbstractBasePageTest;
import org.codeforamerica.shiba.testutilities.SuccessPage;
import org.codeforamerica.shiba.testutilities.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

abstract class JourneyTest extends AbstractBasePageTest {

  protected PDAcroForm caf;
  protected PDAcroForm ccap;
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

    when(featureFlagConfiguration.get("apply-for-mille-lacs")).thenReturn(FeatureFlag.ON);
    when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
    when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
    caf = null;
    ccap = null;
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

  protected String signApplicationAndDownloadPdfs(String signature,
      boolean shouldHaveCafDownloadLink,
      boolean shouldHaveCcapDownloadLink) {
    testPage.enter("applicantSignature", signature);
    testPage.clickButton("Submit");
    testPage.clickContinue();
    testPage.clickContinue();

    // No document upload
    testPage.clickButton("I'll do this later");
    testPage.clickButton("Finish application");

    // Next steps screen
    testPage.clickContinue();
    return downloadPdfs(shouldHaveCafDownloadLink, shouldHaveCcapDownloadLink);
  }

  protected String downloadPdfs(boolean shouldHaveCafDownloadLink,
      boolean shouldHaveCcapDownloadLink) {
    // Download CAF
    SuccessPage successPage = new SuccessPage(driver);
    assertThat(successPage.CAFdownloadPresent()).isEqualTo(shouldHaveCafDownloadLink);
    assertThat(successPage.CCAPdownloadPresent()).isEqualTo(shouldHaveCcapDownloadLink);
    successPage.downloadPdfs();
    await().until(pdfDownloadCompletes(successPage));
    var pdfs = getAllFiles();
    caf = pdfs.getOrDefault(CAF, null);
    ccap = pdfs.getOrDefault(CCAP, null);
    return getApplicationId();
  }

  private String getApplicationId() {
    // Retrieves the application id from the filename of a downloaded PDF
    return Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
        .map(File::getName)
        .findFirst()
        .orElseThrow()
        .split("_")[4];
  }

  @NotNull
  private Callable<Boolean> pdfDownloadCompletes(SuccessPage successPage) {
    return () -> getAllFiles().size() == successPage.pdfDownloadLinks();
  }

  protected void getToHomeAddress(List<String> programSelections) {
    // Landing pages
    testPage.clickButton("Apply now");
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
    testPage.clickContinue();

    // Personal Info
    testPage.enter("firstName", "Ahmed");
    testPage.enter("lastName", "St. George");
    testPage.enter("otherName", "defaultOtherName");
    testPage.enter("dateOfBirth", "01/12/1928");
    testPage.enter("ssn", "123456789");
    testPage.enter("maritalStatus", "Never married");
    testPage.enter("sex", "Female");
    testPage.enter("livedInMnWholeLife", "Yes");
    testPage.enter("moveToMnDate", "10/20/1993");
    testPage.enter("moveToMnPreviousCity", "Chicago");
    testPage.clickContinue();

    // How can we get in touch with you?
    testPage.enter("phoneNumber", "7234567890");
    testPage.enter("email", "some@example.com");
    testPage.enter("phoneOrEmail", "Text me");
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

    // Let's review your info
    assertThat(driver.findElementById("mailingAddress-address_street").getText())
        .isEqualTo("smarty street");
  }

  protected void assertApplicationSubmittedEventWasPublished(String applicationId,
      FlowType flowType,
      int expectedNumberOfEvents) {
    ArgumentCaptor<ApplicationSubmittedEvent> captor = ArgumentCaptor
        .forClass(ApplicationSubmittedEvent.class);
    verify(pageEventPublisher, times(expectedNumberOfEvents)).publish(captor.capture());
    List<ApplicationSubmittedEvent> allValues = captor.getAllValues();
    ApplicationSubmittedEvent applicationSubmittedEvent = allValues.get(allValues.size() - 1);
    assertThat(applicationSubmittedEvent.getFlow()).isEqualTo(flowType);
    assertThat(applicationSubmittedEvent.getApplicationId()).isEqualTo(applicationId);
    assertThat(applicationSubmittedEvent.getLocale()).isEqualTo(ENGLISH);
  }

  protected void deleteAFile() {
    testPage.clickLink("delete");

    assertThat(testPage.getTitle()).isEqualTo("Delete a file");
    testPage.clickButton("Yes, delete the file");
  }

  protected void waitForErrorMessage() {
    WebElement errorMessage = driver.findElementByClassName("text--error");
    await().until(() -> !errorMessage.getText().isEmpty());
  }
}
