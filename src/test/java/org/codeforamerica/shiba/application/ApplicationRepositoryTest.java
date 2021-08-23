package org.codeforamerica.shiba.application;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.County.Other;
import static org.codeforamerica.shiba.County.StLouis;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.AbstractRepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

class ApplicationRepositoryTest extends AbstractRepositoryTest {

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @MockBean
  private Clock clock;

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
    pageData.put("someInput", InputData.builder().value(emptyList()).build());
    applicationData.setPagesData(new PagesData(Map.of("somePage", pageData)));
    Subworkflows subworkflows = new Subworkflows();
    PagesData subflowIteration = new PagesData();
    PageData groupedPage = new PageData();
    groupedPage.put("someGroupedPageInput",
        InputData.builder().value(List.of("someGroupedPageValue")).build());
    subflowIteration.put("someGroupedPage", groupedPage);
    subworkflows.addIteration("someGroup", subflowIteration);
    applicationData.setSubworkflows(subworkflows);

    MockMultipartFile image = new MockMultipartFile("image", "test".getBytes());
    applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");

    Application application = Application.builder()
        .id("someid")
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS))
        .applicationData(applicationData)
        .county(Olmsted)
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

  @Test
  void shouldSaveApplicationWithOptionalFieldsPopulated() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesData(Map.of("somePage",
        new PageData(Map.of("someInput", InputData.builder().value(emptyList()).build())))));

    Application application = Application.builder()
        .id("someid")
        .completedAt(ZonedDateTime.now(UTC))
        .applicationData(applicationData)
        .county(Olmsted)
        .timeToComplete(Duration.ofSeconds(12415))
        .sentiment(Sentiment.HAPPY)
        .feedback("so so happy")
        .flow(FlowType.FULL)
        .build();

    applicationRepository.save(application);

    Application savedApplication = applicationRepository.find("someid");
    assertThat(savedApplication.getSentiment()).isEqualTo(application.getSentiment());
    assertThat(savedApplication.getFeedback()).isEqualTo(application.getFeedback());
    assertThat(savedApplication.getFlow()).isEqualTo(FlowType.FULL);
  }

  @Test
  void shouldUpdateExistingApplication() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesData(Map.of(
        "somePage",
        new PageData(Map.of("someInput", InputData.builder().value(emptyList()).build()))
    )));

    String applicationId = "someid";
    Application application = Application.builder()
        .id(applicationId)
        .applicationData(applicationData)
        .county(Olmsted)
        .timeToComplete(Duration.ofSeconds(12415))
        .sentiment(Sentiment.MEH)
        .feedback("someFeedback")
        .flow(FlowType.FULL)
        .build();

    applicationRepository.save(application);

    ApplicationData updatedApplicationData = new ApplicationData();
    updatedApplicationData.setPagesData(new PagesData(Map.of(
        "someUpdatedPage",
        new PageData(Map.of("someUpdatedInput", InputData.builder().value(emptyList()).build()))
    )));
    ZonedDateTime completedAt = ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS);
    Application updatedApplication = Application.builder()
        .id(application.getId())
        .completedAt(completedAt)
        .applicationData(updatedApplicationData)
        .county(Hennepin)
        .timeToComplete(Duration.ofSeconds(421))
        .sentiment(Sentiment.HAPPY)
        .feedback("someUpdatedFeedback")
        .flow(FlowType.EXPEDITED)
        .build();

    applicationRepository.save(updatedApplication);
    ZonedDateTime expectedUpdatedAt = ZonedDateTime.now(UTC);

    Application retrievedApplication = applicationRepository.find(applicationId);

    assertThat(retrievedApplication).usingRecursiveComparison()
        .ignoringFields("fileName", "updatedAt").isEqualTo(updatedApplication);
    assertThat(retrievedApplication.getUpdatedAt()).isBetween(completedAt, expectedUpdatedAt);
  }

  @Test
  void shouldReturnApplicationIdsOfDocumentsToResubmit() {
    Application application1 = Application.builder()
        .id("someId1")
        .applicationData(new ApplicationData())
        .timeToComplete(Duration.ofSeconds(1))
        .county(Olmsted)
        .flow(FlowType.FULL)
        .completedAt(ZonedDateTime.now(UTC).minusDays(2)) // 2 days ago
        .build();
    Application application2 = Application.builder()
        .id("someId2")
        .applicationData(new ApplicationData())
        .timeToComplete(Duration.ofSeconds(1))
        .county(Hennepin)
        .flow(FlowType.FULL)
        .completedAt(ZonedDateTime.now(UTC))
        .build();
    Application application3 = Application.builder()
        .id("someId3")
        .applicationData(new ApplicationData())
        .timeToComplete(Duration.ofSeconds(1))
        .county(Anoka)
        .flow(FlowType.FULL)
        .completedAt(ZonedDateTime.now(UTC)
            .minusDays(2)) // 2 days ago should be included, we no longer limit to last 24 hours
        .build();
    Application application4 = Application.builder()
        .id("someId4")
        .applicationData(new ApplicationData())
        .timeToComplete(Duration.ofSeconds(1))
        .county(Anoka)
        .flow(FlowType.FULL)
        .completedAt(ZonedDateTime.now(UTC))
        .build();

    applicationRepository.save(application1);
    applicationRepository.save(application2);
    applicationRepository.save(application3);
    applicationRepository.save(application4);

    applicationRepository.updateStatus("someId1", CAF, DELIVERY_FAILED);
    applicationRepository.updateStatus("someId2", UPLOADED_DOC, DELIVERY_FAILED);
    // In Progress is NOT included
    applicationRepository.updateStatus("someId3", CAF, IN_PROGRESS);
    // In Progress is NOT included
    applicationRepository.updateStatus("someId3", UPLOADED_DOC, IN_PROGRESS);
    applicationRepository.updateStatus("someId4", CCAP, DELIVERY_FAILED);

    Map<Document, List<String>> failedApplications = applicationRepository
        .getApplicationIdsToResubmit();
    assertThat(failedApplications.get(CAF)).containsExactlyInAnyOrder("someId1");
    assertThat(failedApplications.get(CCAP)).containsExactlyInAnyOrder("someId4");
    assertThat(failedApplications.get(UPLOADED_DOC)).containsExactlyInAnyOrder("someId2");
  }

  @Test
  void shouldLimitTheNumberOfResubmissions() {
    IntStream.range(0, 30).forEach(i -> {
          var id = "someId" + i;
          applicationRepository.save(Application.builder()
              .id(id)
              .applicationData(new ApplicationData())
              .timeToComplete(Duration.ofSeconds(1))
              .county(Olmsted)
              .flow(FlowType.FULL)
              .completedAt(ZonedDateTime.now(UTC).minusDays(2)) // 2 days ago
              .build());
          applicationRepository.updateStatus(id, CAF, DELIVERY_FAILED);
          applicationRepository.updateStatus(id, CCAP, DELIVERY_FAILED);
          applicationRepository.updateStatus(id, UPLOADED_DOC, DELIVERY_FAILED);
        }
    );

    Map<Document, List<String>> failedApplications = applicationRepository
        .getApplicationIdsToResubmit();
    assertThat(failedApplications.get(CCAP)).hasSize(10);
    assertThat(failedApplications.get(CAF)).hasSize(10);
    assertThat(failedApplications.get(UPLOADED_DOC)).hasSize(5);
  }

  @Test
  void shouldReturnNothingWhenThereAreNoDocumentsToResubmit() {
    var deliveredApplication = Application.builder()
        .id("someId1")
        .applicationData(new ApplicationData())
        .timeToComplete(Duration.ofSeconds(1))
        .county(Olmsted)
        .flow(FlowType.FULL)
        .completedAt(ZonedDateTime.now(UTC)) // Today
        .build();
    var sendingApplication = Application.builder()
        .id("someId2")
        .applicationData(new ApplicationData())
        .timeToComplete(Duration.ofSeconds(1))
        .county(Hennepin)
        .flow(FlowType.FULL)
        .completedAt(ZonedDateTime.now(UTC))
        .build();
    var inProgressApplication = Application.builder()
        .id("someId3")
        .applicationData(new ApplicationData())
        .timeToComplete(Duration.ofSeconds(1))
        .county(Hennepin)
        .flow(FlowType.FULL)
        .completedAt(ZonedDateTime.now(UTC))
        .build();
    var incompleteApplication = Application.builder()
        .id("someId4")
        .applicationData(new ApplicationData())
        .county(StLouis)
        .flow(FlowType.MINIMUM)
        .completedAt(null)
        .build();

    applicationRepository.save(deliveredApplication);
    applicationRepository.save(sendingApplication);
    applicationRepository.save(inProgressApplication);
    applicationRepository.save(incompleteApplication);

    applicationRepository.updateStatus(deliveredApplication.getId(), CAF, DELIVERED);
    applicationRepository.updateStatus(deliveredApplication.getId(), CCAP, DELIVERED);
    applicationRepository.updateStatus(deliveredApplication.getId(), UPLOADED_DOC, DELIVERED);

    applicationRepository.updateStatus(sendingApplication.getId(), CAF, SENDING);
    applicationRepository.updateStatus(sendingApplication.getId(), CCAP, SENDING);
    applicationRepository.updateStatus(sendingApplication.getId(), UPLOADED_DOC, SENDING);

    applicationRepository.updateStatus(inProgressApplication.getId(), CAF, IN_PROGRESS);
    applicationRepository.updateStatus(inProgressApplication.getId(), CCAP, IN_PROGRESS);
    applicationRepository.updateStatus(inProgressApplication.getId(), UPLOADED_DOC, IN_PROGRESS);

    applicationRepository.updateStatus(incompleteApplication.getId(), CAF, IN_PROGRESS);
    applicationRepository.updateStatus(incompleteApplication.getId(), CCAP, IN_PROGRESS);
    applicationRepository.updateStatus(incompleteApplication.getId(), UPLOADED_DOC, IN_PROGRESS);

    var failedApplications = applicationRepository.getApplicationIdsToResubmit();
    assertThat(failedApplications.get(CAF)).isEmpty();
    assertThat(failedApplications.get(CCAP)).isEmpty();
    assertThat(failedApplications.get(UPLOADED_DOC)).isEmpty();
  }

  @Nested
  class EncryptionAndDecryption extends AbstractRepositoryTest {

    ApplicationRepository applicationRepositoryWithMockEncryptor;
    @SuppressWarnings("unchecked")
    Encryptor<ApplicationData> mockEncryptor = mock(Encryptor.class);
    String jsonData = "\"{here: 'is the encrypted data'}\"";

    @BeforeEach
    void setUp() {
      applicationRepositoryWithMockEncryptor = new ApplicationRepository(jdbcTemplate,
          mockEncryptor, clock);
      when(mockEncryptor.encrypt(any())).thenReturn(jsonData);
    }

    @Test
    void shouldEncryptApplicationData() {
      ApplicationData applicationData = new ApplicationData();
      Application application = Application.builder()
          .id("someid")
          .completedAt(ZonedDateTime.now(UTC))
          .applicationData(applicationData)
          .county(Olmsted)
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
          .completedAt(ZonedDateTime.now(UTC))
          .applicationData(applicationData)
          .county(Olmsted)
          .timeToComplete(Duration.ofSeconds(1))
          .build();

      applicationRepositoryWithMockEncryptor.save(application);

      String actualEncryptedData = jdbcTemplate.queryForObject(
          "SELECT application_data " +
              "FROM applications " +
              "WHERE id = 'someid'", String.class);
      assertThat(actualEncryptedData).isEqualTo(jsonData);
    }

    @Test
    void shouldDecryptApplicationData() {
      ApplicationData applicationData = new ApplicationData();
      String applicationId = "someid";
      Application application = Application.builder()
          .id(applicationId)
          .completedAt(ZonedDateTime.now(UTC))
          .applicationData(applicationData)
          .county(Olmsted)
          .timeToComplete(Duration.ofSeconds(1))
          .build();
      ApplicationData decryptedApplicationData = new ApplicationData();
      decryptedApplicationData.setPagesData(new PagesData(Map.of("somePage",
          new PageData(Map.of("someInput", InputData.builder().value(List.of("CASH")).build())))));
      when(mockEncryptor.decrypt(any())).thenReturn(decryptedApplicationData);

      applicationRepositoryWithMockEncryptor.save(application);

      applicationRepositoryWithMockEncryptor.find(applicationId);

      verify(mockEncryptor).decrypt(jsonData);
    }

    @Test
    void shouldUseDecryptedApplicationDataForTheRetrievedApplication() {
      ApplicationData applicationData = new ApplicationData();
      applicationData.setPagesData(new PagesData(Map.of("somePage", new PageData())));
      String applicationId = "someid";
      Application application = Application.builder()
          .id(applicationId)
          .completedAt(ZonedDateTime.now(UTC))
          .applicationData(applicationData)
          .county(Olmsted)
          .timeToComplete(Duration.ofSeconds(1))
          .build();
      ApplicationData decryptedApplicationData = new ApplicationData();
      decryptedApplicationData.setPagesData(new PagesData(Map.of("somePage",
          new PageData(Map.of("someInput", InputData.builder().value(List.of("CASH")).build())))));

      when(mockEncryptor.decrypt(any())).thenReturn(decryptedApplicationData);

      applicationRepositoryWithMockEncryptor.save(application);

      Application retrievedApplication = applicationRepositoryWithMockEncryptor.find(applicationId);
      assertThat(retrievedApplication.getApplicationData()).isEqualTo(decryptedApplicationData);
    }
  }

  @Nested
  class MetricsQueries extends AbstractRepositoryTest {

    County defaultCounty = County.Other;

    ZonedDateTime defaultCompletedAt = ZonedDateTime.now(UTC);

    Duration defaultDuration = Duration.ofMinutes(14);

    @Test
    void shouldCalculateMedianTimeToComplete() {
      Application application1 = Application.builder()
          .id("someId1")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(1))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application2 = Application.builder()
          .id("someId2")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(2))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application3 = Application.builder()
          .id("someId3")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(3))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application4 = Application.builder()
          .id("someId4")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(4))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();

      applicationRepository.save(application1);
      applicationRepository.save(application2);
      applicationRepository.save(application3);
      applicationRepository.save(application4);

      assertThat(applicationRepository.getMedianTimeToComplete())
          .isEqualTo(Duration.ofDays(2).plusHours(12));
    }

    @Test
    void shouldReturn0ForMedianTimeToCompleteWhenThereIsNoEntries() {
      assertThat(applicationRepository.getMedianTimeToComplete()).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldGetCountOfNonLaterDocsSubmissions() {
      Application application1 = Application.builder()
          .id("someId1")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application2 = Application.builder()
          .id("someId2")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application3 = Application.builder()
          .id("someId3")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application4 = Application.builder()
          .id("someId4")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application5 = Application.builder()
          .id("someId5")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(defaultCounty)
          .flow(FlowType.LATER_DOCS)
          .completedAt(defaultCompletedAt)
          .build();

      applicationRepository.save(application1);
      applicationRepository.save(application2);
      applicationRepository.save(application3);
      applicationRepository.save(application4);
      applicationRepository.save(application5);

      assertThat(applicationRepository.count()).isEqualTo(4);
    }

    @Test
    void shouldGetCountByCounty() {
      Application application1 = Application.builder()
          .id("someId1")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(Olmsted)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application2 = Application.builder()
          .id("someId2")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application3 = Application.builder()
          .id("someId3")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(Hennepin)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application4 = Application.builder()
          .id("someId4")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(Hennepin)
          .flow(FlowType.FULL)
          .completedAt(defaultCompletedAt)
          .build();
      Application application5 = Application.builder()
          .id("someId5")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(Hennepin)
          .flow(FlowType.LATER_DOCS)
          .completedAt(defaultCompletedAt)
          .build();

      applicationRepository.save(application1);
      applicationRepository.save(application2);
      applicationRepository.save(application3);
      applicationRepository.save(application4);
      applicationRepository.save(application5);

      assertThat(applicationRepository.countByCounty()).isEqualTo(
          Map.of(
              Hennepin, 2,
              Olmsted, 1,
              Other, 1
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
      when(clock.instant())
          .thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
      Application application1 = Application.builder()
          .id("someId1")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(Olmsted)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")))
          .build();
      Application application2 = Application.builder()
          .id("someId2")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(Olmsted)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")))
          .build();
      Application application3 = Application.builder()
          .id("someId3")
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(Hennepin)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
          .build();

      applicationRepository.save(application1);
      applicationRepository.save(application2);
      applicationRepository.save(application3);

      assertThat(applicationRepository.countByCountyWeekToDate(ZoneId.of("America/Chicago")))
          .isEqualTo(
              Map.of(
                  Hennepin, 1,
                  Olmsted, 1
              )
          );
    }

    @Test
    void shouldGetAverageTimeToCompleteForWeekToDateInSpecifiedTimezone() {
      when(clock.instant())
          .thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
      Application application1 = Application.builder()
          .id("someId1")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(1))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")))
          .build();
      Application application2 = Application.builder()
          .id("someId2")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(2))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")))
          .build();
      Application application3 = Application.builder()
          .id("someId3")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(3))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
          .build();

      applicationRepository.save(application1);
      applicationRepository.save(application2);
      applicationRepository.save(application3);

      assertThat(
          applicationRepository.getAverageTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
          .isEqualTo(Duration.ofDays(2).plusHours(12));
    }

    @Test
    void shouldGetAverageTimeToCompleteForWeekToDateInSpecifiedTimezone_whenNoApplicationIsFound() {
      when(clock.instant()).thenReturn(Instant.now());
      assertThat(
          applicationRepository.getAverageTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
          .isEqualTo(Duration.ofSeconds(0));
    }

    @Test
    void shouldGetMedianForWeekToDate() {
      when(clock.instant())
          .thenReturn(LocalDate.of(2020, 1, 1).atStartOfDay(ZoneId.of("UTC")).toInstant());
      Application application1 = Application.builder()
          .id("someId1")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(1))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 29, 5, 59, 59, 0, ZoneId.of("UTC")))
          .build();
      Application application2 = Application.builder()
          .id("someId2")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(2))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 29, 6, 0, 0, 0, ZoneId.of("UTC")))
          .build();
      Application application3 = Application.builder()
          .id("someId3")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(4))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
          .build();
      Application application4 = Application.builder()
          .id("someId4")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(10))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
          .build();
      Application application5 = Application.builder()
          .id("someId5")
          .applicationData(new ApplicationData())
          .timeToComplete(Duration.ofDays(20))
          .county(defaultCounty)
          .flow(FlowType.FULL)
          .completedAt(ZonedDateTime.of(2019, 12, 31, 17, 59, 59, 0, ZoneId.of("UTC")))
          .build();

      applicationRepository.save(application1);
      applicationRepository.save(application2);
      applicationRepository.save(application3);
      applicationRepository.save(application4);
      applicationRepository.save(application5);

      assertThat(
          applicationRepository.getMedianTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
          .isEqualTo(Duration.ofDays(7));
    }

    @Test
    void shouldGetMedianForWeekToDate_whenNoApplicationIsFound() {
      when(clock.instant()).thenReturn(Instant.now());
      assertThat(
          applicationRepository.getMedianTimeToCompleteWeekToDate(ZoneId.of("America/Chicago")))
          .isEqualTo(Duration.ofSeconds(0));
    }

    @Test
    void shouldCalculateTheSentimentDistribution() {
      Application.ApplicationBuilder applicationBuilder = Application.builder()
          .applicationData(new ApplicationData())
          .timeToComplete(defaultDuration)
          .county(defaultCounty)
          .completedAt(defaultCompletedAt);
      Application application1 = applicationBuilder.id("id1").sentiment(Sentiment.HAPPY).build();
      Application application2 = applicationBuilder.id("id2").sentiment(Sentiment.HAPPY).build();
      Application application3 = applicationBuilder.id("id3").sentiment(Sentiment.MEH).build();
      Application application4 = applicationBuilder.id("id4").sentiment(Sentiment.SAD).build();
      Application application5 = applicationBuilder.id("id5").sentiment(null).build();

      applicationRepository.save(application1);
      applicationRepository.save(application2);
      applicationRepository.save(application3);
      applicationRepository.save(application4);
      applicationRepository.save(application5);

      assertThat(applicationRepository.getSentimentDistribution()).isEqualTo(Map.of(
          Sentiment.HAPPY, 0.5,
          Sentiment.MEH, 0.25,
          Sentiment.SAD, 0.25));
    }

  }
}
