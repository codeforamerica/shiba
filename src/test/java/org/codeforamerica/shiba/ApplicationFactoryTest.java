package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationFactoryTest {
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    Clock clock = mock(Clock.class);

    ApplicationFactory applicationFactory = new ApplicationFactory(applicationRepository, clock);

    ApplicationData applicationData = new ApplicationData();

    ZoneOffset zoneOffset = ZoneOffset.UTC;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(zoneOffset);
    }

    @Test
    void shouldObtainACopyOfTheApplicationData() {
        Application application = applicationFactory.newApplication(applicationData);

        assertThat(application.getApplicationData()).isNotSameAs(applicationData);
        assertThat(application.getApplicationData()).isEqualTo(applicationData);
    }

    @Test
    void shouldProvideApplicationId() {
        String applicationId = "someId";
        when(applicationRepository.getNextId()).thenReturn(applicationId);

        Application application = applicationFactory.newApplication(applicationData);

        assertThat(application.getId()).isEqualTo(applicationId);
    }

    @Test
    void shouldProvideCompletedAtTimestamp() {
        Instant instant = Instant.ofEpochSecond(125423L);
        when(clock.instant()).thenReturn(instant);

        Application application = applicationFactory.newApplication(applicationData);

        assertThat(application.getCompletedAt()).isEqualTo(ZonedDateTime.ofInstant(instant, zoneOffset));
    }
}