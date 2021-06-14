package org.codeforamerica.shiba.pages.journeys;

import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.pages.Page;
import org.codeforamerica.shiba.pages.SuccessPage;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public abstract class JourneyTest extends AbstractBasePageTest {
    protected PDAcroForm caf;

    @MockBean
    protected Clock clock;
    @MockBean
    protected SmartyStreetClient smartyStreetClient;
    @SpyBean
    protected DocumentRepositoryService documentRepositoryService;
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

        when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
        caf = null;
    }

    protected void assertCafFieldEquals(String fieldName, String expectedVal) {
        assertPdfFieldEquals(fieldName, expectedVal, caf);
    }

    private void assertPdfFieldEquals(String fieldName, String expectedVal, PDAcroForm pdf) {
        assertThat(getPdfFieldText(pdf, fieldName)).isEqualTo(expectedVal);
    }

    protected String signApplicationAndDownloadCaf(String signature) {
        testPage.enter("applicantSignature", signature);
        testPage.clickButton("Submit");

        // No document upload
        testPage.clickButton("Skip this for now");

        // Download CAF
        SuccessPage successPage = new SuccessPage(driver);
        assertThat(successPage.CAFdownloadPresent()).isTrue();
        assertThat(successPage.CCAPdownloadPresent()).isFalse();
        successPage.downloadPdfs();
        await().until(() -> getAllFiles().size() == successPage.pdfDownloadLinks());
        caf = getAllFiles().get(CAF);
        return successPage.getConfirmationNumber(); // Application ID
    }

    protected static void getToHomeAddress(String dateOfBirth, String email, String firstName, String lastName, String moveDate, String needsInterpreter, String otherName, String previousCity, String sex, Page testPage, List<String> programSelectinos) {
        testPage.clickButton("Apply now");
        testPage.clickContinue();

        // Language Preferences
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", needsInterpreter);
        testPage.clickContinue();

        // Program Selection

        programSelectinos.forEach(program -> testPage.enter("programs", program));
        testPage.clickContinue();
        testPage.clickContinue();

        // Personal Info
        testPage.enter("firstName", firstName);
        testPage.enter("lastName", lastName);
        testPage.enter("otherName", otherName);
        testPage.enter("dateOfBirth", dateOfBirth);
        testPage.enter("ssn", "123456789");
        testPage.enter("maritalStatus", "Never married");
        testPage.enter("sex", sex);
        testPage.enter("livedInMnWholeLife", "Yes");
        testPage.enter("moveToMnDate", moveDate);
        testPage.enter("moveToMnPreviousCity", previousCity);
        testPage.clickContinue();

        // How can we get in touch with you?
        testPage.enter("phoneNumber", "7234567890");
        testPage.enter("email", email);
        testPage.enter("phoneOrEmail", "Text me");
        testPage.clickContinue();
    }

}
