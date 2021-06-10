package org.codeforamerica.shiba.application;

import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.mockito.Mockito.*;

class ApplicationFactoryTest {

    Clock clock = mock(Clock.class);

    @SuppressWarnings("unchecked")
    CountyParser countyParser = mock(CountyParser.class);
    MonitoringService monitoringService = mock(MonitoringService.class);
    ApplicationFactory applicationFactory = new ApplicationFactory(clock, countyParser, monitoringService);
    ApplicationData applicationData = new ApplicationData();
    ZoneOffset zoneOffset = ZoneOffset.UTC;
    PagesData pagesData;

    @BeforeEach
    void setUp() {
        pagesData = new PagesData();
        PageData homeAddress = new PageData();
        homeAddress.put("zipCode", InputData.builder().value(List.of("something")).build());
        pagesData.put("homeAddress", homeAddress);
        applicationData.setPagesData(pagesData);
        Subworkflows subworkflows = new Subworkflows();
        subworkflows.addIteration("someGroup", new PagesData(Map.of("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())))));
        applicationData.setSubworkflows(subworkflows);
        applicationData.setFlow(FlowType.FULL);
        applicationData.setStartTimeOnce(Instant.EPOCH);

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
    void shouldProvideApplicationFlow() {
        FlowType flow = FlowType.FULL;
        applicationData.setFlow(flow);

        Application application = applicationFactory.newApplication(applicationData);

        assertThat(application.getFlow()).isEqualTo(flow);
    }

    @Test
    void shouldParseCounty() {
        when(countyParser.parse(applicationData)).thenReturn(Hennepin);

        Application application = applicationFactory.newApplication(applicationData);

        assertThat(application.getCounty()).isEqualTo(Hennepin);
    }

    @Test
    void shouldAddApplicationIdToMonitoringServiceForSentry() {
        applicationData.setId("appId");
        applicationFactory.newApplication(applicationData);

        verify(monitoringService).setApplicationId("appId");
    }
}