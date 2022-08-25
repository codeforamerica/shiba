package org.codeforamerica.shiba;

import static java.time.ZonedDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.application.Status.UNDELIVERABLE;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatus;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
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
  private ApplicationStatusRepository applicationStatusRepository;

  @MockBean
  private PageEventPublisher pageEventPublisher;

  @MockBean
  private ClientRegistrationRepository repository;

  @MockBean
  private DocumentRepository documentRepository;

  @MockBean
  private PdfGenerator pdfGenerator;

  private final ZonedDateTime moreThan60DaysAgo = now().withFixedOffsetZone().minusDays(60)
      .minusNanos(1);
  private final ZonedDateTime tenHoursAgo = now().withFixedOffsetZone().minusHours(10);

  @Test
  void itTriggersAnEventForAppsWithMissingStatuses() {
    for (int i = 0; i < 31; i++) {
      if (i == 0) {
        makeBlankStatusLaterDocApplication(Integer.toString(i), Hennepin,
            tenHoursAgo.plusMinutes(i), true);
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
    String applicationIdToResubmit = makeBlankStatusApplication("1", Hennepin, tenHoursAgo).getId();
    String appWithExistingStatus = makeSendingApplicationThatShouldNotBeResubmitted().getId();
    String appWithUploadedDocsOlderThan60Days = makeBlankStatusApplication("4", Anoka,
        moreThan60DaysAgo).getId();
    String laterDocsSubmissionOlderThan60Days = makeBlankStatusLaterDocApplication("5", Anoka,
        moreThan60DaysAgo, true).getId();
    String appWithOnlyProgramsNone = makeBlankStatusApplication("6", Anoka, List.of("NONE"),
            tenHoursAgo).getId();

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

    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", appWithOnlyProgramsNone,
            LocaleContextHolder.getLocale()));
  }

  @Test
  void setsApplicationsWithDocsOlderThan60DaysAsUndeliverable() {
    String appWithUploadedDocsOlderThan60Days = makeBlankStatusApplication("48",
        Hennepin, moreThan60DaysAgo).getId();
    String laterDocsSubmissionOlderThan60Days = makeBlankStatusLaterDocApplication("51",
        Hennepin, moreThan60DaysAgo, true).getId();

    resubmissionService.resubmitBlankStatusApplicationsViaEsb();
    List<String> appWithUploadedDocsOlderThan60DaysFilename = applicationStatusRepository.getAndSetFileNames(
        makeBlankStatusApplication("48",
            Hennepin, moreThan60DaysAgo), CAF);
    List<String> appWithUploadedDocsOlderThan60DaysFileNames = applicationStatusRepository.getAndSetFileNames(
        makeBlankStatusApplication("48",
            Hennepin, moreThan60DaysAgo), UPLOADED_DOC);
    List<String> laterDocsSubmissionOlderThan60DaysFileNames = applicationStatusRepository.getAndSetFileNames(
        makeBlankStatusLaterDocApplication("51",
            Hennepin, moreThan60DaysAgo, true), UPLOADED_DOC);

    assertThat(applicationStatusRepository.findAll(
        appWithUploadedDocsOlderThan60Days)).containsExactlyInAnyOrder(
        new ApplicationStatus(appWithUploadedDocsOlderThan60Days, CAF, "Hennepin", SENDING,
            appWithUploadedDocsOlderThan60DaysFilename.get(0)),
        new ApplicationStatus(appWithUploadedDocsOlderThan60Days, UPLOADED_DOC, "Hennepin",
            Status.UNDELIVERABLE, appWithUploadedDocsOlderThan60DaysFileNames.get(0)),
        new ApplicationStatus(appWithUploadedDocsOlderThan60Days, UPLOADED_DOC, "Hennepin",
            Status.UNDELIVERABLE, appWithUploadedDocsOlderThan60DaysFileNames.get(1))
    );
    assertThat(applicationStatusRepository.findAll(
        laterDocsSubmissionOlderThan60Days)).containsExactlyInAnyOrder(
        new ApplicationStatus(laterDocsSubmissionOlderThan60Days, UPLOADED_DOC, "Hennepin",
            Status.UNDELIVERABLE, laterDocsSubmissionOlderThan60DaysFileNames.get(0)),
        new ApplicationStatus(laterDocsSubmissionOlderThan60Days, UPLOADED_DOC, "Hennepin",
            Status.UNDELIVERABLE, laterDocsSubmissionOlderThan60DaysFileNames.get(1))

    );
  }

  @Test
  void shouldSetLaterDocsAppsWithNoDocumentsToUndeliverableAndNotPublishSubmissionEvents() {
    Application laterDocsWithoutDocuments = makeBlankStatusLaterDocApplication("60", Hennepin,
        now(), false);
    Application laterDocsWithDocuments = makeBlankStatusLaterDocApplication("61", Hennepin,
        now().minusMinutes(5), true);
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();
    List<String> laterDocsWithoutDocumentsFileNames = applicationStatusRepository.getAndSetFileNames(
        laterDocsWithoutDocuments, UPLOADED_DOC);
    List<String> laterDocsWithDocumentsFileNames = applicationStatusRepository.getAndSetFileNames(
        laterDocsWithDocuments, UPLOADED_DOC);
    assertThat(applicationStatusRepository.findAll(laterDocsWithDocuments.getId())).contains(
        new ApplicationStatus(laterDocsWithDocuments.getId(), UPLOADED_DOC, "Hennepin", SENDING,
            laterDocsWithDocumentsFileNames.get(0))
    );
    verify(pageEventPublisher).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", laterDocsWithDocuments.getId(),
            LocaleContextHolder.getLocale()));
    assertThat(applicationStatusRepository.findAll(laterDocsWithoutDocuments.getId())).contains(
        new ApplicationStatus(laterDocsWithoutDocuments.getId(), UPLOADED_DOC, "Hennepin",
            UNDELIVERABLE, laterDocsWithoutDocumentsFileNames.get(0))
    );
    verify(pageEventPublisher, never()).publish(
        new UploadedDocumentsSubmittedEvent("resubmission", laterDocsWithoutDocuments.getId(),
            LocaleContextHolder.getLocale()));
  }

  @Test
  void shouldSetLaterDocsAppsWithAllFilesOfSize0BytesToUndeliverableAndNotPublishSubmissionEvents() {
    Application laterDocsWithDocsOfSize0Bytes = makeBlankStatusLaterDocApplication("71", Hennepin,
        now().minusMinutes(5), true);
    
    laterDocsWithDocsOfSize0Bytes.getApplicationData().getUploadedDocs()
        .forEach(doc -> doc.setSize(0));
    /*
     * ApplicationFile applicationFile1 = new ApplicationFile("doc1.pdf".getBytes(),
     * "fileName.txt"); ApplicationFile applicationFile2 = new
     * ApplicationFile("doc2.pdf".getBytes(), "fileName1.txt"); var coverPage =
     * "someCoverPageText".getBytes();
     * when(pdfGenerator.generateCoverPageForUploadedDocs(eq(laterDocsWithDocsOfSize0Bytes)))
     * .thenReturn(coverPage); var uploadedDocs =
     * laterDocsWithDocsOfSize0Bytes.getApplicationData().getUploadedDocs();
     * when(pdfGenerator.generateCombinedUploadedDocument(eq(uploadedDocs),
     * eq(laterDocsWithDocsOfSize0Bytes), eq(coverPage), any()))
     * .thenReturn(List.of(applicationFile1, applicationFile2));
     */
    
    applicationRepository.save(laterDocsWithDocsOfSize0Bytes);
    resubmissionService.resubmitBlankStatusApplicationsViaEsb();
    
    List<String> laterDocsWithDocsOfSize0BytesFileNames =
        applicationStatusRepository.getAndSetFileNames(laterDocsWithDocsOfSize0Bytes, UPLOADED_DOC);
    assertThat(applicationStatusRepository.findAll(laterDocsWithDocsOfSize0Bytes.getId()))
        .contains(new ApplicationStatus(laterDocsWithDocsOfSize0Bytes.getId(), UPLOADED_DOC,
            "Hennepin", UNDELIVERABLE, laterDocsWithDocsOfSize0BytesFileNames.get(0)));
    verify(pageEventPublisher, never()).publish(new UploadedDocumentsSubmittedEvent("resubmission",
        laterDocsWithDocsOfSize0Bytes.getId(), LocaleContextHolder.getLocale()));

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
	  return makeBlankStatusApplication(id, county, List.of(SNAP, CASH), createdAt);
  }

  private Application makeBlankStatusApplication(String id, County county,
      List<String> programs, ZonedDateTime createdAt) {

    ApplicationData applicationData = new TestApplicationDataBuilder().withUploadedDocs();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", programs)
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

  private Application makeSendingApplicationThatShouldNotBeResubmitted() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    String applicationId = "1000";
    Application applicationThatShouldNotBeResubmitted = Application.builder()
        .completedAt(now().minusHours(1)) // important that this is completed!!!
        .county(County.Anoka)
        .id(applicationId)
        .applicationData(applicationData)
        .docUploadEmailStatus(null)
        .flow(FlowType.FULL)
        .build();
    applicationRepository.save(applicationThatShouldNotBeResubmitted);

    applicationStatusRepository.createOrUpdate(
        applicationId,
        CAF,
        "Anoka",
        SENDING, "");

    return applicationThatShouldNotBeResubmitted;
  }
}
