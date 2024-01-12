package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.application.FlowType.FULL;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.XML;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.testutilities.TestUtils.resetApplicationData;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNation;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.mnit.FilenetWebServiceClient;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.config.FeatureFlag;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.EmailClient;
import org.codeforamerica.shiba.testutilities.NonSessionScopedApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test")
@SpringBootTest
@ContextConfiguration(classes = {NonSessionScopedApplicationData.class})
@Tag("db")
public class RoutingDestinationServiceTest {
	
	  public static final byte[] FILE_BYTES = new byte[10];

	  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	  @Autowired
	  private ServicingAgencyMap<CountyRoutingDestination> countyMap;
	  @Autowired
	  private ServicingAgencyMap<TribalNationRoutingDestination> tribalNationsMap;

	  @MockBean
	  private FeatureFlagConfiguration featureFlagConfig;
	  @MockBean
	  private FilenetWebServiceClient mnitClient;
	  @MockBean
	  private EmailClient emailClient;
	  @MockBean
	  private XmlGenerator xmlGenerator;
	  @MockBean
	  private MonitoringService monitoringService;
	  @MockBean
	  private DocumentRepository documentRepository;
	  @MockBean
	  private ClientRegistrationRepository repository;
	  @MockBean
	  private FilenameGenerator fileNameGenerator;
	  @MockBean
	  private ApplicationRepository applicationRepository;
	  @MockBean
	  private ApplicationStatusRepository applicationStatusRepository;
	  @MockBean
	  private MessageSource messageSource;
	  @SpyBean
	  private PdfGenerator pdfGenerator;

	  @Autowired
	  private ApplicationData applicationData;
	  @Autowired
	  private MnitDocumentConsumer documentConsumer;

	  private Application application;
	  
	  @Autowired
	  private RoutingDecisionService routingDecisionService;

	  @BeforeEach
	  void setUp() {
	    applicationData = new TestApplicationDataBuilder(applicationData)
	        .withPersonalInfo()
	        .withContactInfo()
	        .withApplicantPrograms(List.of("SNAP"))
	        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
	        .build();

	    ZonedDateTime completedAt = ZonedDateTime.of(
	        LocalDateTime.of(2021, 6, 10, 1, 28),
	        ZoneOffset.UTC);

	    application = Application.builder()
	        .id("someId")
	        .completedAt(completedAt)
	        .applicationData(applicationData)
	        .county(Olmsted)
	        .timeToComplete(null)
	        .flow(FULL)
	        .build();
	    when(messageSource.getMessage(any(), any(), any())).thenReturn("default success message");
	    when(fileNameGenerator.generatePdfFilename(any(), any())).thenReturn("some-file.pdf");
	    when(featureFlagConfig.get("filenet")).thenReturn(FeatureFlag.ON);

	    doReturn(application).when(applicationRepository).find(any());
	  }

	  @AfterEach
	  void afterEach() {
	    resetApplicationData(applicationData);
	  }
	  
	  @ParameterizedTest
	  @CsvSource({
	      "Becker,WhiteEarthNation",
	      "Mahnomen,WhiteEarthNation",
	      "Clearwater,WhiteEarthNation"})
	  public void routeToWhiteEarthNationForLinealDescendantWhenLivingInTheThreeCounties(String countyName, TribalNation expectedDestination) { 
		    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
		    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any(), any());
		    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
		    when(xmlGenerator.generate(any(), any(), any(), any())).thenReturn(xmlApplicationFile);

		    application.setApplicationData(new TestApplicationDataBuilder()
		        .withApplicantPrograms(List.of("EA"))
		        .withPageData("homeAddress", "county", List.of(countyName))
		        .withPageData("identifyCounty", "county", countyName)
		        .withPageData("linealDescendantWhiteEarthNation", "linealDescendantWEN", List.of("true") )
		        .build());
		    application.setCounty(County.getForName(countyName));
		    documentConsumer.processCafAndCcap(application);

