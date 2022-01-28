package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.*;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AppsStuckInProgressResubmissionTest {

  @Autowired
  private ResubmissionService resubmissionService;

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private DocumentStatusRepository documentStatusRepository;

  @MockBean
  private PageEventPublisher pageEventPublisher;

  @Test
  void itTriggersAnEventForAppsStuckInProgress() throws Exception {
    String applicationIdToResubmit = makeApplicationThatShouldBeResubmitted();
    String applicationIdThatShouldNotBeResubmitted = makeInProgressApplicationThatShouldNotBeResubmitted();
    String applicationIdThatIsNotInProgress = makeApplicationThatWasDelivered();

    // actually try to resubmit it
    resubmissionService.resubmitFailedApplications();

    /// make sure that application had an applicationSubmittedEvent triggered
    verify(pageEventPublisher).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdToResubmit, FlowType.FULL,
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", applicationIdToResubmit,
            LocaleContextHolder.getLocale()));

    // Other applications should not have the event triggered
    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdThatShouldNotBeResubmitted,
            FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdThatIsNotInProgress,
            FlowType.FULL,
            LocaleContextHolder.getLocale()));

  }

  @NotNull
  private String makeApplicationThatShouldBeResubmitted() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    String applicationId = "resubmitMePlz";
    Application inProgressApplicationThatShouldBeCompleted = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(20)) // important that this is completed!!!
        .county(County.Anoka)
        .id(applicationId)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(inProgressApplicationThatShouldBeCompleted);

    // Save CAF
    documentStatusRepository.createOrUpdate(
        applicationId,
        Document.CAF,
        "Anoka",
        Status.IN_PROGRESS);

    documentStatusRepository.createOrUpdate(
        applicationId,
        Document.UPLOADED_DOC,
        "Anoka",
        Status.IN_PROGRESS);

    return applicationId;
  }

  @NotNull
  private String makeInProgressApplicationThatShouldNotBeResubmitted() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    String applicationId = "dontResubmitMePlz";
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
    return applicationId;
  }

  @NotNull
  private String makeApplicationThatWasDelivered() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    String applicationId = "alsoDontResubmitMePlz";
    Application inProgressApplicationThatShouldBeCompleted = Application.builder()
        .completedAt(ZonedDateTime.now().minusHours(24)) // important that this is completed!!!
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
        Status.DELIVERED);
    return applicationId;
  }

  // TODO uploaded doc submitted event

  // TODO xml is in progress????
}
