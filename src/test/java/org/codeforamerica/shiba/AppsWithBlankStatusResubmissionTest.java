package org.codeforamerica.shiba;

import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.DocumentStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.Status;
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

  @Test
  void itTriggersAnEventFor50AppsWithMissingStatuses() {

    for (int i = 0; i < 51; i++) {
      makeApplicationThatShouldBeResubmitted(i);
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

  
//  @Test
//  void itDoesNotTriggerAnEventForAppsThatShouldNotBeResubmitted() {
//    String applicationIdToResubmit = makeApplicationThatShouldBeResubmitted(1);
//    String applicationIdThatShouldNotBeResubmitted = makeInProgressApplicationThatShouldNotBeResubmitted();
//    String applicationIdThatIsNotInProgress = makeApplicationThatWasDelivered();
//
//    // actually try to resubmit it
//    resubmissionService.resubmitInProgressAndSendingApplicationsViaEsb();
//
//    verify(pageEventPublisher).publish(
//        new ApplicationSubmittedEvent("resubmission", applicationIdToResubmit, FlowType.FULL,
//            LocaleContextHolder.getLocale()));
//    verify(pageEventPublisher).publish(
//        new UploadedDocumentsSubmittedEvent("resubmission", applicationIdToResubmit,
//            LocaleContextHolder.getLocale()));
//
//    verify(pageEventPublisher, never()).publish(
//        new ApplicationSubmittedEvent("resubmission", applicationIdThatShouldNotBeResubmitted,
//            FlowType.FULL,
//            LocaleContextHolder.getLocale()));
//    verify(pageEventPublisher, never()).publish(
//        new UploadedDocumentsSubmittedEvent("resubmission", applicationIdThatShouldNotBeResubmitted,
//            LocaleContextHolder.getLocale()));
//
//    verify(pageEventPublisher, never()).publish(
//        new ApplicationSubmittedEvent("resubmission", applicationIdThatIsNotInProgress,
//            FlowType.FULL,
//            LocaleContextHolder.getLocale()));
//    verify(pageEventPublisher, never()).publish(
//        new UploadedDocumentsSubmittedEvent("resubmission", applicationIdThatIsNotInProgress,
//            LocaleContextHolder.getLocale()));
//  }

  @NotNull
  private String makeApplicationThatShouldBeResubmitted(int id) {
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
        .county(County.Anoka)
        .id(applicationId)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(applicationWithNoStatuses);

    return applicationId;
  }
}
