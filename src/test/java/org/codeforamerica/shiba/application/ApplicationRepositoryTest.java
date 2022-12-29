package org.codeforamerica.shiba.application;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Beltrami;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.County.Wright;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.Sentiment;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.testutilities.AbstractRepositoryTest;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;

class ApplicationRepositoryTest extends AbstractRepositoryTest {

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

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
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("somePage", "someInput", emptyList())
        .withSubworkflow("someGroup", new PagesDataBuilder()
            .withPageData("someGroupedPage", "someGroupedPageInput", "someGroupedPageValue"))
        .build();

    String contentType = "image/jpeg";
    String originalFilename = "originalFilename";
    MockMultipartFile image = new MockMultipartFile("image", originalFilename, contentType,
        "test".getBytes());
    applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", contentType);
    applicationData.setClientIP("192.168.0.0");

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
    assertThat(savedApplication.getApplicationStatuses()).isEmpty();

    ApplicationData savedApplicationData = savedApplication.getApplicationData();
    assertThat(savedApplicationData.getClientIP()).isEqualTo("192.168.0.0");

    UploadedDocument uploadedDoc = savedApplication.getApplicationData().getUploadedDocs().get(0);
    assertThat(uploadedDoc.getFilename()).isEqualTo(originalFilename);
    assertThat(uploadedDoc.getS3Filepath()).isEqualTo("someS3FilePath");
    assertThat(uploadedDoc.getThumbnailFilepath()).isEqualTo("someDataUrl");
    assertThat(uploadedDoc.getType()).isEqualTo(contentType);
    assertThat(uploadedDoc.getSize()).isEqualTo(4L);
  }

  @Test
  void shouldSaveApplicationWithOptionalFieldsPopulated() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("somePage", "someInput", emptyList()).build();

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
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("somePage", "someInput", emptyList()).build();

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

    ApplicationData updatedApplicationData = new TestApplicationDataBuilder()
        .withPageData("someUpdatedPage", "someUpdatedInput", emptyList()).build();
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
        .applicationStatuses(emptyList())
        .build();

    applicationRepository.save(updatedApplication);

    Application retrievedApplication = applicationRepository.find(applicationId);

    assertThat(retrievedApplication).usingRecursiveComparison()
        .ignoringFields("fileName", "updatedAt").isEqualTo(updatedApplication);
  }

  @Nested
  class EncryptionAndDecryptionTest extends AbstractRepositoryTest {

    ApplicationRepository applicationRepositoryWithMockEncryptor;
    @SuppressWarnings("unchecked")
    Encryptor<ApplicationData> mockEncryptor = mock(Encryptor.class);
    String jsonData = "\"{here: 'is the encrypted data'}\"";

    @BeforeEach
    void setUp() {
      applicationRepositoryWithMockEncryptor = new ApplicationRepository(jdbcTemplate,
          mockEncryptor);
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
      ApplicationData decryptedApplicationData = new TestApplicationDataBuilder()
          .withPageData("somePage", "someInput", "CASH").build();
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
      ApplicationData decryptedApplicationData = new TestApplicationDataBuilder()
          .withPageData("somePage", "someInput", "CASH").build();

      when(mockEncryptor.decrypt(any())).thenReturn(decryptedApplicationData);

      applicationRepositoryWithMockEncryptor.save(application);

      Application retrievedApplication = applicationRepositoryWithMockEncryptor.find(applicationId);
      assertThat(retrievedApplication.getApplicationData()).isEqualTo(decryptedApplicationData);
    }
  }

  @Test
  void shouldReturnAppsWithNoStatusesInCountyToResubmitInAlphabeticalCountyAndOldestFirst() {
    Application newestApplication = Application.builder()
        .id("1")
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS))
        .applicationData(new TestApplicationDataBuilder().base().build())
        .county(Anoka)
        .build();
    Application middleApplication = Application.builder()
        .id("2")
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS).minusDays(14))
        .applicationData(new TestApplicationDataBuilder().base().build())
        .county(Anoka)
        .build();
    Application oldestApplication = Application.builder()
        .id("3")
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS).minusMonths(1))
        .applicationData(new TestApplicationDataBuilder().base().build())
        .county(Olmsted)
        .build();
    applicationRepository.save(newestApplication);
    applicationRepository.save(middleApplication);
    applicationRepository.save(oldestApplication);

    assertThat(applicationRepository.findApplicationsWithBlankStatuses()
        .stream().map(Application::getId))
        .containsExactly("2", "1", "3");
  }

  @Test
  void shouldReturnAppsWithNoStatusesToResubmitInChronologicalOrderByOldestFirstAndCounty() {
    Application wrightApp = Application.builder()
        .id("1")
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS))
        .applicationData(new TestApplicationDataBuilder().base().build())
        .county(Wright)
        .build();
    Application newerAnokaApp = Application.builder()
        .id("3")
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS).minusDays(1))
        .applicationData(new TestApplicationDataBuilder().base().build())
        .county(Anoka)
        .build();
    Application beltramiApp = Application.builder()
        .id("5")
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS).minusDays(14))
        .applicationData(new TestApplicationDataBuilder().base().build())
        .county(Beltrami)
        .build();
    Application oldAnokaApp = Application.builder()
        .id("7")
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS).minusMonths(1))
        .applicationData(new TestApplicationDataBuilder().base().build())
        .county(Anoka)
        .build();

    List.of(wrightApp, newerAnokaApp, beltramiApp, oldAnokaApp)
        .forEach(application -> applicationRepository.save(application));

    assertThat(applicationRepository.findApplicationsWithBlankStatuses().stream().map(Application::getId)).containsExactly(
      oldAnokaApp.getId(), newerAnokaApp.getId(), beltramiApp.getId(), wrightApp.getId()
    );
  }
}
