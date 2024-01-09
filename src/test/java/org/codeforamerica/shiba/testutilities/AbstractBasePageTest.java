package org.codeforamerica.shiba.testutilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.NO;
import static org.codeforamerica.shiba.testutilities.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.DocumentRepositoryTestConfig;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({WebDriverConfiguration.class, DocumentRepositoryTestConfig.class})
@ActiveProfiles("test")
public abstract class AbstractBasePageTest {

  protected static final String PROGRAM_SNAP = "Food (SNAP)";
  protected static final String PROGRAM_CASH = "Cash programs";
  protected static final String PROGRAM_GRH = "Housing Support (GRH)";
  protected static final String PROGRAM_CCAP = "Child Care Assistance";
  protected static final String PROGRAM_EA = "Emergency Assistance";
  protected static final String PROGRAM_CERTAIN_POPS = "Healthcare for Seniors and People with Disabilities";
  protected static final String PROGRAM_NONE = "None of the above";
  private static final String UPLOADED_JPG_FILE_NAME = "shiba+file.jpg";
  private static final String UPLOADED_PDF_NAME = "test-caf.pdf";
  private static final String XFA_PDF_NAME = "xfa-invoice-example.pdf";
  private static final String PASSWORD_PROTECTED_PDF = "password-protected.pdf";
  private static final String UPLOAD_RIFF_WITH_RENAMED_JPG_EXTENSION = "RiffSavedAsJPGTestDoc.jpg";
  @Autowired
  protected RemoteWebDriver driver;

  @Autowired
  protected Path path;

  protected String baseUrl;

  @LocalServerPort
  protected String localServerPort;

  protected Page testPage;

  @BeforeEach
  protected void setUp() throws IOException {
    baseUrl = "http://localhost:%s".formatted(localServerPort);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
    driver.navigate().to(baseUrl);
    initTestPage();
  }

  protected void initTestPage() {
    testPage = new Page(driver);
  }

  protected void navigateTo(String pageName) {
    driver.navigate().to(baseUrl + "/pages/" + pageName);
  }

  protected Map<Document, PDAcroForm> getAllFiles() {
    return Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
        .filter(file -> file.getName().endsWith(".pdf")).sorted((f1,f2)-> Long.compare(f2.lastModified(), f1.lastModified()))
        .collect(Collectors.toMap(this::getDocumentType, pdfFile -> {
          try {
            return Loader.loadPDF(pdfFile).getDocumentCatalog().getAcroForm();
          } catch (IOException e) {
            throw new IllegalStateException(e);
          }
        }, (r1, r2) -> r1));
  }

  protected List<File> getZipFile() {
    return Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
        .filter(file -> file.getName().endsWith(".zip")).toList();
  }

