package org.codeforamerica.shiba.application;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.IN_PROGRESS;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Document.XML;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.AbstractRepositoryTest;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class ApplicationStatusRepositoryTest extends AbstractRepositoryTest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private ApplicationStatusRepository applicationStatusRepository;

  private final RoutingDecisionService routingDecisionService = mock(RoutingDecisionService.class);
  private RoutingDestination routingDestination;

  @BeforeEach
  void setUp() {
    applicationStatusRepository = new ApplicationStatusRepository(jdbcTemplate,
        routingDecisionService);
    routingDestination = CountyRoutingDestination.builder().county(Olmsted)
        .build();
    when(routingDecisionService.getRoutingDestinations(any(ApplicationData.class),
        any(Document.class)))
        .thenReturn(List.of(routingDestination));
  }

  @Test
  void createOrUpdateShouldCreateOrUpdateStatusesFromAnIdDocTypeDestinationAndStatus() {
    applicationStatusRepository.createOrUpdate("someId", CAF, "Hennepin", DELIVERED);
    applicationStatusRepository.createOrUpdate("someId2", CAF, "Hennepin", DELIVERED);
    assertThat(applicationStatusRepository.findAll("someId")).containsExactlyInAnyOrder(
        new ApplicationStatus("someId", CAF, "Hennepin", DELIVERED)
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

    applicationStatusRepository.createOrUpdateAllDocuments(application, IN_PROGRESS);
    List<ApplicationStatus> resultingStatuses = applicationStatusRepository.findAll(
        applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new ApplicationStatus(applicationData.getId(), CAF, routingDestination.getName(),
            IN_PROGRESS),
        new ApplicationStatus(applicationData.getId(), CERTAIN_POPS, routingDestination.getName(),
            IN_PROGRESS)
    );

    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(List.of("CCAP"));
    applicationStatusRepository.createOrUpdateAllDocuments(application, IN_PROGRESS);
    resultingStatuses = applicationStatusRepository.findAll(applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new ApplicationStatus(applicationData.getId(), CAF, routingDestination.getName(),
            IN_PROGRESS),
        new ApplicationStatus(applicationData.getId(), CERTAIN_POPS, routingDestination.getName(),
            IN_PROGRESS),
        new ApplicationStatus(applicationData.getId(), CCAP, routingDestination.getName(),
            IN_PROGRESS)
    );
  }

  @Test
  void getApplicationStatusToResubmitShouldOnlyReturnFailedSubmissions() {
    applicationStatusRepository.createOrUpdate("someId1", CAF, "Olmsted", DELIVERY_FAILED);
    applicationStatusRepository.createOrUpdate("someId1", XML, "Olmsted", DELIVERED);

    applicationStatusRepository.createOrUpdate("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED);
    applicationStatusRepository.createOrUpdate("someId2", CERTAIN_POPS, "Olmsted", SENDING);

    applicationStatusRepository.createOrUpdate("someId3", CAF, "Olmsted", IN_PROGRESS);
    applicationStatusRepository.createOrUpdate("someId3", UPLOADED_DOC, "Olmsted", IN_PROGRESS);

    applicationStatusRepository.createOrUpdate("someId4", CCAP, "Olmsted", DELIVERY_FAILED);

    List<ApplicationStatus> failedApplications = applicationStatusRepository.getApplicationStatusToResubmit();
    assertThat(failedApplications).containsExactlyInAnyOrder(
        new ApplicationStatus("someId1", CAF, "Olmsted", DELIVERY_FAILED),
        new ApplicationStatus("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED),
        new ApplicationStatus("someId4", CCAP, "Olmsted", DELIVERY_FAILED)
    );
  }
}
