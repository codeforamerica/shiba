package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.codeforamerica.shiba.pages.enrichment.smartystreets.SmartyStreetClient;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("integration")
public class ReleaseUserJourneyPageTest extends AbstractBasePageTest {

    @MockBean
    Clock clock;

    @MockBean
    PageEventPublisher pageEventPublisher;

    @MockBean
    SmartyStreetClient smartyStreetClient;

    @MockBean
    MailGunEmailClient mailGunEmailClient;

    @MockBean
    FeatureFlagConfiguration featureFlagConfiguration;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl);
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(smartyStreetClient.validateAddress(any())).thenReturn(Optional.empty());

        when(featureFlagConfiguration.get("document-upload-feature")).thenReturn(FeatureFlag.OFF);
    }

    @Test
    void documentUploadIsDisabledInRelease() {
        testPage.clickButton("Apply now");
        testPage.clickContinue();
        testPage.enter("writtenLanguage", "English");
        testPage.enter("spokenLanguage", "English");
        testPage.enter("needInterpreter", "Yes");
        testPage.clickContinue();
        testPage.enter("programs", "Emergency Assistance");
        testPage.clickContinue();
        testPage.clickContinue();
        fillOutPersonalInfo();
        testPage.clickContinue();
        navigateTo("signThisApplication");
        testPage.enter("applicantSignature", "some name");
        testPage.clickButton("Submit");

        assertThat(testPage.getTitle()).isEqualTo("Success");
    }

    private void fillOutPersonInfo() {
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

    private void fillOutPersonalInfo() {
        fillOutPersonInfo();
        testPage.enter("moveToMnPreviousCity", "Chicago");
    }
}
