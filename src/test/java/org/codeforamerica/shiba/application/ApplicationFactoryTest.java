package org.codeforamerica.shiba.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.parsers.CountyParser;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationFactoryTest {

  Clock clock = mock(Clock.class);

  CountyParser countyParser = mock(CountyParser.class);
  MonitoringService monitoringService = mock(MonitoringService.class);
  ApplicationFactory applicationFactory = new ApplicationFactory(clock, countyParser,
      monitoringService);
  ApplicationData applicationData = new ApplicationData();
  ZoneOffset zoneOffset = ZoneOffset.UTC;

  @BeforeEach
  void setUp() {
    new TestApplicationDataBuilder(applicationData)
        .withPageData("homeAddress", "zipCode", "something")
        .withSubworkflow("someGroup", new PagesDataBuilder()
            .withPageData("somePage", "someInput", "someValue"));
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
