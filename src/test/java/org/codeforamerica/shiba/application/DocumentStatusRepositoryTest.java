package org.codeforamerica.shiba.application;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.AbstractRepositoryTest;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class DocumentStatusRepositoryTest extends AbstractRepositoryTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private DocumentStatusRepository documentStatusRepository;

  private final RoutingDecisionService routingDecisionService = mock(RoutingDecisionService.class);
  private RoutingDestination routingDestination;
  private final FilenameGenerator filenameGenerator = mock(FilenameGenerator.class);
  private final PdfGenerator pdfGenerator = mock(PdfGenerator.class);

  @BeforeEach
  void setUp() {
    documentStatusRepository = new DocumentStatusRepository(jdbcTemplate,
        routingDecisionService, filenameGenerator, pdfGenerator );
    routingDestination = CountyRoutingDestination.builder().county(Olmsted)
        .build();
    when(routingDecisionService.getRoutingDestinations(any(ApplicationData.class),
        any(Document.class)))
        .thenReturn(List.of(routingDestination));
  }

  @Test
  void createOrUpdateShouldCreateOrUpdateStatusesFromAnIdDocTypeDestinationAndStatus() {
    documentStatusRepository.createOrUpdate("someId", CAF, "Hennepin", DELIVERED,"");
    documentStatusRepository.createOrUpdate("someId2", CAF, "Hennepin", DELIVERED,"");
    assertThat(documentStatusRepository.findAll("someId")).containsExactlyInAnyOrder(
        new DocumentStatus("someId", CAF, "Hennepin", DELIVERED,"")
    );
  }

  @Test
  void createOrUpdateAllDocumentsShouldChangeAllDocumentStatuses() {
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

    documentStatusRepository.createOrUpdateApplicationType(application, SENDING);
    List<DocumentStatus> resultingStatuses = documentStatusRepository.findAll(
        applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new DocumentStatus(applicationData.getId(), CAF, routingDestination.getName(),
            SENDING,""),
        new DocumentStatus(applicationData.getId(), CERTAIN_POPS, routingDestination.getName(),
            SENDING,"")
    );

    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(List.of("CCAP"));
    documentStatusRepository.createOrUpdateApplicationType(application, SENDING);
    resultingStatuses = documentStatusRepository.findAll(applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new DocumentStatus(applicationData.getId(), CCAP, routingDestination.getName(),
            SENDING,"")
    );
  }

  @Test
  void getApplicationStatusToResubmitShouldOnlyReturnFailedSubmissions() {
    documentStatusRepository.createOrUpdate("someId1", CAF, "Olmsted", DELIVERY_FAILED,"");
    documentStatusRepository.createOrUpdate("someId1", XML, "Olmsted", DELIVERED,"");

    documentStatusRepository.createOrUpdate("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED,"fileName");
    documentStatusRepository.createOrUpdate("someId2", CERTAIN_POPS, "Olmsted", SENDING,"");

    documentStatusRepository.createOrUpdate("someId3", CAF, "Olmsted", SENDING,"");
    documentStatusRepository.createOrUpdate("someId3", UPLOADED_DOC, "Olmsted", SENDING,"fileName");

    documentStatusRepository.createOrUpdate("someId4", CCAP, "Olmsted", DELIVERY_FAILED,"");

    List<DocumentStatus> failedApplications = documentStatusRepository.getDocumentStatusToResubmit();
    assertThat(failedApplications).containsExactlyInAnyOrder(
        new DocumentStatus("someId1", CAF, "Olmsted", DELIVERY_FAILED,""),
        new DocumentStatus("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED,"fileName"),
        new DocumentStatus("someId4", CCAP, "Olmsted", DELIVERY_FAILED,"")
    );
  }

  @Test
  void deleteApplicationStatusesNoLongerUsed() {
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
    documentStatusRepository.createOrUpdateApplicationType(application, SENDING);
    List<DocumentStatus> resultingStatuses = documentStatusRepository.findAll(
        applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new DocumentStatus(applicationData.getId(), CAF, routingDestination.getName(),
            SENDING,""),
        new DocumentStatus(applicationData.getId(), CERTAIN_POPS, routingDestination.getName(),
            SENDING,"")
    );

    ApplicationData applicationData2 = new TestApplicationDataBuilder()
        .base()
        .withApplicantPrograms(List.of("SNAP", "CCAP"))
        .build();

    application.setApplicationData(applicationData2);
    documentStatusRepository.createOrUpdateApplicationType(application, SENDING);
    List<DocumentStatus> resultingStatuses2 = documentStatusRepository.findAll(
        applicationData.getId());
    assertThat(resultingStatuses2).containsExactlyInAnyOrder(
        new DocumentStatus(applicationData.getId(), CAF, routingDestination.getName(),
            SENDING,""),
        new DocumentStatus(applicationData.getId(), CCAP, routingDestination.getName(),
            SENDING,"")
    );
  }
}
