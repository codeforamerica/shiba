package org.codeforamerica.shiba.metrics;

import org.codeforamerica.shiba.*;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Sql(statements = {"ALTER SEQUENCE application_id RESTART WITH 12", "TRUNCATE TABLE applications"})
class ApplicationRepositoryTest {

    @Autowired
    ApplicationRepository applicationRepository;

    @SuppressWarnings("unchecked")
    Encryptor<ApplicationData> mockEncryptor = mock(Encryptor.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ApplicationFactory applicationFactory;

    @MockBean
    Clock clock;

    ApplicationRepository applicationRepositoryWithMockEncryptor;

    @Test
    void shouldGenerateIdForNextApplication() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).endsWith("12");

        String nextIdAgain = applicationRepository.getNextId();

        assertThat(nextIdAgain).endsWith("13");
    }

    @Test
    void shouldPrefixIdWithRandom3DigitSalt() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).matches("^[1-9]\\d{2}.*");

        String nextIdAgain = applicationRepository.getNextId();

        assertThat(nextIdAgain.substring(0, 3)).isNotEqualTo(nextId.substring(0, 3));
    }

    @Test
    void shouldPadTheIdWithZeroesUntilReach10Digits() {
        String nextId = applicationRepository.getNextId();

        assertThat(nextId).hasSize(10);
        assertThat(nextId.substring(3, 8)).isEqualTo("00000");
    }

    @Test
    void shouldSaveApplication() {
        ApplicationData applicationData = new ApplicationData();
        PageData pageData = new PageData();
        pageData.put("programs", InputData.builder().value(emptyList()).build());
        applicationData.setPagesData(new PagesData(Map.of("choosePrograms", pageData)));
        Subworkflows subworkflows = new Subworkflows();
        PagesData subflowIteration = new PagesData();
        PageData groupedPage = new PageData();
        groupedPage.put("someGroupedPageInput", InputData.builder().value(List.of("someGroupedPageValue")).build());
        subflowIteration.put("someGroupedPage", groupedPage);
        subworkflows.addIteration("someGroup", subflowIteration);
        applicationData.setSubworkflows(subworkflows);

        Application application = Application.builder()
                .id("someid")
                .completedAt(ZonedDateTime.now(ZoneOffset.UTC))
                .applicationData(applicationData)
                .county(OLMSTED)
                .fileName("")
                .timeToComplete(Duration.ofSeconds(12415))
                .build();

        applicationRepository.save(application);

        Application savedApplication = applicationRepository.find("someid");
        assertThat(savedApplication.getId()).isEqualTo(application.getId());
        assertThat(savedApplication.getCompletedAt()).isEqualTo(application.getCompletedAt());
        assertThat(savedApplication.getApplicationData()).isEqualTo(application.getApplicationData());
        assertThat(savedApplication.getCounty()).isEqualTo(application.getCounty());
        assertThat(savedApplication.getTimeToComplete()).isEqualTo(application.getTimeToComplete());
    }

    @Nested
    @SpringBootTest
    @ExtendWith(SpringExtension.class)
    @ActiveProfiles("test")
    @Sql(statements = {"TRUNCATE TABLE applications"})
    class EncryptionAndDecryption {
        @BeforeEach
        void setUp() {
            applicationRepositoryWithMockEncryptor = new ApplicationRepository(jdbcTemplate, mockEncryptor, applicationFactory, clock);
            when(mockEncryptor.encrypt(any())).thenReturn("default encrypted data".getBytes());
        }

        @Test
        void shouldEncryptApplicationData() {
            ApplicationData applicationData = new ApplicationData();
            Application application = Application.builder()
                    .id("someid")
                    .completedAt(ZonedDateTime.now(ZoneOffset.UTC))
                    .applicationData(applicationData)
                    .county(OLMSTED)
                    .fileName("")
                    .timeToComplete(Duration.ofSeconds(1))
                    .build();

            applicationRepositoryWithMockEncryptor.save(application);

            verify(mockEncryptor).encrypt(applicationData);
        }

        @Test
        void shouldStoreEncryptedApplicationData() {
            ApplicationData applicationData = new ApplicationData();
            Application application = Application.builder()
                    .id("someid")
                    .completedAt(ZonedDateTime.now(ZoneOffset.UTC))
                    .applicationData(applicationData)
                    .county(OLMSTED)
                    .fileName("")
                    .timeToComplete(Duration.ofSeconds(1))
                    .build();
            byte[] expectedEncryptedData = "here is the encrypted data".getBytes();
            when(mockEncryptor.encrypt(any())).thenReturn(expectedEncryptedData);

            applicationRepositoryWithMockEncryptor.save(application);

            byte[] actualEncryptedData = jdbcTemplate.queryForObject(
                    "SELECT encrypted_data " +
                            "FROM applications " +
                            "WHERE id = 'someid'", byte[].class);
            assertThat(actualEncryptedData).isEqualTo(expectedEncryptedData);
        }

        @Test
        void shouldDecryptApplicationData() {
            ApplicationData applicationData = new ApplicationData();
            String applicationId = "someid";
            Application application = Application.builder()
                    .id(applicationId)
                    .completedAt(ZonedDateTime.now(ZoneOffset.UTC))
                    .applicationData(applicationData)
                    .county(OLMSTED)
                    .fileName("")
                    .timeToComplete(Duration.ofSeconds(1))
                    .build();
            byte[] encryptedData = "here is the encrypted data".getBytes();
            when(mockEncryptor.encrypt(any())).thenReturn(encryptedData);
            ApplicationData decryptedApplicationData = new ApplicationData();
            decryptedApplicationData.setPagesData(new PagesData(Map.of("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("CASH")).build())))));
            when(mockEncryptor.decrypt(any())).thenReturn(decryptedApplicationData);

            applicationRepositoryWithMockEncryptor.save(application);

            applicationRepositoryWithMockEncryptor.find(applicationId);

            verify(mockEncryptor).decrypt(encryptedData);
        }

        @Test
        void shouldUseDecryptedApplicationDataForTheRetrievedApplication() {
            ApplicationData applicationData = new ApplicationData();
            applicationData.setPagesData(new PagesData(Map.of("somePage", new PageData())));
            String applicationId = "someid";
            Application application = Application.builder()
                    .id(applicationId)
                    .completedAt(ZonedDateTime.now(ZoneOffset.UTC))
                    .applicationData(applicationData)
                    .county(OLMSTED)
                    .fileName("")
                    .timeToComplete(Duration.ofSeconds(1))
                    .build();
            ApplicationData decryptedApplicationData = new ApplicationData();
            decryptedApplicationData.setPagesData(new PagesData(Map.of("choosePrograms", new PageData(Map.of("programs", InputData.builder().value(List.of("CASH")).build())))));

            when(mockEncryptor.decrypt(any())).thenReturn(decryptedApplicationData);

            applicationRepositoryWithMockEncryptor.save(application);

            Application retrievedApplication = applicationRepositoryWithMockEncryptor.find(applicationId);

            assertThat(retrievedApplication.getApplicationData()).isEqualTo(decryptedApplicationData);
        }
    }

    @Nested
    @SpringBootTest
    @ExtendWith(SpringExtension.class)
    @ActiveProfiles("test")
    @Sql(statements = {"TRUNCATE TABLE applications"})
    class MetricsQueries {
        County defaultCounty = County.OTHER;

        ZonedDateTime defaultCompletedAt = ZonedDateTime.now(ZoneOffset.UTC);

        Duration defaultDuration = Duration.ofMinutes(14);

        @Test
        void shouldCalculateMedianTimeToComplete() {
            Application application1 = Application.builder()
                    .id("someId1")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(1))
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application2 = Application.builder()
                    .id("someId2")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(2))
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application3 = Application.builder()
                    .id("someId3")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(3))
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application4 = Application.builder()
                    .id("someId4")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(4))
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();

            applicationRepository.save(application1);
            applicationRepository.save(application2);
            applicationRepository.save(application3);
            applicationRepository.save(application4);

            assertThat(applicationRepository.getMedianTimeToComplete()).isEqualTo(Duration.ofDays(2).plusHours(12));
        }

        @Test
        void shouldReturn0ForMedianTimeToCompleteWhenThereIsNoEntries() {
            assertThat(applicationRepository.getMedianTimeToComplete()).isEqualTo(Duration.ZERO);
        }

        @Test
        void shouldGetCount() {
            Application application1 = Application.builder()
                    .id("someId1")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application2 = Application.builder()
                    .id("someId2")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application3 = Application.builder()
                    .id("someId3")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application4 = Application.builder()
                    .id("someId4")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();

            applicationRepository.save(application1);
            applicationRepository.save(application2);
            applicationRepository.save(application3);
            applicationRepository.save(application4);

            assertThat(applicationRepository.count()).isEqualTo(4);
        }

        @Test
        void shouldGetCountByCounty() {
            Application application1 = Application.builder()
                    .id("someId1")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(OLMSTED)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application2 = Application.builder()
                    .id("someId2")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(defaultCounty)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application3 = Application.builder()
                    .id("someId3")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(HENNEPIN)
                    .completedAt(defaultCompletedAt)
                    .build();
            Application application4 = Application.builder()
                    .id("someId4")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(HENNEPIN)
                    .completedAt(defaultCompletedAt)
                    .build();

            applicationRepository.save(application1);
            applicationRepository.save(application2);
            applicationRepository.save(application3);
            applicationRepository.save(application4);

            assertThat(applicationRepository.countByCounty()).isEqualTo(
                    Map.of(
                            HENNEPIN, 2,
                            OLMSTED, 1,
                            OTHER, 1
                    )
            );
        }

        @Test
        void shouldGetCountByCountyForWeekToDateInSpecifiedTimezone() {
        /*Calendar for reference
        S   M   T   W  TH  F  S
        29  30  31  1  2   3  4
        Chicago is in -06:00
        * */
            when(clock.instant()).thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
            Application application1 = Application.builder()
                    .id("someId1")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(OLMSTED)
                    .completedAt(ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")))
                    .build();
            Application application2 = Application.builder()
                    .id("someId2")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(OLMSTED)
                    .completedAt(ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")))
                    .build();
            Application application3 = Application.builder()
                    .id("someId3")
                    .applicationData(new ApplicationData())
                    .timeToComplete(defaultDuration)
                    .county(HENNEPIN)
                    .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
                    .build();

            applicationRepository.save(application1);
            applicationRepository.save(application2);
            applicationRepository.save(application3);

            assertThat(applicationRepository.countByCountyWeekToDate(ZoneId.of("America/Chicago"))).isEqualTo(
                    Map.of(
                            HENNEPIN, 1,
                            OLMSTED, 1
                    )
            );
        }

        @Test
        void shouldGetAverageTimeToCompleteForWeekToDateInSpecifiedTimezone() {
            when(clock.instant()).thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
            Application application1 = Application.builder()
                    .id("someId1")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(1))
                    .county(defaultCounty)
                    .completedAt(ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")))
                    .build();
            Application application2 = Application.builder()
                    .id("someId2")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(2))
                    .county(defaultCounty)
                    .completedAt(ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")))
                    .build();
            Application application3 = Application.builder()
                    .id("someId3")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(3))
                    .county(defaultCounty)
                    .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
                    .build();

            applicationRepository.save(application1);
            applicationRepository.save(application2);
            applicationRepository.save(application3);

            assertThat(applicationRepository.getAverageTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
                    .isEqualTo(Duration.ofDays(2).plusHours(12));
        }

        @Test
        void shouldGetAverageTimeToCompleteForWeekToDateInSpecifiedTimezone_whenNoApplicationIsFound() {
            assertThat(applicationRepository.getAverageTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
                    .isEqualTo(Duration.ofSeconds(0));
        }

        @Test
        void shouldGetMedianForWeekToDate() {
            when(clock.instant()).thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
            Application application1 = Application.builder()
                    .id("someId1")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(1))
                    .county(defaultCounty)
                    .completedAt(ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")))
                    .build();
            Application application2 = Application.builder()
                    .id("someId2")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(2))
                    .county(defaultCounty)
                    .completedAt(ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")))
                    .build();
            Application application3 = Application.builder()
                    .id("someId3")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(4))
                    .county(defaultCounty)
                    .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
                    .build();
            Application application4 = Application.builder()
                    .id("someId4")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(10))
                    .county(defaultCounty)
                    .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
                    .build();
            Application application5 = Application.builder()
                    .id("someId5")
                    .applicationData(new ApplicationData())
                    .timeToComplete(Duration.ofDays(20))
                    .county(defaultCounty)
                    .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
                    .build();

            applicationRepository.save(application1);
            applicationRepository.save(application2);
            applicationRepository.save(application3);
            applicationRepository.save(application4);
            applicationRepository.save(application5);

            assertThat(applicationRepository.getMedianTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
                    .isEqualTo(Duration.ofDays(7));
        }

        @Test
        void shouldGetMedianForWeekToDate_whenNoApplicationIsFound() {
            assertThat(applicationRepository.getMedianTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
                    .isEqualTo(Duration.ofSeconds(0));
        }
    }
}