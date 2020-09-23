package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationFactory;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.metrics.Metrics;
import org.codeforamerica.shiba.pages.CountyMap;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static java.util.Collections.emptyList;
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

    CountyMap<MnitCountyInformation> countyMap = new CountyMap<>();

    @SuppressWarnings("unchecked")
    ApplicationDataParser<Address> homeAddressParser = mock(ApplicationDataParser.class);
    ApplicationFactory applicationFactory = new ApplicationFactory(clock, countyZipCodeMap, countyMap, locationClient, homeAddressParser);

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
        PageData chooseProgramsData = new PageData();
        chooseProgramsData.put("programs", InputData.builder().value(emptyList()).build());
        pagesData.put("choosePrograms", chooseProgramsData);
        applicationData.setPagesData(pagesData);
        Subworkflows subworkflows = new Subworkflows();
        subworkflows.addIteration("someGroup", new PagesData(Map.of("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())))));
        applicationData.setSubworkflows(subworkflows);
        applicationData.setFlow(FlowType.FULL);

        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(zoneOffset);
        when(homeAddressParser.parse(any())).thenReturn(new Address("", "", "", "something"));
        countyMap.getCounties().put(OTHER, new MnitCountyInformation());
        countyMap.getCounties().put(HENNEPIN, new MnitCountyInformation());
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
        when(homeAddressParser.parse(applicationData)).thenReturn(new Address("", "", "", zipCode));

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

    @Nested
    class fileName {
        @Test
        void shouldIncludeIdInFileNameForApplication() {
            String applicationId = "someId";
            Application application = applicationFactory.newApplication(applicationId, applicationData, defaultMetrics);
            assertThat(application.getFileName()).contains(applicationId);
        }

        @Test
        void shouldIncludeSubmitDateInCentralTimeZone() {
            when(clock.instant()).thenReturn(Instant.parse("2007-09-10T04:59:59.00Z"));
            Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);
            assertThat(application.getFileName()).contains("20070909");
        }

        @Test
        void shouldIncludeSubmitTimeInCentralTimeZone() {
            when(clock.instant()).thenReturn(Instant.parse("2007-09-10T04:05:59.00Z"));
            Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);
            assertThat(application.getFileName()).contains("230559");
        }

        @Test
        void shouldIncludeCorrectCountyNPI() {
            String countyNPI = setupCounty();

            Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);

            assertThat(application.getFileName()).contains(countyNPI);
        }

        @Test
        void shouldIncludeProgramCodes() {
            PageData chooseProgramsData = new PageData();
            List<String> programs = new ArrayList<>(List.of(
                    "SNAP", "CASH", "GRH", "EA", "CCAP"
            ));
            Collections.shuffle(programs);
            chooseProgramsData.put("programs", InputData.builder().value(programs).build());
            pagesData.put("choosePrograms", chooseProgramsData);
            Application application = applicationFactory.newApplication("", applicationData, defaultMetrics);

            assertThat(application.getFileName()).contains("EKFC");
        }

        @Test
        void shouldArrangeNameCorrectly() {
            String countyNPI = setupCounty();
            String applicationId = "someId";
            setupProgramData();
            when(clock.instant()).thenReturn(Instant.parse("2007-09-10T04:59:59.00Z"));

            Application application = applicationFactory.newApplication(applicationId, applicationData, defaultMetrics);

            assertThat(application.getFileName()).isEqualTo(String.format("%s_MNB_%s_%s_%s_%s",
                    countyNPI, "20070909", "235959", applicationId, "EKFC"));
        }

        @Test
        void shouldCreateFileNameWhenReconstituteAnApplication() {
            String countyNPI = setupCounty();
            String applicationId = "someId";
            setupProgramData();
            Instant completedAt = Instant.parse("2007-09-10T04:59:59.00Z");
            when(clock.instant()).thenReturn(completedAt);

            Application application = applicationFactory.reconstituteApplication(
                    applicationId,
                    ZonedDateTime.ofInstant(completedAt, ZoneId.of("UTC")),
                    applicationData,
                    HENNEPIN,
                    null,
                    null,
                    Sentiment.HAPPY, "someFeedback");

            assertThat(application.getFileName()).isEqualTo(String.format("%s_MNB_%s_%s_%s_%s",
                    countyNPI, "20070909", "235959", applicationId, "EKFC"));
        }

        private void setupProgramData() {
            PageData chooseProgramsData = new PageData();
            chooseProgramsData.put("programs", InputData.builder().value(List.of(
                    "SNAP", "CASH", "GRH", "EA", "CCAP"
            )).build());
            pagesData.put("choosePrograms", chooseProgramsData);
        }

        private String setupCounty() {
            County county = HENNEPIN;
            when(locationClient.getCounty(any())).thenReturn(Optional.of("Hennepin"));
            String zipCode = "someZip";
            countyZipCodeMap.put(zipCode, county);
            MnitCountyInformation mnitCountyInformation = new MnitCountyInformation();
            String countyNPI = "someNPI";
            mnitCountyInformation.setDhsProviderId(countyNPI);
            countyMap.getCounties().put(county, mnitCountyInformation);
            PageData homeAddress = new PageData();
            homeAddress.put("zipCode", InputData.builder().value(List.of(zipCode)).build());
            pagesData.put("homeAddress", homeAddress);
            return countyNPI;
        }
    }

}