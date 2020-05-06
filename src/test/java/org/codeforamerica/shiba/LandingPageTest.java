package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.LandingPage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LandingPageTest {
    @Test
    void shouldShowTheLandingPage() {
        LandingPage landingPage = new LandingPage();

        assertThat(landingPage.getHeader()).isEqualTo("Minnesota’s combined benefits application");
    }
}
