package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.ExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.codeforamerica.shiba.County.HENNEPIN;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.Mockito.*;

class ApplicationSubmittedListenerTest {
    MnitDocumentConsumer mnitDocumentConsumer = mock(MnitDocumentConsumer.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    EmailClient emailClient = mock(EmailClient.class);
    ExpeditedEligibilityDecider expeditedEligibilityDecider = mock(ExpeditedEligibilityDecider.class);
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);

    CountyEmailMap countyEmailMap = new CountyEmailMap();
    Boolean sendCaseWorkerEmail = true;
    Boolean sendViaApi = true;

    ApplicationSubmittedListener applicationSubmittedListener = new ApplicationSubmittedListener(
            mnitDocumentConsumer,
            applicationRepository,
            emailClient,
            expeditedEligibilityDecider,
            pdfGenerator,
            countyEmailMap,
            sendCaseWorkerEmail,
            sendViaApi
    );

    @Test
    void shouldSendSubmittedApplicationToMNIT() {
        String applicationId = "someId";
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(ZonedDateTime.now())
                .applicationData(null)
                .county(null)
                .fileName("")
                .timeToComplete(null)
                .build();
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(applicationId);
        when(applicationRepository.find(applicationId)).thenReturn(application);

        applicationSubmittedListener.sendViaApi(event);

        verify(mnitDocumentConsumer).process(application);
    }

    @Test
    void shouldSendMailForSubmittedApplication() {
        String applicationId = "applicationId";
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData contactInfoPage = new PageData();
        String email = "abc@123.com";
        contactInfoPage.put("email", InputData.builder().value(List.of(email)).build());
        pagesData.put("contactInfo", contactInfoPage);
        applicationData.setPagesData(pagesData);
        String appIdFromDb = "id";
        Application application = Application.builder()
                .id(appIdFromDb)
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(null)
                .fileName("")
                .timeToComplete(null)
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(applicationId);
        when(expeditedEligibilityDecider.decide(pagesData)).thenReturn(ExpeditedEligibility.ELIGIBLE);
        ApplicationFile applicationFile = new ApplicationFile("someContent".getBytes(), "someFileName");
        when(pdfGenerator.generate(appIdFromDb, CLIENT)).thenReturn(applicationFile);

        applicationSubmittedListener.sendConfirmationEmail(event);

        verify(emailClient).sendConfirmationEmail(email, appIdFromDb, ExpeditedEligibility.ELIGIBLE, applicationFile);
    }

    @Test
    void shouldNotSendEmailIfEmailIsMissing() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData contactInfo = new PageData();
        pagesData.put("contactInfo", contactInfo);
        applicationData.setPagesData(pagesData);
        when(applicationRepository.find(any())).thenReturn(Application.builder()
                .id("")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(null)
                .fileName("")
                .timeToComplete(null)
                .build());
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("appId");

        applicationSubmittedListener.sendConfirmationEmail(event);

        verifyNoInteractions(pdfGenerator);
        verifyNoInteractions(expeditedEligibilityDecider);
        verifyNoInteractions(emailClient);
    }

    @Test
    void shouldSendEmailToCaseWorkers() {
        String applicationId = "applicationId";
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData personalInfoPage = new PageData();
        personalInfoPage.put("firstName", InputData.builder().value(List.of("Testy")).build());
        personalInfoPage.put("lastName", InputData.builder().value(List.of("McTesterson")).build());
        pagesData.put("personalInfo", personalInfoPage);
        applicationData.setPagesData(pagesData);
        String appIdFromDb = "id";
        String fullName = "Testy McTesterson";
        County recipientCounty = HENNEPIN;
        String email = "someEmail";
        countyEmailMap.put(recipientCounty, email);
        Application application = Application.builder()
                .id(appIdFromDb)
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(recipientCounty)
                .fileName("")
                .timeToComplete(null)
                .build();
        when(applicationRepository.find(applicationId)).thenReturn(application);
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(applicationId);
        when(expeditedEligibilityDecider.decide(pagesData)).thenReturn(ExpeditedEligibility.ELIGIBLE);
        ApplicationFile applicationFile = new ApplicationFile("someContent".getBytes(), "someFileName");
        when(pdfGenerator.generate(appIdFromDb, CASEWORKER)).thenReturn(applicationFile);

        applicationSubmittedListener.sendCaseWorkerEmail(event);

        verify(emailClient).sendCaseWorkerEmail(email, fullName, appIdFromDb, applicationFile);
    }

    @Test
    void shouldNotSendEmailIfSendCaseWorkerEmailIsFalse() {
        applicationSubmittedListener = new ApplicationSubmittedListener(
                mnitDocumentConsumer,
                applicationRepository,
                emailClient,
                expeditedEligibilityDecider,
                pdfGenerator,
                countyEmailMap,
                false,
                false
        );

        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("");

        applicationSubmittedListener.sendCaseWorkerEmail(event);

        verifyNoInteractions(emailClient);
    }

    @Test
    void shouldNotSendViaApiIfSendViaApiIsFalse() {
        applicationSubmittedListener = new ApplicationSubmittedListener(
                mnitDocumentConsumer,
                applicationRepository,
                emailClient,
                expeditedEligibilityDecider,
                pdfGenerator,
                countyEmailMap,
                true,
                false
        );

        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("");

        applicationSubmittedListener.sendViaApi(event);

        verifyNoInteractions(mnitDocumentConsumer);
    }
}