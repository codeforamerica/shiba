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
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
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

  private ApplicationStatusRepository applicationStatusRepository;

  private final RoutingDecisionService routingDecisionService = mock(RoutingDecisionService.class);
  @MockBean
  private FilenameGenerator filenameGenerator;
  private CountyRoutingDestination routingDestination;
  @MockBean
  private PdfGenerator pdfGenerator;

  @BeforeEach
  void setUp() {
    CountyMap<CountyRoutingDestination> countyMap = new CountyMap<>();
    routingDestination = new CountyRoutingDestination(Olmsted, "dpi", "email", "phoneNumber");
    countyMap.setDefaultValue(routingDestination);
    SnapExpeditedEligibilityDecider decider = mock(SnapExpeditedEligibilityDecider.class);
    filenameGenerator = new FilenameGenerator(countyMap, decider);
    applicationStatusRepository = new ApplicationStatusRepository(jdbcTemplate,
        routingDecisionService, filenameGenerator, pdfGenerator );
    when(routingDecisionService.getRoutingDestinations(any(ApplicationData.class),
        any(Document.class)))
        .thenReturn(List.of(routingDestination));
  }

  @Test
  void createOrUpdateShouldCreateOrUpdateStatusesFromAnIdDocTypeDestinationAndStatus() {
    applicationStatusRepository.createOrUpdate("someId", CAF, "Hennepin", DELIVERED, "sysGeneratedName");
    applicationStatusRepository.createOrUpdate("someId2", CAF, "Hennepin", DELIVERED, "sysGeneratedName");
    assertThat(applicationStatusRepository.findAll("someId")).containsExactlyInAnyOrder(
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

    applicationStatusRepository.createOrUpdateApplicationType(application, SENDING);
    List<ApplicationStatus> resultingStatuses = applicationStatusRepository.findAll(
        applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new ApplicationStatus(applicationData.getId(), CAF, routingDestination.getName(),
            SENDING,  applicationStatusRepository.getAndSetFileNames(application,CAF).get(0)),
        new ApplicationStatus(applicationData.getId(), CERTAIN_POPS, routingDestination.getName(),
            SENDING, applicationStatusRepository.getAndSetFileNames(application,CERTAIN_POPS).get(0))
    );

    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(List.of("CCAP"));
    applicationStatusRepository.createOrUpdateApplicationType(application, SENDING);
    resultingStatuses = applicationStatusRepository.findAll(applicationData.getId());
    assertThat(resultingStatuses).containsExactlyInAnyOrder(
        new ApplicationStatus(applicationData.getId(), CCAP, routingDestination.getName(),
            SENDING, applicationStatusRepository.getAndSetFileNames(application,CCAP).get(0))
    );
  }

  @Test
  void getApplicationStatusToResubmitShouldOnlyReturnFailedSubmissions() {
    applicationStatusRepository.createOrUpdate("someId1", CAF, "Olmsted", DELIVERY_FAILED, "");
    applicationStatusRepository.createOrUpdate("someId1", XML, "Olmsted", DELIVERED, "");

    applicationStatusRepository.createOrUpdate("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED, "fileName");
    applicationStatusRepository.createOrUpdate("someId2", CERTAIN_POPS, "Olmsted", SENDING, "");

    applicationStatusRepository.createOrUpdate("someId3", CAF, "Olmsted", SENDING, "");
    applicationStatusRepository.createOrUpdate("someId3", UPLOADED_DOC, "Olmsted", SENDING, "fileName");

    applicationStatusRepository.createOrUpdate("someId4", CCAP, "Olmsted", DELIVERY_FAILED, "");

    List<ApplicationStatus> failedApplications = applicationStatusRepository.getDocumentStatusToResubmit();
    assertThat(failedApplications).containsExactlyInAnyOrder(
        new ApplicationStatus("someId1", CAF, "Olmsted", DELIVERY_FAILED, ""),
        new ApplicationStatus("someId2", UPLOADED_DOC, "Olmsted", DELIVERY_FAILED, "fileName"),
        new ApplicationStatus("someId4", CCAP, "Olmsted", DELIVERY_FAILED, "")
    );
  }
  
}
