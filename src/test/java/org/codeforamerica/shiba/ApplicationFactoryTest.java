package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.HENNEPIN;
import static org.codeforamerica.shiba.County.OTHER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationFactoryTest {
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    Clock clock = mock(Clock.class);

    Map<String, County> countyZipCodeMap = new HashMap<>();

    ApplicationFactory applicationFactory = new ApplicationFactory(applicationRepository, clock, countyZipCodeMap);

    ApplicationData applicationData = new ApplicationData();

    ZoneOffset zoneOffset = ZoneOffset.UTC;

    @BeforeEach
    void setUp() {
        PagesData pagesData = new PagesData();
        PageData homeAddress = new PageData();
        homeAddress.put("zipCode", InputData.builder().value(List.of("something")).build());
        pagesData.put("homeAddress", homeAddress);
        applicationData.setPagesData(pagesData);
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

    @Test
    void shouldProvideCounty() {
        String zipCode = "12345";
        countyZipCodeMap.put(zipCode, HENNEPIN);
        PagesData pagesData = new PagesData();
        PageData homeAddress = new PageData();
        homeAddress.put("zipCode", InputData.builder().value(List.of(zipCode)).build());
        pagesData.put("homeAddress", homeAddress);

        applicationData.setPagesData(pagesData);
        Application application = applicationFactory.newApplication(applicationData);

        assertThat(application.getCounty()).isEqualTo(HENNEPIN);
    }

    @Test
    void shouldLabelCountyAsOtherWhenZipCodeHasNoMapping() {
        String zipCode = "12345";
        PagesData pagesData = new PagesData();
        PageData homeAddress = new PageData();
        homeAddress.put("zipCode", InputData.builder().value(List.of(zipCode)).build());
        pagesData.put("homeAddress", homeAddress);

        applicationData.setPagesData(pagesData);
        Application application = applicationFactory.newApplication(applicationData);

        assertThat(application.getCounty()).isEqualTo(OTHER);
    }
}