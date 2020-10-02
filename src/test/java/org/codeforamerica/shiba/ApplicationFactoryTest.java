package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.HENNEPIN;
import static org.codeforamerica.shiba.County.OTHER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationFactoryTest {

    Clock clock = mock(Clock.class);
    LocationClient locationClient = mock(LocationClient.class);

    Map<String, County> countyZipCodeMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    ApplicationDataParser<Address> homeAddressParser = mock(ApplicationDataParser.class);

    ApplicationFactory applicationFactory = new ApplicationFactory(clock, countyZipCodeMap, locationClient, homeAddressParser);

    ApplicationData applicationData = new ApplicationData();

    Metrics defaultMetrics = new Metrics();

    ZoneOffset zoneOffset = ZoneOffset.UTC;

    PagesData pagesData;

    @BeforeEach
    void setUp() {
        defaultMetrics.setStartTimeOnce(Instant.EPOCH);
        pagesData = new PagesData();
        PageData homeAddress = new PageData();
        homeAddress.put("zipCode", InputData.builder().value(List.of("something")).build());
        pagesData.put("homeAddress", homeAddress);
        applicationData.setPagesData(pagesData);
        Subworkflows subworkflows = new Subworkflows();
        subworkflows.addIteration("someGroup", new PagesData(Map.of("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())))));
        applicationData.setSubworkflows(subworkflows);
        applicationData.setFlow(FlowType.FULL);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(zoneOffset);
        when(homeAddressParser.parse(any())).thenReturn(new Address("", "", "", "something",""));
    }

    @Test
    void shouldObtainACopyOfTheApplicationData() {
        Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);

        assertThat(application.getApplicationData()).isNotSameAs(applicationData);
        assertThat(application.getApplicationData()).isEqualTo(applicationData);
    }

    @Test
    void shouldProvideCompletedAtTimestamp() {
        Instant instant = Instant.ofEpochSecond(125423L);
        when(clock.instant()).thenReturn(instant);

        Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);

        assertThat(application.getCompletedAt()).isEqualTo(ZonedDateTime.ofInstant(instant, zoneOffset));
    }

    @Test
    void shouldProvideTimeToComplete() {
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        Metrics metrics = new Metrics();
        metrics.setStartTimeOnce(now.minusSeconds(142));

        Application application = applicationFactory.newApplication("", applicationData, metrics);

        assertThat(application.getTimeToComplete()).isEqualTo(Duration.ofSeconds(142));
    }

    @Test
    void shouldProvideApplicationFlow() {
        FlowType flow = FlowType.FULL;
        applicationData.setFlow(flow);

        Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);

        assertThat(application.getFlow()).isEqualTo(flow);
    }

    @Test
    void shouldUseLocationClientToGetCounty() {
        when(locationClient.getCounty(any())).thenReturn(Optional.of("Hennepin"));

        Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);

        assertThat(application.getCounty()).isEqualTo(HENNEPIN);
    }

    @Test
    void shouldProvideCountyThroughMappingWhenLocationCountyIsNotDetermined() {
        String zipCode = "12345";
        countyZipCodeMap.put(zipCode, HENNEPIN);
        PageData homeAddress = new PageData();
        homeAddress.put("zipCode", InputData.builder().value(List.of(zipCode)).build());
        pagesData.put("homeAddress", homeAddress);

        when(locationClient.getCounty(any())).thenReturn(Optional.empty());
        when(homeAddressParser.parse(applicationData)).thenReturn(new Address("", "", "", zipCode, ""));

        Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);

        assertThat(application.getCounty()).isEqualTo(HENNEPIN);
    }

    @Test
    void shouldProvideCountyThroughMappingWhenLocationCountyIsNotDetermined_labelCountyAsOtherWhenZipCodeHasNoMapping() {
        String zipCode = "12345";
        PageData homeAddress = new PageData();
        homeAddress.put("zipCode", InputData.builder().value(List.of(zipCode)).build());
        pagesData.put("homeAddress", homeAddress);

        when(locationClient.getCounty(any())).thenReturn(Optional.empty());

        Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);

        assertThat(application.getCounty()).isEqualTo(OTHER);
    }
}