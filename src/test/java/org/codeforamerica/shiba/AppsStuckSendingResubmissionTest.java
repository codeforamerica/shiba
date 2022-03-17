package org.codeforamerica.shiba;

import static java.time.ZonedDateTime.now;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.DocumentStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.events.ApplicationSubmittedEvent;
import org.codeforamerica.shiba.pages.events.PageEventPublisher;
import org.codeforamerica.shiba.pages.events.UploadedDocumentsSubmittedEvent;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.jetbrains.annotations.NotNull;
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
class AppsStuckSendingResubmissionTest {

  @Autowired
  private ResubmissionService resubmissionService;

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private DocumentStatusRepository documentStatusRepository;

  @MockBean
  private PageEventPublisher pageEventPublisher;

  @Test
  void itTriggersAnEventFor50AppsStuckSending() {
    // Only the first 50 of these should be resubmitted.
    for (int i = 0; i < 51; i++) {
      makeApplicationThatShouldBeResubmitted(i);
    }

    // actually try to resubmit it
    resubmissionService.republishApplicationsInSendingStatus();

    // make sure that the first 50 applications had an applicationSubmittedEvent triggered
    for (int i = 0; i < 50; i++) {
      verify(pageEventPublisher).publish(
          new ApplicationSubmittedEvent("resubmission", String.valueOf(i), FlowType.FULL,
              LocaleContextHolder.getLocale()));
      verify(pageEventPublisher).publish(
          new UploadedDocumentsSubmittedEvent("resubmission", String.valueOf(i),
              LocaleContextHolder.getLocale()));
    }

    // Other applications should not have the event triggered
    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", String.valueOf(50), FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", String.valueOf(50),
            LocaleContextHolder.getLocale()));
  }

  @Test
  void itDoesNotTriggerAnEventForAppsThatShouldNotBeResubmitted() {
    String applicationIdToResubmit = makeApplicationThatShouldBeResubmitted(1);
    String applicationIdThatShouldNotBeResubmitted = makeSendingApplicationThatShouldNotBeResubmitted();
    String applicationIdThatWasDelivered = makeApplicationThatWasDelivered();

    // actually try to resubmit it
    resubmissionService.republishApplicationsInSendingStatus();

    verify(pageEventPublisher).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdToResubmit, FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", applicationIdToResubmit,
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdThatShouldNotBeResubmitted,
            FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", applicationIdThatShouldNotBeResubmitted,
            LocaleContextHolder.getLocale()));

    verify(pageEventPublisher, never()).publish(
        new ApplicationSubmittedEvent("resubmission", applicationIdThatWasDelivered,
            FlowType.FULL,
            LocaleContextHolder.getLocale()));
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", applicationIdThatWasDelivered,
            LocaleContextHolder.getLocale()));
  }

  @NotNull
  private String makeApplicationThatShouldBeResubmitted(int id) {
    String applicationId = String.valueOf(id);

    ApplicationData applicationData = new TestApplicationDataBuilder().withUploadedDocs();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());

    Application applicationStuckSending = Application.builder()
        .completedAt(now().minusHours(10)) // outside of 8hr window and should be resubmitted
        .county(Anoka)
        .id(applicationId)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(applicationStuckSending);

    // Save CAF
    documentStatusRepository.createOrUpdate(
        applicationId,
        Document.CAF,
        "Anoka",
        SENDING);

    documentStatusRepository.createOrUpdate(
        applicationId,
        UPLOADED_DOC,
        "Anoka",
        SENDING);

    return applicationId;
  }

  @NotNull
  private String makeSendingApplicationThatShouldNotBeResubmitted() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    String applicationId = "6";
    Application sendingApplicationThatShouldBeCompleted = Application.builder()
        .completedAt(now().minusHours(1)) // within 8hr window of apps that might be retrying
        .county(Anoka)
        .id(applicationId)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(sendingApplicationThatShouldBeCompleted);

    documentStatusRepository.createOrUpdate(
        applicationId,
        Document.CAF,
        "Anoka",
        SENDING);
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
    String applicationId = "7";
    Application sendingApplicationThatShouldBeCompleted = Application.builder()
        .completedAt(now().minusHours(12)) // outside of 8hr window of apps that are retrying
        .county(Anoka)
        .id(applicationId)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(sendingApplicationThatShouldBeCompleted);

    documentStatusRepository.createOrUpdate(
        applicationId,
        Document.CAF,
        "Anoka",
        DELIVERED);
    return applicationId;
  }
}
