package org.codeforamerica.shiba.pages.journeys;

import org.codeforamerica.shiba.AbstractShibaMockMvcTest;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Tag("journey")
public class UserJourneyMockMvcTest extends AbstractShibaMockMvcTest {
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        when(featureFlagConfiguration.get("apply-without-address")).thenReturn(FeatureFlag.OFF);
        mockMvc.perform(get("/pages/languagePreferences").session(session)); // start timer
        postExpectingSuccess("languagePreferences",
                             Map.of("writtenLanguage", List.of("ENGLISH"), "spokenLanguage", List.of("ENGLISH"))
        );
    }


}
