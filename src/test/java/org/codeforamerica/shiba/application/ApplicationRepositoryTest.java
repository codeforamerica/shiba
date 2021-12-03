package org.codeforamerica.shiba.application;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.County.StLouis;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
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
    applicationData.setRoutingDestinationNames(List.of("White Earth", "Olmsted"));

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
    assertThat(savedApplication.getCafApplicationStatus()).isNull();
    assertThat(savedApplication.getCcapApplicationStatus()).isNull();
    assertThat(savedApplication.getCertainPopsApplicationStatus()).isNull();

    UploadedDocument uploadedDoc = savedApplication.getApplicationData().getUploadedDocs().get(0);
    assertThat(uploadedDoc.getFilename()).isEqualTo(originalFilename);
    assertThat(uploadedDoc.getS3Filepath()).isEqualTo("someS3FilePath");
    assertThat(uploadedDoc.getThumbnailFilepath()).isEqualTo("someDataUrl");
    assertThat(uploadedDoc.getType()).isEqualTo(contentType);
    assertThat(uploadedDoc.getSize()).isEqualTo(4L);

    assertThat(savedApplication.getApplicationData().getRoutingDestinationNames()).containsExactly(
        "White Earth", "Olmsted");
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

    applicationRepository.updateStatus("someId1", CAF, "Olmsted", DELIVERY_FAILED);
    applicationRepository.updateStatus("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED);
    // In Progress is NOT included
    applicationRepository.updateStatus("someId3", CAF, "Olmsted", IN_PROGRESS);
    // In Progress is NOT included
    applicationRepository.updateStatus("someId3", UPLOADED_DOC, "Olmsted", IN_PROGRESS);
    applicationRepository.updateStatus("someId4", CCAP, "Olmsted", DELIVERY_FAILED);

    List<ApplicationStatus> failedApplications = applicationRepository.getApplicationIdsToResubmit();
    assertThat(failedApplications).containsExactlyInAnyOrder(
        new ApplicationStatus("someId1", CAF, "Olmsted", DELIVERY_FAILED),
        new ApplicationStatus("someId4", CCAP, "Olmsted", DELIVERY_FAILED),
        new ApplicationStatus("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED)
    );
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

    applicationRepository.updateStatus(deliveredApplication.getId(), CAF, "Olmsted", DELIVERED);
    applicationRepository.updateStatus(deliveredApplication.getId(), CCAP, "Olmsted", DELIVERED);
    applicationRepository.updateStatus(deliveredApplication.getId(), UPLOADED_DOC, "Olmsted",
        DELIVERED);

    applicationRepository.updateStatus(sendingApplication.getId(), CAF, "Olmsted", SENDING);
    applicationRepository.updateStatus(sendingApplication.getId(), CCAP, "Olmsted", SENDING);
    applicationRepository.updateStatus(sendingApplication.getId(), UPLOADED_DOC, "Olmsted",
        SENDING);

    applicationRepository.updateStatus(inProgressApplication.getId(), CAF, "Olmsted", IN_PROGRESS);
    applicationRepository.updateStatus(inProgressApplication.getId(), CCAP, "Olmsted", IN_PROGRESS);
    applicationRepository.updateStatus(inProgressApplication.getId(), UPLOADED_DOC, "Olmsted",
        IN_PROGRESS);

    applicationRepository.updateStatus(incompleteApplication.getId(), CAF, "Olmsted", IN_PROGRESS);
    applicationRepository.updateStatus(incompleteApplication.getId(), CCAP, "Olmsted", IN_PROGRESS);
    applicationRepository.updateStatus(incompleteApplication.getId(), UPLOADED_DOC, "Olmsted",
        IN_PROGRESS);

    var failedApplications = applicationRepository.getApplicationIdsToResubmit();
    assertThat(failedApplications).isEmpty();
  }

  @Test
  void saveShouldUpdateApplicationStatuses() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .base()
        .withApplicantPrograms(List.of("SNAP", "CERTAIN_POPS"))
        .build();
    Application application = Application.builder()
        .id(applicationData.getId())
        .completedAt(ZonedDateTime.now(UTC).truncatedTo(ChronoUnit.MILLIS))
        .applicationData(applicationData)
        .county(Olmsted)
        .build();
    RoutingDecisionService routingDecisionService = mock(RoutingDecisionService.class);
    RoutingDestination routingDestination = CountyRoutingDestination.builder().county(Olmsted)
        .build();
    String routingDestName = routingDestination.getName();
    when(routingDecisionService.getRoutingDestinations(eq(applicationData), any(Document.class)))
        .thenReturn(List.of(routingDestination));
    applicationRepository.save(application);
    applicationRepository.updateStatusToInProgress(application, routingDecisionService);
    Application resultingApplication = applicationRepository.find(applicationData.getId());
    assertThat(resultingApplication.getApplicationStatus(CAF, routingDestName))
        .isEqualTo(IN_PROGRESS);
    assertThat(resultingApplication.getApplicationStatus(CERTAIN_POPS, routingDestName))
        .isEqualTo(IN_PROGRESS);
    assertThat(resultingApplication.getApplicationStatus(CCAP, routingDestName)).isNull();

    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(List.of("CCAP"));
    applicationRepository.updateStatusToInProgress(application, routingDecisionService);
    resultingApplication = applicationRepository.find(applicationData.getId());
    assertThat(resultingApplication.getApplicationStatus(CAF, routingDestName))
        .isEqualTo(IN_PROGRESS);
    assertThat(resultingApplication.getApplicationStatus(CERTAIN_POPS, routingDestName))
        .isEqualTo(IN_PROGRESS);
    assertThat(resultingApplication.getApplicationStatus(CCAP, routingDestName))
        .isEqualTo(IN_PROGRESS);
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
}
