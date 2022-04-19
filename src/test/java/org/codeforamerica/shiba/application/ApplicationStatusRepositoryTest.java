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
import org.codeforamerica.shiba.CountyMap;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

public class ApplicationStatusRepositoryTest extends AbstractRepositoryTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private DocumentStatusRepository documentStatusRepository;

  private final RoutingDecisionService routingDecisionService = mock(RoutingDecisionService.class);
  private CountyMap<CountyRoutingDestination> countyMap;
  @MockBean
  private FilenameGenerator filenameGenerator;
  private CountyRoutingDestination routingDestination;
  @MockBean
  private PdfGenerator pdfGenerator;

  @BeforeEach
  void setUp() {
    countyMap = new CountyMap<>();
    routingDestination = CountyRoutingDestination.builder()
        .county(Olmsted)
        .build();
    countyMap.setDefaultValue(routingDestination);
    filenameGenerator = new FilenameGenerator(countyMap);
    documentStatusRepository = new DocumentStatusRepository(jdbcTemplate,
        routingDecisionService, filenameGenerator, pdfGenerator );
    when(routingDecisionService.getRoutingDestinations(any(ApplicationData.class),
        any(Document.class)))
        .thenReturn(List.of(routingDestination));
  }

  @Test
  void createOrUpdateShouldCreateOrUpdateStatusesFromAnIdDocTypeDestinationAndStatus() {
    documentStatusRepository.createOrUpdate("someId", CAF, "Hennepin", DELIVERED, "sysGeneratedName");
    documentStatusRepository.createOrUpdate("someId2", CAF, "Hennepin", DELIVERED, "sysGeneratedName");
    assertThat(documentStatusRepository.findAll("someId")).containsExactlyInAnyOrder(
        new ApplicationStatus("someId", CAF, "Hennepin", DELIVERED, "sysGeneratedName")
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
    List<ApplicationStatus> resultingStatuses = documentStatusRepository.findAll(
        applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new ApplicationStatus(applicationData.getId(), CAF, routingDestination.getName(),
            SENDING,  documentStatusRepository.getAndSetFileNames(application,CAF).get(0)),
        new ApplicationStatus(applicationData.getId(), CERTAIN_POPS, routingDestination.getName(),
            SENDING, documentStatusRepository.getAndSetFileNames(application,CERTAIN_POPS).get(0))
    );

    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(List.of("CCAP"));
    documentStatusRepository.createOrUpdateApplicationType(application, SENDING);
    resultingStatuses = documentStatusRepository.findAll(applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new ApplicationStatus(applicationData.getId(), CCAP, routingDestination.getName(),
            SENDING, documentStatusRepository.getAndSetFileNames(application,CCAP).get(0))
    );
  }

  @Test
  void getApplicationStatusToResubmitShouldOnlyReturnFailedSubmissions() {
    documentStatusRepository.createOrUpdate("someId1", CAF, "Olmsted", DELIVERY_FAILED, "");
    documentStatusRepository.createOrUpdate("someId1", XML, "Olmsted", DELIVERED, "");

    documentStatusRepository.createOrUpdate("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED, "fileName");
    documentStatusRepository.createOrUpdate("someId2", CERTAIN_POPS, "Olmsted", SENDING, "");

    documentStatusRepository.createOrUpdate("someId3", CAF, "Olmsted", SENDING, "");
    documentStatusRepository.createOrUpdate("someId3", UPLOADED_DOC, "Olmsted", SENDING, "fileName");

    documentStatusRepository.createOrUpdate("someId4", CCAP, "Olmsted", DELIVERY_FAILED, "");

    List<ApplicationStatus> failedApplications = documentStatusRepository.getDocumentStatusToResubmit();
    assertThat(failedApplications).containsExactlyInAnyOrder(
        new ApplicationStatus("someId1", CAF, "Olmsted", DELIVERY_FAILED, ""),
        new ApplicationStatus("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED, "fileName"),
        new ApplicationStatus("someId4", CCAP, "Olmsted", DELIVERY_FAILED, "")
    );
  }
  
}
