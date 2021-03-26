package org.codeforamerica.shiba;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.Page;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.enrichment.Address;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.pages.YesNoAnswer.NO;
import static org.codeforamerica.shiba.pages.YesNoAnswer.YES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public abstract class AbstractBasePageTest {
    private static final String UPLOADED_FILE_NAME = "shiba.jpg";
    public static final String PROGRAM_SNAP = "Food (SNAP)";
    public static final String PROGRAM_CASH = "Cash programs";
    public static final String PROGRAM_GRH = "Housing Support (GRH)";
    public static final String PROGRAM_CCAP = "Child Care Assistance";
    public static final String PROGRAM_EA = "Emergency Assistance";
    static protected RemoteWebDriver driver;

    protected Path path;
    protected String baseUrl;
    protected String baseUrlWithAuth;
    @Value("${shiba-username}:${shiba-password}")
    protected String authParams;

    @LocalServerPort
    protected String localServerPort;

    protected Page testPage;

    @BeforeAll
    static void beforeAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    protected void setUp() throws IOException {
        baseUrl = String.format("http://localhost:%s", localServerPort);
        baseUrlWithAuth = String.format("http://%s@localhost:%s", authParams, localServerPort);
        ChromeOptions options = new ChromeOptions();
        path = Files.createTempDirectory("");
        HashMap<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory", path.toString());
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("--window-size=1280,800");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        testPage = new Page(driver);
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    protected void navigateTo(String pageName) {
        driver.navigate().to(baseUrl + "/pages/" + pageName);
    }

    protected String getPdfFieldText(PDAcroForm pdAcroForm, String fieldName) {
        return pdAcroForm.getField(fieldName).getValueAsString();
    }

    protected Map<Document, PDAcroForm> getAllFiles() {
        return Arrays.stream(path.toFile().listFiles())
                .filter(file -> file.getName().endsWith(".pdf"))
                .collect(Collectors.toMap(this::getDocumentType, pdfFile -> {
                    try {
                        return PDDocument.load(pdfFile).getDocumentCatalog().getAcroForm();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }));
    }

    private Document getDocumentType(File file) {
        String fileName = file.getName();
        if (fileName.contains("_CAF")) {
            return Document.CAF;
        } else if (fileName.contains("_CCAP")) {
            return Document.CCAP;
        } else {
            return Document.CAF;
        }
    }

    @SuppressWarnings("unused")
    public static void takeSnapShot(String fileWithPath) {
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

    protected void fillOutContactInfo() {
        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("phoneOrEmail", "Text me");
    }

    protected Boolean allPdfsHaveBeenDownloaded() {
        File[] listFiles = path.toFile().listFiles();
        List<String> documentNames = Arrays.stream(listFiles).map(File::getName).collect(Collectors.toList());

        Function<Document, Boolean> expectedPdfExists = expectedPdfName -> documentNames.stream().anyMatch(documentName ->
                documentName.contains("_MNB_") && documentName.endsWith(".pdf") &&
                        documentName.contains(expectedPdfName.toString())
        );
        return List.of(CAF, CCAP).stream().allMatch(expectedPdfExists::apply);
    }

    protected void completeFlowFromLandingPageThroughContactInfo(List<String> programSelections) {
        testPage.clickButton("Apply now");
        testPage.clickContinue();
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", "Yes");
        testPage.clickContinue();
        programSelections.forEach(program -> testPage.enter("programs", program));
        testPage.clickContinue();
        testPage.clickContinue();

        fillOutPersonalInfo();

        testPage.clickContinue();
    }

    protected void completeFlowFromLandingPageThroughReviewInfo(List<String> programSelections, SmartyStreetClient mockSmartyStreetClient) {
        completeFlowFromLandingPageThroughContactInfo(programSelections);

        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", "some@email.com");
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();
        testPage.enter("zipCode", "12345");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        testPage.enter("isHomeless", "I don't have a permanent address");
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
        assertThat(driver.findElementById("mailing-address_street").getText()).isEqualTo("smarty street");
    }

    protected SuccessPage nonExpeditedFlowToSuccessPage(boolean hasHousehold, boolean isWorking, SmartyStreetClient mockSmartyStreetClient) {
        completeFlowFromLandingPageThroughReviewInfo(List.of(PROGRAM_CCAP, PROGRAM_CASH), mockSmartyStreetClient);
        testPage.clickLink("This looks correct");

        if (hasHousehold) {
            testPage.enter("addHouseholdMembers", YES.getDisplayValue());
            testPage.clickContinue();
            fillOutHousemateInfo(PROGRAM_CCAP);
            testPage.clickContinue();
            testPage.clickButton("Yes, that's everyone");
            testPage.enter("whoNeedsChildCare", "defaultFirstName defaultLastName");
            testPage.clickContinue();
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
        testPage.clickContinue();

        if (isWorking) {
            testPage.enter("areYouWorking", YES.getDisplayValue());
            testPage.clickButton("Add a job");

            if (hasHousehold) {
                testPage.enter("whoseJobIsIt", "defaultFirstName defaultLastName");
                testPage.clickContinue();
            }

            testPage.enter("employersName", "some employer");
            testPage.clickContinue();
            testPage.enter("selfEmployment", YES.getDisplayValue());
            paidByTheHourOrSelectPayPeriod();
            testPage.enter("currentlyLookingForJob", NO.getDisplayValue());
        } else {
            testPage.enter("areYouWorking", NO.getDisplayValue());
            testPage.enter("currentlyLookingForJob", YES.getDisplayValue());

            if (hasHousehold) {
                testPage.enter("whoIsLookingForAJob", "defaultFirstName defaultLastName");
                testPage.clickContinue();
            }
        }

        testPage.clickContinue();
        testPage.enter("unearnedIncome", "Social Security");
        testPage.clickContinue();
        testPage.enter("socialSecurityAmount", "200");
        testPage.clickContinue();
        testPage.enter("unearnedIncomeCcap", "Money from a Trust");
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
        testPage.enter("supportAndCare", YES.getDisplayValue());
        testPage.enter("haveVehicle", YES.getDisplayValue());
        testPage.enter("ownRealEstate", YES.getDisplayValue());
        testPage.enter("haveInvestments", NO.getDisplayValue());
        testPage.enter("haveSavings", YES.getDisplayValue());
        testPage.enter("liquidAssets", "1234");
        testPage.clickContinue();
        testPage.enter("haveMillionDollars", NO.getDisplayValue());
        testPage.enter("haveSoldAssets", NO.getDisplayValue());
        testPage.clickContinue();
        testPage.enter("registerToVote", "Yes, send me more info");
        completeHelperWorkflow();
        driver.findElement(By.id("additionalInfo")).sendKeys("Some additional information about my application");
        testPage.clickContinue();
        testPage.enter("agreeToTerms", "I agree");
        testPage.clickContinue();
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        skipDocumentUploadFlow();

        return new SuccessPage(driver);
    }

    protected void skipDocumentUploadFlow() {
        testPage.clickButton("Skip this for now");
    }

    protected void fillOutHousemateInfo(String programSelection) {
        testPage.enter("relationship", "housemate");
        testPage.enter("programs", programSelection);
        fillOutPersonInfo(); // need to fill out programs checkbox set above first
        testPage.enter("moveToMnPreviousState", "Illinois");
    }

    protected void paidByTheHourOrSelectPayPeriod() {
        if (new Random().nextBoolean()) {
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

    private void completeHelperWorkflow() {
        if (new Random().nextBoolean()) {
            testPage.enter("helpWithBenefits", YES.getDisplayValue());
            testPage.enter("communicateOnYourBehalf", YES.getDisplayValue());
            testPage.enter("getMailNotices", YES.getDisplayValue());
            testPage.enter("spendOnYourBehalf", YES.getDisplayValue());
            fillOutHelperInfo();
        } else {
            testPage.enter("helpWithBenefits", NO.getDisplayValue());
        }
    }

	protected void completeFlowFromReviewInfoToDisability(List<String> programSelections) {
		testPage.clickLink("This looks correct");
		testPage.enter("addHouseholdMembers", NO.getDisplayValue());
		testPage.clickContinue();
		if(programSelections.contains(PROGRAM_CCAP)) {
			testPage.enter("livingSituation", "None of these");
			testPage.clickContinue();
		}
		testPage.enter("goingToSchool", YES.getDisplayValue());
		testPage.enter("isPregnant", NO.getDisplayValue());
		testPage.enter("migrantOrSeasonalFarmWorker", NO.getDisplayValue());
		testPage.enter("isUsCitizen", YES.getDisplayValue());
		testPage.enter("hasDisability", NO.getDisplayValue());
	}

    protected void completeDocumentUploadFlow() {
        testPage.clickElementById("drag-and-drop-box");
        uploadDefaultFile();

        testPage.clickButton("I'm finished uploading");
    }

    private String getAbsoluteFilepath(String resourceFilename) {
        URL resource = this.getClass().getClassLoader().getResource(resourceFilename);
        if (resource != null) {
            return resource.getFile();
        }
        return "";
    }

    private void uploadFile(String filepath) {
        testPage.clickElementById("drag-and-drop-box"); // is this needed?
        WebElement upload = driver.findElement(By.cssSelector("input"));
        upload.sendKeys(filepath);
    }

    protected void uploadDefaultFile() {
        uploadFile(getAbsoluteFilepath(UPLOADED_FILE_NAME));
        assertThat(driver.findElement(By.id("document-upload")).getText()).contains(UPLOADED_FILE_NAME);
    }

    private void getToDocumentRecommendationScreen() {
        testPage.clickButton("Apply now");
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
        testPage.clickButton("Submit");
    }

    protected void getToDocumentUploadScreen() {
        getToDocumentRecommendationScreen();
        testPage.clickButton("Upload documents now");
    }
}
