package org.codeforamerica.shiba.pages.features;

import org.codeforamerica.shiba.AbstractBasePageTest;
import org.codeforamerica.shiba.UploadDocumentConfiguration;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public abstract class FeatureTest extends AbstractBasePageTest {
    @MockBean public Clock clock;
    @MockBean public SmartyStreetClient smartyStreetClient;
    @MockBean PageEventPublisher pageEventPublisher;
    @MockBean MailGunEmailClient mailGunEmailClient;
    @MockBean FeatureFlagConfiguration featureFlagConfiguration;
    @SpyBean UploadDocumentConfiguration uploadDocumentConfiguration;

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl);
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(smartyStreetClient.validateAddress(any())).thenReturn(Optional.empty());

        when(featureFlagConfiguration.get("document-upload-feature")).thenReturn(FeatureFlag.ON);
        when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);
        when(featureFlagConfiguration.get("send-non-partner-county-alert")).thenReturn(FeatureFlag.OFF);

        when(uploadDocumentConfiguration.getMaxFilesize()).thenReturn(50);
    }
}
