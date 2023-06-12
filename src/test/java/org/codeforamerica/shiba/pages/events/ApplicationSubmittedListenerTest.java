package org.codeforamerica.shiba.pages.events;

import static org.codeforamerica.shiba.County.Ramsey;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.parsers.ContactInfoParser;
import org.codeforamerica.shiba.application.parsers.EmailParser;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.CcapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibility;
import org.codeforamerica.shiba.output.caf.SnapExpeditedEligibilityDecider;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.pages.rest.CommunicationClient;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.i18n.LocaleContextHolder;

import com.google.gson.JsonObject;

class ApplicationSubmittedListenerTest {

  MnitDocumentConsumer mnitDocumentConsumer = mock(MnitDocumentConsumer.class);
  ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
  EmailClient emailClient = mock(EmailClient.class);
  SnapExpeditedEligibilityDecider snapExpeditedEligibilityDecider = mock(
      SnapExpeditedEligibilityDecider.class);
  CcapExpeditedEligibilityDecider ccapExpeditedEligibilityDecider = mock(
      CcapExpeditedEligibilityDecider.class);
  PdfGenerator pdfGenerator = mock(PdfGenerator.class);
  FeatureFlagConfiguration featureFlagConfiguration = mock(FeatureFlagConfiguration.class);
  MonitoringService monitoringService = mock(MonitoringService.class);
  ApplicationSubmittedListener applicationSubmittedListener;
  RoutingDecisionService routingDecisionService = mock(RoutingDecisionService.class);
  CommunicationClient communicationClient = mock(CommunicationClient.class);

  @BeforeEach
  void setUp() {
    LocaleContextHolder.setLocale(Locale.ENGLISH);
    when(routingDecisionService.getRoutingDestinationByName("Ramsey")).thenReturn(new CountyRoutingDestination(Ramsey, "DPI", "email", "(651)555-5555"));
    applicationSubmittedListener = new ApplicationSubmittedListener(
        mnitDocumentConsumer,
        applicationRepository,
        emailClient,
        snapExpeditedEligibilityDecider,
        ccapExpeditedEligibilityDecider,
        pdfGenerator,
        featureFlagConfiguration,
        monitoringService, 
        routingDecisionService, 
        communicationClient
        );
  }
  
	@Nested
	class SendTextToCommHub{

		@Test
		void confirmTextMessageNotSentWhenDisabled() {
			String applicationId = "applicationId";
			ApplicationData applicationData = mock(ApplicationData.class);
			when(applicationData.getPagesData())
			.thenReturn(new PagesDataBuilder()
					.withPageData("contactInfo", "phoneOrEmail", "TEXT")
					.withPageData("identifyCounty", "county", "Ramsey")
					.withPageData("contactInfo", "phoneNumber", "(651)555-5555")
					.withPageData("personalInfo", "firstName", "FirstName").build());
			String appIdFromDb = "id";
			JsonObject jsonObject = new JsonObject();
			Application application = Application.builder().id(appIdFromDb).completedAt(ZonedDateTime.now())
					.county(Ramsey)
					.applicationData(applicationData).build();
			when(applicationRepository.find(applicationId)).thenReturn(application);
			ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId", applicationId, null,
					Locale.ENGLISH);
			when(communicationClient.isEnabled()).thenReturn(false);
			applicationSubmittedListener.notifyApplicationSubmission(event);  
			verify(communicationClient, never()).send(jsonObject);
		}

