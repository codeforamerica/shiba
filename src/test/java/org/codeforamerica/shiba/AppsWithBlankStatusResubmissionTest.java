package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Sherburne;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.DocumentStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@ActiveProfiles("test")
// drop all applications and application statuses before this test runs to avoid test pollution
@Sql(statements = {"TRUNCATE TABLE applications", "TRUNCATE TABLE application_status"})
class AppsWithBlankStatusResubmissionTest {

  @Autowired
  private ResubmissionService resubmissionService;

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private DocumentStatusRepository documentStatusRepository;

  @MockBean
  private PageEventPublisher pageEventPublisher;

  @MockBean
  private FeatureFlagConfiguration featureFlagConfiguration;

  @Test
  void itTriggersAnEventFor50AppsWithMissingStatuses() {
    when(featureFlagConfiguration.get("only-submit-blank-status-apps-from-sherburne")).thenReturn(
        FeatureFlag.OFF);
    for (int i = 0; i < 51; i++) {
      makeBlankStatusApplication(i, Hennepin);
    }

    // actually try to resubmit it
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();


    // make sure that the first 50 applications had an applicationSubmittedEvent triggered
    for (int i = 0; i < 50; i++) {
      verify(pageEventPublisher).publish(
          new ApplicationSubmittedEvent("resubmission", String.valueOf(i), FlowType.FULL,
              LocaleContextHolder.getLocale()));
    }

    // Other applications should not have the event triggered
    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", String.valueOf(50), FlowType.FULL,
            LocaleContextHolder.getLocale()));
  }

  
  @Test
  void itDoesNotTriggerAnEventForAppsThatShouldNotBeResubmitted() {
    when(featureFlagConfiguration.get("only-submit-blank-status-apps-from-sherburne")).thenReturn(
        FeatureFlag.OFF);
    String applicationIdToResubmit = makeBlankStatusApplication(1, Hennepin).getId();
    String applicationIdThatShouldNotBeResubmitted = makeInProgressApplicationThatShouldNotBeResubmitted().getId();

    // actually try to resubmit it
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();

    verify(pageEventPublisher).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdToResubmit, FlowType.FULL,
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdThatShouldNotBeResubmitted,
            FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", applicationIdThatShouldNotBeResubmitted,
            LocaleContextHolder.getLocale()));
  }

  @Test
  void ensureOnlySherburneAppsAreRetriggeredWhenFeatureFlagIsOn() {
    when(featureFlagConfiguration.get("only-submit-blank-status-apps-from-sherburne")).thenReturn(
        FeatureFlag.ON);
    Application sherburneApp = makeBlankStatusApplication(1, Sherburne);
    Application notSherburneApp = makeBlankStatusApplication(2, Anoka);

    resubmissionService.resubmitBlankStatusApplicationsViaEsb();

    verify(pageEventPublisher).publish(
        new ApplicationSubmittedEvent("resubmission", sherburneApp.getId(), FlowType.FULL,
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", notSherburneApp.getId(),
            FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", notSherburneApp.getId(),
            LocaleContextHolder.getLocale()));
  }

  private Application makeBlankStatusApplication(int id,
      County county) {
    String applicationId = String.valueOf(id);

    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    applicationData.setId(applicationId);

    Application applicationWithNoStatuses = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(10)) // important that this is completed!!!
        .county(county)
        .id(applicationId)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(applicationWithNoStatuses);

    return applicationWithNoStatuses;
  }

  private Application makeInProgressApplicationThatShouldNotBeResubmitted () {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    String applicationId = "1000";
    Application inProgressApplicationThatShouldBeCompleted = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(1)) // important that this is completed!!!
        .county(County.Anoka)
        .id(applicationId)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(inProgressApplicationThatShouldBeCompleted);

    documentStatusRepository.createOrUpdate(
        applicationId,
        Document.CAF,
        "Anoka",
        Status.IN_PROGRESS);

    return inProgressApplicationThatShouldBeCompleted;
  }
}
