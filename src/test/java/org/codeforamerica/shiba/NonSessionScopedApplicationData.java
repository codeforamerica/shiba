package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class NonSessionScopedApplicationData {
    @Bean
    public ApplicationData applicationData() {
        return new ApplicationData();
    }
}
