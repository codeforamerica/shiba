package org.codeforamerica.shiba.pages.events;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.Mockito.*;

class ApplicationSubmittedListenerTest {
    MnitDocumentConsumer mnitDocumentConsumer = mock(MnitDocumentConsumer.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    EmailClient emailClient = mock(EmailClient.class);
    SnapExpeditedEligibilityDecider expeditedEligibilityDecider = mock(SnapExpeditedEligibilityDecider.class);
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);
    EmailParser emailParser = mock(EmailParser.class);
    CountyMap<MnitCountyInformation> countyMap = new CountyMap<>();
    DocumentListParser documentListParser = mock(DocumentListParser.class);
    FeatureFlagConfiguration featureFlagConfiguration = mock(FeatureFlagConfiguration.class);
    MonitoringService monitoringService = mock(MonitoringService.class);
    ApplicationSubmittedListener applicationSubmittedListener;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        applicationSubmittedListener = new ApplicationSubmittedListener(
                mnitDocumentConsumer,
                applicationRepository,
                emailClient,
                expeditedEligibilityDecider,
                pdfGenerator,
                countyMap,
                featureFlagConfiguration,
                emailParser,
                documentListParser,
                monitoringService);
    }

    @Nested
    class sendApplicationToMNIT {
        @Test
        void shouldSendSubmittedApplicationToMNITWhenFlagIsOn() {
            when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.ON);
            String applicationId = "someId";
            Application application = Application.builder().id(applicationId).build();
            ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId", applicationId, null, Locale.ENGLISH);
            when(applicationRepository.find(applicationId)).thenReturn(application);

            applicationSubmittedListener.sendViaApi(event);

            verify(mnitDocumentConsumer).process(application);
        }

        @Test
        void shouldNotSendViaApiIfSendViaApiIsFalse() {
            when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.OFF);

            ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("", "", null, Locale.ENGLISH);

            applicationSubmittedListener.sendViaApi(event);

            verifyNoInteractions(mnitDocumentConsumer);
        }
    }

    @Nested
    class sendClientConfirmationEmail {
        @BeforeEach
        void setUp() {
            when(emailParser.parse(any())).thenReturn(Optional.of("email@address"));
        }

        @Test
        void shouldSendConfirmationMailForSubmittedApplicationWithCAF() {
            String applicationId = "applicationId";
            ApplicationData applicationData = new ApplicationData();
            String email = "abc@123.com";
            String appIdFromDb = "id";
            Application application = Application.builder()
                    .id(appIdFromDb)
                    .completedAt(ZonedDateTime.now())
                    .applicationData(applicationData)
                    .build();
            when(applicationRepository.find(applicationId)).thenReturn(application);
            ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId", applicationId, null, Locale.ENGLISH);
            when(expeditedEligibilityDecider.decide(applicationData)).thenReturn(SnapExpeditedEligibility.ELIGIBLE);
            ApplicationFile applicationFile = new ApplicationFile("someContent".getBytes(), "someFileName");
            when(documentListParser.parse(applicationData)).thenReturn(List.of(Document.CAF));
            when(pdfGenerator.generate(appIdFromDb, Document.CAF, CLIENT)).thenReturn(applicationFile);
            when(emailParser.parse(applicationData)).thenReturn(Optional.of(email));
            applicationSubmittedListener.sendConfirmationEmail(event);

            verify(emailClient).sendConfirmationEmail(email, appIdFromDb, SnapExpeditedEligibility.ELIGIBLE, List.of(applicationFile), Locale.ENGLISH);
        }

        @Test
        void shouldSendConfirmationMailForSubmittedApplicationWithCCAP() {
            String applicationId = "applicationId";
            ApplicationData applicationData = new ApplicationData();
            String email = "abc@123.com";
            String appIdFromDb = "id";
            Application application = Application.builder()
                    .id(appIdFromDb)
                    .completedAt(ZonedDateTime.now())
                    .applicationData(applicationData)
                    .build();
            when(applicationRepository.find(applicationId)).thenReturn(application);
            ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId", applicationId, null, Locale.ENGLISH);
            when(expeditedEligibilityDecider.decide(applicationData)).thenReturn(SnapExpeditedEligibility.ELIGIBLE);
            ApplicationFile applicationFile = new ApplicationFile("someContent".getBytes(), "someFileName");
            when(documentListParser.parse(applicationData)).thenReturn(List.of(Document.CCAP));
            when(pdfGenerator.generate(appIdFromDb, Document.CCAP, CLIENT)).thenReturn(applicationFile);
            when(emailParser.parse(applicationData)).thenReturn(Optional.of(email));
            applicationSubmittedListener.sendConfirmationEmail(event);

            verify(emailClient).sendConfirmationEmail(email, appIdFromDb, SnapExpeditedEligibility.ELIGIBLE, List.of(applicationFile), Locale.ENGLISH);
        }

        @Test
        void shouldSendConfirmationMailForSubmittedApplicationWithCAFAndCCAP() {
            String applicationId = "applicationId";
            ApplicationData applicationData = new ApplicationData();
            String email = "abc@123.com";
            String appIdFromDb = "id";
            Application application = Application.builder()
                    .id(appIdFromDb)
                    .completedAt(ZonedDateTime.now())
                    .applicationData(applicationData)
                    .build();
            when(applicationRepository.find(applicationId)).thenReturn(application);
            ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId", applicationId, null, Locale.ENGLISH);
            when(expeditedEligibilityDecider.decide(applicationData)).thenReturn(SnapExpeditedEligibility.ELIGIBLE);
            ApplicationFile applicationFileCAF = new ApplicationFile("someContent".getBytes(), "someFileName");
            when(documentListParser.parse(applicationData)).thenReturn(List.of(Document.CAF, Document.CCAP));
            when(pdfGenerator.generate(appIdFromDb, Document.CAF, CLIENT)).thenReturn(applicationFileCAF);
            ApplicationFile applicationFileCCAP = new ApplicationFile("someContent".getBytes(), "someFileName");
            when(pdfGenerator.generate(appIdFromDb, Document.CCAP, CLIENT)).thenReturn(applicationFileCCAP);
            List<ApplicationFile> applicationFiles = List.of(applicationFileCAF, applicationFileCCAP);

            when(emailParser.parse(applicationData)).thenReturn(Optional.of(email));
            applicationSubmittedListener.sendConfirmationEmail(event);

            verify(emailClient).sendConfirmationEmail(email, appIdFromDb, SnapExpeditedEligibility.ELIGIBLE, applicationFiles, Locale.ENGLISH);
        }


        @Test
        void shouldNotSendConfirmationEmailIfEmailIsMissingFromTheApplication() {
            ApplicationData applicationData = new ApplicationData();
            when(applicationRepository.find(any())).thenReturn(Application.builder()
                    .id("")
                    .completedAt(ZonedDateTime.now())
                    .applicationData(applicationData)
                    .build());
            when(emailParser.parse(applicationData)).thenReturn(empty());
            ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId", "appId", null, Locale.ENGLISH);

            applicationSubmittedListener.sendConfirmationEmail(event);

            verifyNoInteractions(pdfGenerator);
            verifyNoInteractions(expeditedEligibilityDecider);
            verifyNoInteractions(emailClient);
        }
    }

    @Nested
    class sendCaseWorkerEmail {
        @Nested
        class featureFlagOn {
            @BeforeEach
            void setUp() {
                when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.ON);
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
                County recipientCounty = Hennepin;
                String email = "someEmail";
                countyMap.getCounties().put(recipientCounty, MnitCountyInformation.builder().email(email).build());
                Application application = Application.builder()
                        .id(appIdFromDb)
                        .completedAt(ZonedDateTime.now())
                        .applicationData(applicationData)
                        .county(recipientCounty)
                        .timeToComplete(null)
                        .build();
                when(applicationRepository.find(applicationId)).thenReturn(application);
                ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId", applicationId, null, Locale.ENGLISH);
                when(expeditedEligibilityDecider.decide(applicationData)).thenReturn(SnapExpeditedEligibility.ELIGIBLE);
                ApplicationFile applicationFile = new ApplicationFile("someContent".getBytes(), "someFileName");
                when(pdfGenerator.generate(appIdFromDb, Document.CAF, CASEWORKER)).thenReturn(applicationFile);

                applicationSubmittedListener.sendCaseWorkerEmail(event);

                verify(emailClient).sendCaseWorkerEmail(email, fullName, appIdFromDb, applicationFile);
            }
        }

        @Test
        void shouldNotSendEmailIfSendCaseWorkerEmailIsFalse() {
            when(featureFlagConfiguration.get("submit-via-email")).thenReturn(FeatureFlag.OFF);

            ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("", "", null, Locale.ENGLISH);

            applicationSubmittedListener.sendCaseWorkerEmail(event);

            verifyNoInteractions(emailClient);
        }
    }
}