  protected void unzipFiles() {
    List<File> filesList = Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
        .filter(file -> file.getName().endsWith(".zip")).collect(Collectors.toCollection(() -> new ArrayList<File>()));
    unzip(filesList);
   
  }

  
  protected List<File> unzip(List<File> filesList) {
    List<File> fileList = new ArrayList<File>();
    for(File file: filesList) {
    try {
      FileInputStream inputStream = new FileInputStream(file);
      ZipInputStream zipStream = new ZipInputStream(inputStream);
      ZipEntry zEntry;
      String destination = path.toFile().getPath();
      while ((zEntry = zipStream.getNextEntry()) != null) {
        if(zEntry.getName().contains("_CAF") || zEntry.getName().contains("_CCAP") || zEntry.getName().contains("_CERTAIN_POPS") ) {
          if (!zEntry.isDirectory()) {
            File files = new File(destination, zEntry.getName());
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
      }
      zipStream.close();//This will delete zip folder after extraction
    } catch (Exception e) {
      System.out.println("Unzipping failed");
      e.printStackTrace();
    }
    }
    return fileList;
  }


  private Document getDocumentType(File file) {
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

  protected void waitForDocumentUploadToComplete() {
    await().atMost(15, TimeUnit.SECONDS)
    .until(() -> driver.findElements(By.linkText("cancel")).isEmpty());
  }

  /**
   * Creates an image of the browser page.
   * It should be a PNG file, example: "webPage.png".
   * If no path is used, the file will be located in the project root.
   * (delete the file afterwards so it isn't committed to GitHub)
   * @param fileWithPath
   */
  @SuppressWarnings("unused")
  public void takeSnapShot(String fileWithPath) {
    TakesScreenshot screenshot = driver;
    Path sourceFile = screenshot.getScreenshotAs(OutputType.FILE).toPath();
    Path destinationFile = new File(fileWithPath).toPath();
    try {
      Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void fillOutPersonalInfo() {
    navigateTo("personalInfo");
    fillOutPersonInfo();
    testPage.enter("moveToMnPreviousCity", "Chicago");
  }

  protected void fillOutPersonInfo() {
    testPage.enter("firstName", "defaultFirstName");
    testPage.enter("lastName", "defaultLastName");
    testPage.enter("otherName", "defaultOtherName");
    testPage.enter("dateOfBirth", "01/12/1928");
    testPage.enter("ssn", "123456789");
    testPage.enter("maritalStatus", "Never married");
    testPage.enter("sex", "Female");
    testPage.enter("livedInMnWholeLife", "Yes");
    testPage.enter("moveToMnDate", "02/18/1776");
  }
  
  protected void fillOutMatchInfo() {
    testPage.enter("firstName", "defaultFirstName");
    testPage.enter("lastName", "defaultLastName");
    testPage.enter("dateOfBirth", "01/12/1928");
    testPage.enter("ssn", "123456789");
    testPage.enter("phoneNumber", "234-567-8900");
    testPage.enter("email", "default@mailnator.com");
    testPage.enter("caseNumber", "1234567");
  }

  protected void getToPersonalInfoScreen(List<String> programSelections) {
    testPage.clickButton("Apply now");

    testPage.enter("county", "Hennepin");
    testPage.clickContinue();

    testPage.clickContinue();
    testPage.clickContinue();
    testPage.enter("writtenLanguage", "English");
    testPage.enter("spokenLanguage", "English");
    testPage.enter("needInterpreter", "Yes");
    testPage.clickContinue();
    programSelections.forEach(program -> testPage.enter("programs", program));
    testPage.clickContinue();
    testPage.clickContinue();
  }

  protected void completeFlowFromLandingPageThroughContactInfo(List<String> programSelections) {
    getToPersonalInfoScreen(programSelections);

    fillOutPersonalInfo();

    testPage.clickContinue();
  }

  protected void completeFlowFromLandingPageThroughReviewInfo(List<String> programSelections,
      SmartyStreetClient mockSmartyStreetClient) {
    completeFlowFromLandingPageThroughContactInfo(programSelections);

    testPage.enter("phoneNumber", "7234567890");
    testPage.enter("email", "some@example.com");
    testPage.enter("phoneOrEmail", "It's okay to text me");
    testPage.clickContinue();
    fillOutAddress();
    testPage.enter("sameMailingAddress", "No, use a different address for mail");
    testPage.clickContinue();

    testPage.clickButton("Use this address");
    testPage.enter("zipCode", "12345");
    testPage.enter("city", "someCity");
    testPage.enter("streetAddress", "someStreetAddress");
    testPage.enter("state", "IL");
    testPage.enter("apartmentNumber", "someApartmentNumber");
    when(mockSmartyStreetClient.validateAddress(any())).thenReturn(
        Optional.of(new Address("smarty street", "City", "CA", "03104", "", "someCounty"))
    );
    testPage.clickContinue();

    testPage.clickElementById("enriched-address");
    testPage.clickContinue();
    assertThat(driver.findElement(By.id("mailingAddress-address_street")).getText())
        .isEqualTo("smarty street");
  }

  protected void fillOutAddress() {
    testPage.enter("zipCode", "12345");
    testPage.enter("city", "someCity");
    testPage.enter("streetAddress", "someStreetAddress");
    testPage.enter("apartmentNumber", "someApartmentNumber");
    testPage.enter("isHomeless", "I don't have a permanent address");
  }

  protected SuccessPage nonExpeditedFlowToSuccessPage(boolean hasHousehold, boolean isWorking,
      SmartyStreetClient mockSmartyStreetClient,
      boolean helpWithBenefits, boolean hasHealthcareCoverage) {
    completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP, PROGRAM_CASH),
        mockSmartyStreetClient);
    testPage.clickLink("This looks correct");

    if (hasHousehold) {
      testPage.enter("addHouseholdMembers", YES.getDisplayValue());
      testPage.clickContinue();
      fillOutHousemateInfo(PROGRAM_CCAP);
      testPage.clickContinue();
      testPage.clickButton("Yes, that's everyone");
      testPage.enter("whoNeedsChildCare", "householdMemberFirstName householdMemberLastName");
      testPage.clickContinue();
      testPage.enter("whoHasAParentNotLivingAtHome",
          "None of the children have parents living outside the home");
      testPage.clickContinue();
      testPage.enter("livingSituation", "None of these");
      testPage.clickContinue();
      testPage.enter("goingToSchool", NO.getDisplayValue());
      testPage.enter("isPregnant", YES.getDisplayValue());
      testPage.enter("whoIsPregnant", "Me");
      testPage.clickContinue();
    } else {
      testPage.enter("addHouseholdMembers", NO.getDisplayValue());
      testPage.clickContinue();
      testPage.enter("livingSituation", "None of these");
      testPage.clickContinue();
      testPage.enter("goingToSchool", NO.getDisplayValue());
      testPage.enter("isPregnant", NO.getDisplayValue());
    }

    testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
    if (hasHousehold) {
      testPage.enter("isUsCitizen", NO.getDisplayValue());
      testPage.enter("whoIsNonCitizen", "Me");
      testPage.clickContinue();
    } else {
      testPage.enter("isUsCitizen", YES.getDisplayValue());
    }
    testPage.enter("hasDisability", NO.getDisplayValue());
    testPage.enter("hasWorkSituation", NO.getDisplayValue());
    if (hasHousehold) {
      testPage.enter("isTribalNationMember", NO.getDisplayValue());
    }
    testPage.clickContinue();

    if (isWorking) {
      testPage.enter("areYouWorking", YES.getDisplayValue());
      testPage.clickButton("Add a job");

      if (hasHousehold) {
        testPage.enter("whoseJobIsIt", "householdMemberFirstName householdMemberLastName");
        testPage.clickContinue();
      }

      testPage.enter("employersName", "some employer");
      testPage.clickContinue();
      testPage.enter("selfEmployment", YES.getDisplayValue());
      paidByTheHourOrSelectPayPeriod(true);
      testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
    } else {
      testPage.enter("areYouWorking", NO.getDisplayValue());
      testPage.enter("currentlyLookingForJob", YES.getDisplayValue());

      if (hasHousehold) {
        testPage.enter("whoIsLookingForAJob", "householdMemberFirstName householdMemberLastName");
        testPage.clickContinue();
      }
    }

    testPage.clickContinue();
    testPage.enter("unearnedIncome", "Social Security");
    testPage.clickContinue();
    testPage.enter("socialSecurityAmount", "200");
    testPage.clickContinue();
    testPage.enter("otherUnearnedIncome", "Money from a Trust");
    testPage.clickContinue();
    testPage.enter("trustMoneyAmount", "200");
    testPage.clickContinue();
    testPage.enter("earnLessMoneyThisMonth", "Yes");
    testPage.clickContinue();
    testPage.clickContinue();
    testPage.enter("homeExpenses", "Rent");
    testPage.clickContinue();
    testPage.enter("homeExpensesAmount", "123321");
    testPage.clickContinue();
    testPage.enter("payForUtilities", "Heating");
    testPage.clickContinue();
    testPage.enter("energyAssistance", YES.getDisplayValue());
    testPage.enter("energyAssistanceMoreThan20", YES.getDisplayValue());
    testPage.enter("medicalExpenses", "None of the above");
    testPage.clickContinue();
    testPage.enter("supportAndCare", YES.getDisplayValue());
    testPage.enter("assets", "A vehicle");
    testPage.enter("assets", "Real estate (not including your own home)");
    testPage.clickContinue();
    testPage.enter("haveSavings", YES.getDisplayValue());
    testPage.enter("liquidAssets", "1234");
    testPage.clickContinue();
    testPage.enter("haveSoldAssets", NO.getDisplayValue());
    testPage.clickContinue();
    testPage.enter("registerToVote", "Yes, send me more info");
    testPage.enter("healthcareCoverage",
        hasHealthcareCoverage ? YES.getDisplayValue() : NO.getDisplayValue());
    testPage.clickContinue();
    completeHelperWorkflow(helpWithBenefits);
    driver.findElement(By.id("additionalInfo"))
        .sendKeys("Some additional information about my application");
    testPage.clickContinue();
    testPage.enter("agreeToTerms", "I agree");
    testPage.enter("drugFelony", NO.getDisplayValue());
    testPage.clickContinue();
    testPage.enter("applicantSignature", "some name");
    testPage.clickButton("Submit");

    skipDocumentUploadFlow();

    return new SuccessPage(driver);
  }

  protected void skipDocumentUploadFlow() {
    testPage.clickButton("I'll do this later");
  }

  protected void fillOutHousemateInfo(String programSelection) {
    testPage.enter("relationship", "housemate");
    testPage.enter("programs", programSelection);
    testPage.enter("firstName", "householdMemberFirstName");
    testPage.enter("lastName", "householdMemberLastName");
    testPage.enter("otherName", "houseHoldyMcMemberson");
    testPage.enter("dateOfBirth", "09/14/1950");
    testPage.enter("ssn", "987654321");
    testPage.enter("maritalStatus", "Never married");
    testPage.enter("sex", "Male");
    testPage.enter("livedInMnWholeLife", "Yes");
    testPage.enter("moveToMnDate", "02/18/1950");
    testPage.enter("moveToMnPreviousState", "Illinois");
  }

  protected void paidByTheHourOrSelectPayPeriod(boolean paidByTheHour) {
    if (paidByTheHour) {
      testPage.enter("paidByTheHour", YES.getDisplayValue());
      testPage.enter("hourlyWage", "1");
      testPage.clickContinue();
      testPage.enter("hoursAWeek", "30");
    } else {
      testPage.enter("paidByTheHour", NO.getDisplayValue());
      testPage.enter("payPeriod", "Twice a month");
      testPage.clickContinue();
      testPage.enter("incomePerPayPeriod", "1");
    }
    testPage.clickContinue();
    testPage.goBack();
    testPage.clickButton("No, I'd rather keep going");
    testPage.clickButton("No, that's it.");
  }

  protected void fillOutHelperInfo() {
    testPage.enter("helpersFullName", "defaultFirstName defaultLastName");
    testPage.enter("helpersStreetAddress", "someStreetAddress");
    testPage.enter("helpersCity", "someCity");
    testPage.enter("helpersZipCode", "12345");
    testPage.enter("helpersPhoneNumber", "7234567890");
    testPage.clickContinue();
  }

  private void completeHelperWorkflow(boolean helpWithBenefits) {
    if (helpWithBenefits) {
      testPage.enter("helpWithBenefits", YES.getDisplayValue());
      testPage.enter("communicateOnYourBehalf", YES.getDisplayValue());
      testPage.enter("getMailNotices", YES.getDisplayValue());
      testPage.enter("spendOnYourBehalf", YES.getDisplayValue());
      fillOutHelperInfo();
    } else {
      testPage.enter("helpWithBenefits", NO.getDisplayValue());
    }
  }

  protected void uploadFile(String filepath) {
    testPage.clickElementById("drag-and-drop-box"); // is this needed?
    WebElement upload = driver.findElement(By.className("dz-hidden-input"));
    upload.sendKeys(filepath);
    await().until(
        () -> !driver.findElements(By.className("file-details")).get(0).getAttribute("innerHTML")
            .isBlank());
  }

  protected void uploadJpgFile() {
    uploadFile(TestUtils.getAbsoluteFilepathString(UPLOADED_JPG_FILE_NAME));
    assertThat(driver.findElement(By.id("document-upload")).getText())
        .contains(UPLOADED_JPG_FILE_NAME);
  }

  protected void uploadInvalidJpg(){
    uploadFile(TestUtils.getAbsoluteFilepathString(UPLOAD_RIFF_WITH_RENAMED_JPG_EXTENSION));
    assertThat(driver.findElement(By.id("document-upload")).getText())
        .contains(UPLOAD_RIFF_WITH_RENAMED_JPG_EXTENSION);
  }

  protected void uploadButtonDisabledCheck() {
    testPage.clickElementById("drag-and-drop-box"); // is this needed?
    WebElement upload = driver.findElement(By.className("dz-hidden-input"));
    upload.sendKeys(TestUtils.getAbsoluteFilepathString(UPLOADED_JPG_FILE_NAME));
    assertThat(driver.findElement(By.id("submit-my-documents")).getAttribute("class"))
    .contains("disabled");
    await().until(
        () -> !driver.findElements(By.className("file-details")).get(0).getAttribute("innerHTML")
            .isBlank());
    assertThat(driver.findElement(By.id("document-upload")).getText())
        .contains(UPLOADED_JPG_FILE_NAME);
  }

  protected void uploadXfaFormatPdf() {
    uploadFile(TestUtils.getAbsoluteFilepathString(XFA_PDF_NAME));
    assertThat(driver.findElement(By.id("document-upload")).getText()).contains(XFA_PDF_NAME);
  }

  protected void uploadPasswordProtectedPdf() {
    uploadFile(TestUtils.getAbsoluteFilepathString(PASSWORD_PROTECTED_PDF));
    assertThat(driver.findElement(By.id("document-upload")).getText())
        .contains(PASSWORD_PROTECTED_PDF);
  }

  protected void uploadPdfFile() {
    uploadFile(TestUtils.getAbsoluteFilepathString(UPLOADED_PDF_NAME));
    assertThat(driver.findElement(By.id("document-upload")).getText()).contains(UPLOADED_PDF_NAME);
  }
  
  protected void uploadVirusFile(String testFileName) {
    uploadFile(TestUtils.getAbsoluteFilepathString(testFileName));
    assertThat(driver.findElement(By.id("document-upload")).getText()).contains(testFileName);
  }

  protected void getToDocumentUploadScreen() {
    testPage.clickButton("Apply now");
    testPage.enter("county", "Hennepin");
    testPage.clickContinue();
    testPage.clickContinue();
    testPage.clickContinue();
    testPage.enter("writtenLanguage", "English");
    testPage.enter("spokenLanguage", "English");
    testPage.enter("needInterpreter", "Yes");
    testPage.clickContinue();
    testPage.enter("programs", PROGRAM_EA);
    testPage.clickContinue();
    testPage.clickContinue();
    fillOutPersonalInfo();
    testPage.clickContinue();
    navigateTo("signThisApplication");
    testPage.enter("applicantSignature", "some name");
    testPage.clickButton("Continue");
    testPage.clickButton("Submit application");
    testPage.clickContinue();
    testPage.clickContinue();
    testPage.clickButton("Add documents now");
    testPage.clickContinue();
  }

  protected void getToLaterDocsUploadScreen() {
    testPage.clickButton("Upload documents");
    
    testPage.enter("county", "Hennepin");
    testPage.clickContinue();
    
    fillOutMatchInfo();
    testPage.clickContinue();
    
    testPage.clickContinue();
  }
  
  protected void getToHealthcareRenewalUploadScreen() {
	navigateTo("healthcareRenewalUpload");
	testPage.enter("county", "Hennepin");
    testPage.clickContinue();
    
    fillOutMatchInfo();
    testPage.clickContinue();
    
    testPage.clickContinue();
  }

  protected String getAttributeForElementAtIndex(List<WebElement> elementList, int index,
      String attributeName) {
    return elementList.get(index).getAttribute(attributeName);
  }

  @NotNull
  protected Callable<Boolean> uploadCompletes() {
    return () -> !getAttributeForElementAtIndex(driver.findElements(By.className("dz-remove")),
        0,
        "innerHTML").isBlank();
  }
}