		    TribalNationRoutingDestination routingDestination = tribalNationsMap.get(expectedDestination);
		    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
		    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
		    verify(mnitClient, times(2)).send(any(), any(), any(), any());
		    verify(mnitClient).send(application, pdfApplicationFile, tribalNationsMap.get(expectedDestination), CAF);
		    verify(mnitClient).send(application, xmlApplicationFile, tribalNationsMap.get(expectedDestination), XML);
		  
	  }
	  
	  @ParameterizedTest
	  @CsvSource({
	      "Becker,Becker",
	      "Mahnomen,Mahnomen",
	      "Clearwater,Clearwater"})
	  public void routeToCountyForNonLinealDescendantWhenLivingInTheThreeCounties(String countyName, County expectedDestination) { 
		    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
		    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any(), any());
		    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
		    when(xmlGenerator.generate(any(), any(), any(), any())).thenReturn(xmlApplicationFile);

		    application.setApplicationData(new TestApplicationDataBuilder()
		        .withApplicantPrograms(List.of("EA"))
		        .withPageData("homeAddress", "county", List.of(countyName))
		        .withPageData("identifyCounty", "county", countyName)
		        .withPageData("linealDescendantWhiteEarthNation", "linealDescendantWEN", List.of("false") )
		        .build());
		    application.setCounty(County.getForName(countyName));

		    documentConsumer.processCafAndCcap(application);
		    CountyRoutingDestination routingDestination = countyMap.get(expectedDestination);
		    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
		    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
		    verify(mnitClient, times(2)).send(any(), any(), any(), any());
		    verify(mnitClient).send(application, pdfApplicationFile, countyMap.get(expectedDestination), CAF);
		    verify(mnitClient).send(application, xmlApplicationFile, countyMap.get(expectedDestination), XML);
		  
	  }


	  @ParameterizedTest
	  @CsvSource({
	      "YellowMedicine,YellowMedicine",
	      "Aitkin,Aitkin",
	      "LakeOfTheWoods,LakeOfTheWoods",
	      "StLouis,StLouis",
	      "LacQuiParle,LacQuiParle"})
	  public void routeToCountyForLinealDescendantWhenNotLivingInTheThreeCounties(String countyName, County expectedCounty) { 
		    ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
		    doReturn(pdfApplicationFile).when(pdfGenerator).generate(anyString(), any(), any(), any());
		    ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
		    when(xmlGenerator.generate(any(), any(), any(), any())).thenReturn(xmlApplicationFile);

		    application.setApplicationData(new TestApplicationDataBuilder()
		        .withApplicantPrograms(List.of("EA"))
		        .withPageData("homeAddress", "county", List.of(countyName))
		        .withPageData("identifyCounty", "county", countyName)
		        .withPageData("linealDescendantWhiteEarthNation", "linealDescendantWEN", List.of("true") )
		        .build());
		    application.setCounty(expectedCounty);

		    documentConsumer.processCafAndCcap(application);

		    CountyRoutingDestination routingDestination = countyMap.get(expectedCounty);
		    verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
		    verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER, routingDestination);
		    verify(mnitClient, times(2)).send(any(), any(), any(), any());
		    verify(mnitClient).send(application, pdfApplicationFile, countyMap.get(expectedCounty), CAF);
		    verify(mnitClient).send(application, xmlApplicationFile, countyMap.get(expectedCounty), XML);
	  }
	  
	  @ParameterizedTest
	  @CsvSource(value = {"Becker", "Mahnomen", "Clearwater"})
	  void routeToWhiteEarthForLinealDescentantsFromTheThreeCounties(String county) throws Exception {
		  // This directly tests the RoutingDecisionService 
		  ApplicationData   applicationData = new TestApplicationDataBuilder()
			        .withPersonalInfo()
			        .withContactInfo()
			        .withApplicantPrograms(List.of("SNAP"))
			        .withPageData("identifyCounty", "county", county)
			        .withPageData("homeAddress", "county", List.of(county))
			        .withPageData("linealDescendantWhiteEarthNation", "linealDescendantWEN", List.of("true") )
			        .withPageData("homeAddress", "enrichedCounty", List.of(county))
			        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
			        .build();

			    ZonedDateTime completedAt = ZonedDateTime.of(
			        LocalDateTime.of(2021, 6, 10, 1, 28),
			        ZoneOffset.UTC);

			    application = Application.builder()
			        .id("someId")
			        .completedAt(completedAt)
			        .applicationData(applicationData)
			        .county(County.getForName(county))
			        .timeToComplete(null)
			        .flow(FULL)
			        .build();

	    var routingDestinations = routingDecisionService.getRoutingDestinations(applicationData, CAF);
	    RoutingDestination routingDestination = routingDestinations.get(0);
	    assertThat(routingDestination.getDhsProviderId()).isEqualTo("A086642300");
	    assertThat(routingDestination.getEmail()).isEqualTo("mnbenefits@state.mn.us");
	    assertThat(routingDestination.getPhoneNumber()).isEqualTo("218-935-2359");
	  }
	  
	  @Test
	  void routeToCountyForWhiteEarthLinealDescentantsForAnyOtherCounties() throws Exception {
		  // Residents of any other counties than the three should never see the WEN lineal descendant page, but test this anyway
		  ApplicationData   applicationData = new TestApplicationDataBuilder()
			        .withPersonalInfo()
			        .withContactInfo()
			        .withApplicantPrograms(List.of("SNAP"))
			        .withPageData("identifyCounty", "county", "Aitkin")
			        .withPageData("homeAddress", "county", List.of("Aitkin"))
			        .withPageData("linealDescendantWhiteEarthNation", "linealDescendantWEN", List.of("true") )
			        .withPageData("homeAddress", "enrichedCounty", List.of("Aitkin"))
			        .withPageData("verifyHomeAddress", "useEnrichedAddress", List.of("true"))
			        .build();

			    ZonedDateTime completedAt = ZonedDateTime.of(
			        LocalDateTime.of(2021, 6, 10, 1, 28),
			        ZoneOffset.UTC);

			    application = Application.builder()
			        .id("someId")
			        .completedAt(completedAt)
			        .applicationData(applicationData)
			        .county(County.getForName("Aitkin"))
			        .timeToComplete(null)
			        .flow(FULL)
			        .build();

	    var routingDestinations = routingDecisionService.getRoutingDestinations(applicationData, CAF);
	    RoutingDestination routingDestination = routingDestinations.get(0);
	    assertThat(routingDestination.getDhsProviderId()).isEqualTo("A000001900");//Aitkin
	  }

}