		@Test
		void shouldSendConfirmationRestCallForSubmittedApplication() {
			ZonedDateTime dateTime = ZonedDateTime.now();
			String applicationId = "applicationId";
			ApplicationData applicationData = mock(ApplicationData.class);
			when(applicationData.getOriginalCounty()).thenReturn("Ramsey");
			when(applicationData.getId()).thenReturn(applicationId);
			when(applicationData.getPagesData())
					.thenReturn(new PagesDataBuilder().withPageData("contactInfo", "phoneOrEmail", "TEXT")
							.withPageData("identifyCounty", "county", "Ramsey")
							.withPageData("contactInfo", "phoneNumber", "(651)555-5555")
							.withPageData("personalInfo", "firstName", "FirstName").build());
			Application application = Application.builder().id(applicationId).county(County.Ramsey)
					.completedAt(dateTime).applicationData(applicationData).build();
			ApplicationSubmittedEvent  event = new ApplicationSubmittedEvent("someSessionId", applicationId, null, Locale.ENGLISH);
			when(applicationSubmittedListener.getApplicationFromEvent(event)).thenReturn(application);
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("appId", applicationData.getId());
			jsonObject.addProperty("firstName", ContactInfoParser.firstName(applicationData));
			jsonObject.addProperty("phoneNumber", ContactInfoParser.phoneNumber(applicationData).replaceAll("[^0-9]", ""));
			jsonObject.addProperty("email", ContactInfoParser.email(applicationData));
			jsonObject.addProperty("opt-status-sms", ContactInfoParser.optedIntoTEXT(applicationData));
			jsonObject.addProperty("opt-status-email", ContactInfoParser.optedIntoEmailCommunications(applicationData));
			jsonObject.addProperty("completed-dt", dateTime.format( DateTimeFormatter.ofPattern("MMM d uuuu, hh:mm:ss", Locale.US))); 
			jsonObject.addProperty("county", "Ramsey");
			jsonObject.addProperty("countyPhoneNumber", "(651)555-5555");
			when(communicationClient.isEnabled()).thenReturn(true);
			applicationSubmittedListener.notifyApplicationSubmission(event);
			verify(communicationClient, times(1)).send(jsonObject);
		}
	}


  @Nested
  class sendApplicationToMNIT {

    @Test
    void shouldSendSubmittedApplicationToMNITWhenFlagIsOn() {
      when(featureFlagConfiguration.get("submit-via-api")).thenReturn(FeatureFlag.ON);
      String applicationId = "someId";
      Application application = Application.builder().id(applicationId).build();
      ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId",
          applicationId,
          null,
          Locale.ENGLISH);
      when(applicationRepository.find(applicationId)).thenReturn(application);

      applicationSubmittedListener.sendViaApi(event);

      verify(mnitDocumentConsumer).processCafAndCcap(application);
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
  class SendClientConfirmationEmail {

    @Test
    void shouldSendConfirmationMailForSubmittedApplicationWithCAF() {
      String applicationId = "applicationId";
      ApplicationData applicationData = mock(ApplicationData.class);
      String email = "abc@123.com";
      String appIdFromDb = "id";
      Application application = Application.builder()
          .id(appIdFromDb)
          .completedAt(ZonedDateTime.now())
          .applicationData(applicationData)
          .build();
      when(applicationRepository.find(applicationId)).thenReturn(application);
      ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId",
          applicationId,
          null,
          Locale.ENGLISH);
      when(applicationData.isCAFApplication()).thenReturn(true);
      when(applicationData.getPagesData()).thenReturn(new PagesData());
      when(snapExpeditedEligibilityDecider.decide(applicationData))
          .thenReturn(SnapExpeditedEligibility.ELIGIBLE);
      when(ccapExpeditedEligibilityDecider.decide(applicationData))
          .thenReturn(CcapExpeditedEligibility.UNDETERMINED);
      ApplicationFile applicationFile = new ApplicationFile("someContent".getBytes(),
          "someFileName");
      when(pdfGenerator.generate(appIdFromDb, Document.CAF, CLIENT)).thenReturn(applicationFile);

      try (MockedStatic<EmailParser> mockEmailParser = Mockito.mockStatic(EmailParser.class)) {
        mockEmailParser.when(() -> EmailParser.parse(any())).thenReturn(Optional.of(email));
        applicationSubmittedListener.sendConfirmationEmail(event);
      }

      verify(emailClient).sendConfirmationEmail(applicationData,
          email,
          appIdFromDb,
          List.of(),
          SnapExpeditedEligibility.ELIGIBLE,
          CcapExpeditedEligibility.UNDETERMINED,
          List.of(applicationFile),
          Locale.ENGLISH);
    }

    @Test
    void shouldSendMultipleConfirmationMailingsWhenOptedIn() {
      String applicationId = "applicationId";
      ApplicationData applicationData = mock(ApplicationData.class);
      String email = "abc@123.com";
      String appIdFromDb = "id";
      Application application = Application.builder()
          .id(appIdFromDb)
          .completedAt(ZonedDateTime.now())
          .applicationData(applicationData)
          .build();
      when(applicationRepository.find(applicationId)).thenReturn(application);
      ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId",
          applicationId,
          null,
          Locale.ENGLISH);
      when(applicationData.isCAFApplication()).thenReturn(true);
      when(applicationData.getPagesData()).thenReturn(new PagesDataBuilder()
          .withPageData("contactInfo", "phoneOrEmail", "EMAIL").build());
      when(snapExpeditedEligibilityDecider.decide(applicationData))
          .thenReturn(SnapExpeditedEligibility.ELIGIBLE);
      when(ccapExpeditedEligibilityDecider.decide(applicationData))
          .thenReturn(CcapExpeditedEligibility.UNDETERMINED);
      ApplicationFile applicationFile = new ApplicationFile("someContent".getBytes(),
          "someFileName");
      when(pdfGenerator.generate(appIdFromDb, Document.CAF, CLIENT)).thenReturn(applicationFile);

      try (MockedStatic<EmailParser> mockEmailParser = Mockito.mockStatic(EmailParser.class)) {
        mockEmailParser.when(() -> EmailParser.parse(any())).thenReturn(Optional.of(email));
        applicationSubmittedListener.sendConfirmationEmail(event);
      }

      verify(emailClient).sendShortConfirmationEmail(applicationData,
          email,
          appIdFromDb,
          List.of(),
          SnapExpeditedEligibility.ELIGIBLE,
          CcapExpeditedEligibility.UNDETERMINED,
          List.of(applicationFile),
          Locale.ENGLISH);
    }

    @Test
    void shouldSendConfirmationMailForSubmittedApplicationWithCCAP() {
      String applicationId = "applicationId";
      ApplicationData applicationData = mock(ApplicationData.class);
      String email = "abc@123.com";
      String appIdFromDb = "id";
      Application application = Application.builder()
          .id(appIdFromDb)
          .completedAt(ZonedDateTime.now())
          .applicationData(applicationData)
          .build();
      when(applicationRepository.find(applicationId)).thenReturn(application);
      ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId",
          applicationId,
          null,
          Locale.ENGLISH);
      when(snapExpeditedEligibilityDecider.decide(applicationData))
          .thenReturn(SnapExpeditedEligibility.UNDETERMINED);
      when(ccapExpeditedEligibilityDecider.decide(applicationData))
          .thenReturn(CcapExpeditedEligibility.ELIGIBLE);
      ApplicationFile applicationFile = new ApplicationFile("someContent".getBytes(),
          "someFileName");
      when(applicationData.isCCAPApplication()).thenReturn(true);
      when(applicationData.getPagesData()).thenReturn(new PagesData());
      when(pdfGenerator.generate(appIdFromDb, Document.CCAP, CLIENT)).thenReturn(applicationFile);
      try (MockedStatic<EmailParser> mockEmailParser = Mockito.mockStatic(EmailParser.class)) {
        mockEmailParser.when(() -> EmailParser.parse(any())).thenReturn(Optional.of(email));
        applicationSubmittedListener.sendConfirmationEmail(event);
      }

      verify(emailClient).sendConfirmationEmail(applicationData,
          email,
          appIdFromDb,
          List.of(),
          SnapExpeditedEligibility.UNDETERMINED,
          CcapExpeditedEligibility.ELIGIBLE,
          List.of(applicationFile),
          Locale.ENGLISH);
    }

    @Test
    void shouldSendConfirmationMailForSubmittedApplicationWithCAFAndCCAP() {
      String applicationId = "applicationId";
      ApplicationData applicationData = mock(ApplicationData.class);
      String email = "abc@123.com";
      String appIdFromDb = "id";
      Application application = Application.builder()
          .id(appIdFromDb)
          .completedAt(ZonedDateTime.now())
          .applicationData(applicationData)
          .build();
      when(applicationRepository.find(applicationId)).thenReturn(application);
      ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId",
          applicationId,
          null,
          Locale.ENGLISH);
      when(snapExpeditedEligibilityDecider.decide(applicationData))
          .thenReturn(SnapExpeditedEligibility.ELIGIBLE);
      when(ccapExpeditedEligibilityDecider.decide(applicationData))
          .thenReturn(CcapExpeditedEligibility.ELIGIBLE);
      ApplicationFile applicationFileCAF = new ApplicationFile("someContent".getBytes(),
          "someFileName");
      when(applicationData.isCAFApplication()).thenReturn(true);
      when(applicationData.isCCAPApplication()).thenReturn(true);
      when(applicationData.getPagesData()).thenReturn(new PagesData());
      when(pdfGenerator.generate(appIdFromDb, Document.CAF, CLIENT)).thenReturn(applicationFileCAF);
      ApplicationFile applicationFileCCAP = new ApplicationFile("someContent".getBytes(),
          "someFileName");
      when(pdfGenerator.generate(appIdFromDb, Document.CCAP, CLIENT))
          .thenReturn(applicationFileCCAP);
      List<ApplicationFile> applicationFiles = List.of(applicationFileCAF, applicationFileCCAP);

      try (MockedStatic<EmailParser> mockEmailParser = Mockito.mockStatic(EmailParser.class)) {
        mockEmailParser.when(() -> EmailParser.parse(any())).thenReturn(Optional.of(email));
        applicationSubmittedListener.sendConfirmationEmail(event);
      }

      verify(emailClient).sendConfirmationEmail(applicationData,
          email,
          appIdFromDb,
          List.of(),
          SnapExpeditedEligibility.ELIGIBLE,
          CcapExpeditedEligibility.ELIGIBLE,
          applicationFiles,
          Locale.ENGLISH);
    }


    @Test
    void shouldNotSendConfirmationEmailIfEmailIsMissingFromTheApplication() {
      ApplicationData applicationData = new ApplicationData();
      when(applicationRepository.find(any())).thenReturn(Application.builder()
          .id("")
          .completedAt(ZonedDateTime.now())
          .applicationData(applicationData)
          .build());
      ApplicationSubmittedEvent event = new ApplicationSubmittedEvent("someSessionId",
          "appId",
          null,
          Locale.ENGLISH);

      try (MockedStatic<EmailParser> mockEmailParser = Mockito.mockStatic(EmailParser.class)) {
        mockEmailParser.when(() -> EmailParser.parse(any())).thenReturn(Optional.empty());
        applicationSubmittedListener.sendConfirmationEmail(event);
      }

      verifyNoInteractions(pdfGenerator);
      verifyNoInteractions(snapExpeditedEligibilityDecider);
      verifyNoInteractions(emailClient);
    }
  }
}