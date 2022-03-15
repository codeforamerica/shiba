package org.codeforamerica.shiba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.application.Status.UNDELIVERABLE;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.DocumentStatus;
import org.codeforamerica.shiba.application.DocumentStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
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

  private final ZonedDateTime moreThan60DaysAgo = ZonedDateTime.now().withFixedOffsetZone().minusDays(60).minusNanos(1);
  private final ZonedDateTime tenHoursAgo = ZonedDateTime.now().withFixedOffsetZone().minusHours(10);

  @Test
  void itTriggersAnEventForAppsWithMissingStatuses() {
    when(featureFlagConfiguration.get("only-submit-blank-status-apps-from-olmsted")).thenReturn(
        FeatureFlag.OFF);
    for (int i = 0; i < 31; i++) {
      if (i == 0) {
        makeBlankStatusLaterDocApplication(Integer.toString(i), Hennepin, tenHoursAgo.plusMinutes(i), true);
      } else {
        makeBlankStatusApplication(Integer.toString(i), Hennepin, tenHoursAgo.plusMinutes(i));
      }
    }

    // actually try to resubmit it
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();

    // make sure that the first 30 applications had an applicationSubmittedEvent triggered
    for (int i = 1; i < 30; i++) {
      verify(pageEventPublisher).publish(
          new ApplicationSubmittedEvent("resubmission", String.valueOf(i), FlowType.FULL,
              LocaleContextHolder.getLocale()));
    }

    verify(pageEventPublisher).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", String.valueOf(0),
            LocaleContextHolder.getLocale()));

    // Other applications should not have the event triggered
    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", String.valueOf(0), FlowType.FULL,
            LocaleContextHolder.getLocale()));
  }

  @Test
  void itDoesNotTriggerAnEventForAppsThatShouldNotBeResubmitted() {
    when(featureFlagConfiguration.get("only-submit-blank-status-apps-from-olmsted")).thenReturn(
        FeatureFlag.OFF);
    String applicationIdToResubmit = makeBlankStatusApplication("1", Hennepin, tenHoursAgo).getId();
    String appWithExistingStatus = makeInProgressApplicationThatShouldNotBeResubmitted().getId();
    String appWithUploadedDocsOlderThan60Days = makeBlankStatusApplication("4", Anoka,
        moreThan60DaysAgo).getId();
    String laterDocsSubmissionOlderThan60Days = makeBlankStatusLaterDocApplication("5", Anoka,
        moreThan60DaysAgo, true).getId();

    // actually try to resubmit it
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();

    verify(pageEventPublisher).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdToResubmit, FlowType.FULL,
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", appWithExistingStatus,
            FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", appWithExistingStatus,
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher).publish(
        new ApplicationSubmittedEvent("resubmission", appWithUploadedDocsOlderThan60Days,
            FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", appWithUploadedDocsOlderThan60Days,
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", laterDocsSubmissionOlderThan60Days,
            LocaleContextHolder.getLocale()));
  }

  @Test
  void setsApplicationsWithDocsOlderThan60DaysAsUndeliverable() {
    when(featureFlagConfiguration.get("only-submit-blank-status-apps-from-olmsted")).thenReturn(
        FeatureFlag.OFF);
    String appWithUploadedDocsOlderThan60Days = makeBlankStatusApplication("48",
        Hennepin, moreThan60DaysAgo).getId();
    String laterDocsSubmissionOlderThan60Days = makeBlankStatusLaterDocApplication("51",
        Hennepin, moreThan60DaysAgo, true).getId();

    resubmissionService.resubmitBlankStatusApplicationsViaEsb();

    assertThat(documentStatusRepository.findAll(
        appWithUploadedDocsOlderThan60Days)).containsExactlyInAnyOrder(
        new DocumentStatus(appWithUploadedDocsOlderThan60Days, CAF, "Hennepin", SENDING),
        new DocumentStatus(appWithUploadedDocsOlderThan60Days, UPLOADED_DOC, "Hennepin",
            Status.UNDELIVERABLE)
    );
    assertThat(documentStatusRepository.findAll(
        laterDocsSubmissionOlderThan60Days)).containsExactlyInAnyOrder(
        new DocumentStatus(laterDocsSubmissionOlderThan60Days, UPLOADED_DOC, "Hennepin",
            Status.UNDELIVERABLE)
    );
  }

  @Test
  void shouldSetLaterDocsAppsWithNoDocumentsToUndeliverableAndNotPublishSubmissionEvents() {
    when(featureFlagConfiguration.get("only-submit-blank-status-apps-from-olmsted")).thenReturn(
        FeatureFlag.OFF);
    Application laterDocsWithoutDocuments = makeBlankStatusLaterDocApplication("60", Hennepin, ZonedDateTime.now(), false);
    Application laterDocsWithDocuments = makeBlankStatusLaterDocApplication("61", Hennepin, ZonedDateTime.now().minusMinutes(5), true);
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();
    assertThat(documentStatusRepository.findAll(laterDocsWithDocuments.getId())).contains(
        new DocumentStatus(laterDocsWithDocuments.getId(),UPLOADED_DOC, "Hennepin", SENDING)
    );
    verify(pageEventPublisher).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", laterDocsWithDocuments.getId(),
            LocaleContextHolder.getLocale()));
    assertThat(documentStatusRepository.findAll(laterDocsWithoutDocuments.getId())).contains(
        new DocumentStatus(laterDocsWithoutDocuments.getId(),UPLOADED_DOC, "Hennepin", UNDELIVERABLE)
    );
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", laterDocsWithoutDocuments.getId(),
            LocaleContextHolder.getLocale()));
  }

  @Test
  void ensureOnlyOlmstedAppsAreRetriggeredWhenFeatureFlagIsOn() {
    when(featureFlagConfiguration.get("only-submit-blank-status-apps-from-olmsted")).thenReturn(
        FeatureFlag.ON);
    Application olmstedApp = makeBlankStatusApplication("1", Olmsted, tenHoursAgo);
    Application olmstedDoc = makeBlankStatusLaterDocApplication("12", Olmsted, tenHoursAgo, true);
    Application notOlmstedApp = makeBlankStatusApplication("2", Anoka, tenHoursAgo);

    resubmissionService.resubmitBlankStatusApplicationsViaEsb();

    verify(pageEventPublisher).publish(
        new ApplicationSubmittedEvent("resubmission", olmstedApp.getId(), FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", olmstedDoc.getId(),
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", notOlmstedApp.getId(),
            FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", notOlmstedApp.getId(),
            LocaleContextHolder.getLocale()));
  }

  private Application makeBlankStatusLaterDocApplication(String id, County county,
      ZonedDateTime completedAt, boolean withDocs) {
    ApplicationData applicationData = withDocs ?
        new TestApplicationDataBuilder().withUploadedDocs() :
        new TestApplicationDataBuilder().build();
    applicationData.setId(id);
    applicationData.setFlow(LATER_DOCS);
    Application laterDocsApplication = Application.builder()
        .completedAt(completedAt) // important that this is completed!!!
        .county(county)
        .id(id)
        .applicationData(applicationData)
        .flow(LATER_DOCS)
        .build();
    applicationRepository.save(laterDocsApplication);
    return laterDocsApplication;
  }

  private Application makeBlankStatusApplication(String id, County county,
      ZonedDateTime createdAt) {

    ApplicationData applicationData = new TestApplicationDataBuilder().withUploadedDocs();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    applicationData.setId(id);

    Application applicationWithNoStatuses = Application.builder()
        .completedAt(createdAt) // important that this is completed!!!
        .county(county)
        .id(id)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(applicationWithNoStatuses);

    return applicationWithNoStatuses;
  }

  private Application makeInProgressApplicationThatShouldNotBeResubmitted() {
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
        CAF,
        "Anoka",
        Status.IN_PROGRESS);

    return inProgressApplicationThatShouldBeCompleted;
  }
}
