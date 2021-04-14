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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
import static org.awaitility.Awaitility.await;
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
    public static final String PROGRAM_SNAP = "Food (SNAP)";
    public static final String PROGRAM_CASH = "Cash programs";
    public static final String PROGRAM_GRH = "Housing Support (GRH)";
    public static final String PROGRAM_CCAP = "Child Care Assistance";
    public static final String PROGRAM_EA = "Emergency Assistance";

    private static final String UPLOADED_JPG_FILE_NAME = "shiba+file.jpg";
    private static final String UPLOADED_HEIC_FILE_NAME = "sample.heic";
    private static final String UPLOADED_PDF_NAME = "test-caf.pdf";
    protected static final String DEFAULT_DOCUMENT_THUMBNAIL = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEsAAABGCAYAAACE0Gk0AAAFK0lEQVR4Xu2caVcTSRSG314SyEJQPIAsMucoihDmzJE1BBhnEZ35xzPjmU9ABAR/AThnFBBQQciedLrn3Epaw6Z9OxsTqr7kQ25XVz31VtWt5baSTiYsyOSIgCJhOeIkjCQs56wkLAYrCasusFKpFF5vbmJ/7x1My4KmqZz3VmRr5A3oHh3DI6Po6e2tKC/Ow67GrJOTE7xaX8PB/gG8Xi8URYFl1XNStZDNZtF+4yamIzO42dHBqbNrWzasbDaDtZUV7GxvIxgMNgAUTUsALCCRSKCzsxMzc3Pw+wOuITh9kA3r9dYmNtZforW1FaqiflEUVaDWqUy8pGbTMpGIx3Fn4DtEorPQdb2mJWDDii0t4mB/Dx6Pt85dj8RkQRGyKiYCZhQMpJJJDN5/gPHJKahq7cZONqznf/6BZCLxuRXtAmfS6RK8WknMgq57hKLLk6KqyGbSSKfTGJuYxPBIuGbqqggWtbRlWtB0HT6fT7RqoVCAUgNeiqLCMPLIZDJFVdkKUyDeSw1Ik8zM7JzolrVIbFh/P/8L8ZMTMXWbBRPpdAq9fX2YikTR0tKCXC5X1lGqV2RN17C3t4dX6y9Fg2iq9iVzhbqkKsrl8/sQnZ1HZ1dX9V5ud3vu2vAsrFQqKVoyOjcvxpBapg8f3mM1FoNhGNC0Mlil8YuUTgN+e/sNROfnxW81U0XKskwTyWRRWSR/j8dTzbKdy+vd7i421tdQMArnYJX6JqhMx8fH6OnpxeyPj8+NcZUUsCqw+vr7EIleAVglheXzOcTjcTwYeojpmWglfE5PJpV0Q1tZVwmWGPxphsxmQDP0D4/GEB79virAmk5ZdncUM2QyiVw2i8npiFBZpak5YRV9C+FSHH/6JFyK+cc/487AQEW8mhdWGbCjw0PhB/70yxN03LrlGlhzwyqNX+TRHH78KECRwtra2lwBa3pYREXViisL6pL9dwYQmYmi1edjA7sWsGxg+XweiXgC94eG8GhsnO0XXhtYtkuRz+VAftjDkTBGwqMXO7eXaO5awRLAFEXAorXkcDjMcimuHSzbDyP/K9TejidPf3M8dv2vYO3u7mBjbRXGZWvDr1T77KYgLcYDwQAWnv3enLBo33/1xTILFjmk1PW8Xg9UVfu8u0uw2kIh/LrwtDlhHR0d4p+tLZimKdZ/ThJt5ZDb8P5gX6wVaaOSNg6bHlY5HKdHb+JgwyxgeXFRnEgFAn4Bms4em1pZTpR0kQ2paCW2jO23b8SRGTmpEtYlNMkZXYktYWd7RyrrW4qTsL5FqOx/CUvCYhBgmEplSVgMAgxTqSwJi0GAYSqVJWExCDBMpbIkLAYBhqlUloTFIMAwlcqSsBgEGKZSWRIWgwDDVCpLwmIQYJhKZUlYDAIMU6ksCYtBgGF6pZRVrwgLBp9TpgTrxfISdncaeMhKgU71jN1xC4vuRVCc5Ns3/zbu+L6eUWFuQFHgK8VwUzz12kpMKMvn89fvrkN5cGY94w1dwbIg7ozSFSUK3iwYBhSVLhwVrxwFgkEsPKvhzb/GRbK6wUXPFC+z0VVuXdNPXWarOaxGxki7xXX2OfsSblf3bREn6TSx75Q2NPreaa0usitF7ttR+xQ+PD4xibv3Bh3nyoZ1Jb7r4Lh6pw3tj3XQ9yD6+vsxFYmgpeV0gPrXsmbDoswa/8UQPi0bFMVwd3V3YWxiCqFQiJWRK1j0hkZ+i4ZVw5JxoWBCVRR03+7B3cFB+P1+djauYbHf1AQPSFiMRpSwJCwGAYbpf7oLq5uJEmkZAAAAAElFTkSuQmCC";
    protected static final String SHIBA_IMAGE_THUMBNAIL = "data:image/png;base64,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";

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
        options.addArguments("--window-size=1280,1600");
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
        return Arrays.stream(Objects.requireNonNull(path.toFile().listFiles()))
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

    protected void waitForDocumentUploadToComplete() {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("delete")));
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
        List<String> documentNames = Arrays.stream(Objects.requireNonNull(listFiles)).map(File::getName).collect(Collectors.toList());

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
        assertThat(driver.findElementById("mailing-address_street").getText()).isEqualTo("smarty street");
    }

    protected void fillOutAddress() {
        testPage.enter("zipCode", "12345");
        testPage.enter("city", "someCity");
        testPage.enter("streetAddress", "someStreetAddress");
        testPage.enter("apartmentNumber", "someApartmentNumber");
        testPage.enter("isHomeless", "I don't have a permanent address");
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
        if (programSelections.contains(PROGRAM_CCAP) || programSelections.contains(PROGRAM_GRH)) {
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
        uploadJpgFile();
        waitForDocumentUploadToComplete();
        testPage.clickButton("I'm finished uploading");
    }

    private String getAbsoluteFilepath(String resourceFilename) {
        URL resource = this.getClass().getClassLoader().getResource(resourceFilename);
        if (resource != null) {
        	return (new File(resource.getFile())).getAbsolutePath();
        }
        return "";
    }

    private void uploadFile(String filepath) {
        testPage.clickElementById("drag-and-drop-box"); // is this needed?
        WebElement upload = driver.findElement(By.className("dz-hidden-input"));
        upload.sendKeys(filepath);
        await().until(() -> !driver.findElementsByClassName("file-details").get(0).getAttribute("innerHTML").isBlank());
    }

    protected void uploadJpgFile() {
        uploadFile(getAbsoluteFilepath(UPLOADED_JPG_FILE_NAME));
        assertThat(driver.findElement(By.id("document-upload")).getText()).contains(UPLOADED_JPG_FILE_NAME);
    }

    protected void uploadHeicFile() {
        uploadFile(getAbsoluteFilepath(UPLOADED_HEIC_FILE_NAME));
        assertThat(driver.findElement(By.id("document-upload")).getText()).contains(UPLOADED_HEIC_FILE_NAME);
    }

    protected void uploadPdfFile() {
        uploadFile(getAbsoluteFilepath(UPLOADED_PDF_NAME));
        assertThat(driver.findElement(By.id("document-upload")).getText()).contains(UPLOADED_PDF_NAME);
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
