package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.application.FlowType.FULL;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.client.RequestMatchers.connectionTo;
import static org.springframework.ws.test.client.RequestMatchers.xpath;
import static org.springframework.ws.test.client.ResponseCreators.withException;
import static org.springframework.ws.test.client.ResponseCreators.withSoapEnvelope;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import javax.xml.soap.SOAPException;
import javax.xml.transform.dom.DOMResult;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationStatus;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Node;

@SpringBootTest
@ActiveProfiles("test")
class FilenetWebServiceClientTest {

  private final Map<String, String> namespaceMapping = Map.of(
      "ns2", "http://docs.oasis-open.org/ns/cmis/messaging/200908/",
      "ns3", "http://docs.oasis-open.org/ns/cmis/core/200908/",
      "cmism", "http://docs.oasis-open.org/cmis/CMIS/v1.1/errata01/os/schema/CMIS-Messaging.xsd");
  private final String fileContent = "fileContent";
  private final String fileName = "fileName";
  private final String filenetIdd = "idd_some-filenet-idd";
  private final StringSource successResponse = new StringSource("" +
      "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
      "<soapenv:Body>" +
      "<b:createDocumentResponse xmlns:b='http://docs.oasis-open.org/ns/cmis/messaging/200908/'>" +
      "<b:objectId>" + filenetIdd + "</b:objectId>" +
      "<b:extension si:nil='true' xmlns:si='http://www.w3.org/2001/XMLSchema-instance'/>" +
      "</b:createDocumentResponse>" +
      "</soapenv:Body>" +
      "</soapenv:Envelope>"
  );
  private final String routerResponse = "{\n \"message\" : \"Success\" \n}";
  @Autowired
  @Qualifier("filenetWebServiceTemplate")
  private WebServiceTemplate webServiceTemplate;
  @Autowired
  private FilenetWebServiceClient filenetWebServiceClient;
  @MockBean
  private Clock clock;
  @Value("${mnit-filenet.upload-url}")
  private String url;
  @Value("${mnit-filenet.username}")
  private String username;
  @Value("${mnit-filenet.password}")
  private String password;
  @Value("${mnit-filenet.sftp-upload-url}")
  private String sftpUploadUrl;
  private MockWebServiceServer mockWebServiceServer;
  @MockBean
  private ApplicationStatusRepository applicationStatusRepository;
  @MockBean
  private RestTemplate restTemplate;
  
  private Application application;
  private CountyRoutingDestination olmsted;
  private CountyRoutingDestination hennepin;
  private String applicationId;

  @BeforeEach
  void setUp() {
    when(clock.instant()).thenReturn(Instant.now());
    when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    olmsted = new CountyRoutingDestination(Olmsted, "A000055800", "email", "8004112222");
    hennepin = new CountyRoutingDestination(Hennepin, "A000027200", "email", "8004112222");

    mockWebServiceServer = MockWebServiceServer.createServer(webServiceTemplate);
    applicationId = "someId";
    application = Application.builder()
            .id("someId")
            .flow(FULL)
            .build();
    
    when(applicationStatusRepository.find(eq(applicationId), eq(CAF), any(), eq(fileName))).thenReturn(
        new ApplicationStatus(applicationId, CAF, olmsted.getName(), SENDING, fileName, "")
    );
    when(applicationStatusRepository.find(applicationId, CAF, hennepin.getName(), fileName)).thenReturn(
        new ApplicationStatus(applicationId, CAF, hennepin.getName(), SENDING, fileName, "")
    );
    when(applicationStatusRepository.find(eq(applicationId), eq(Document.UPLOADED_DOC), any(), eq(fileName))).thenReturn(
            new ApplicationStatus(applicationId, Document.UPLOADED_DOC, olmsted.getName(), SENDING, fileName, "")
    );

    String routerRequest = String.format("%s/%s", sftpUploadUrl, filenetIdd);
    Mockito.when(restTemplate.getForObject(routerRequest, String.class)).thenReturn(routerResponse);
  }

  @Test
  void sendsTheDocumentToFilenetAndSFTP() {
    mockWebServiceServer.expect(connectionTo(url))
        .andExpect(xpath("//ns2:createDocument/ns2:repositoryId", namespaceMapping)
            .evaluatesTo("Programs"))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyBoolean[@propertyDefinitionId='Read']/ns3:value",
            namespaceMapping)
            .evaluatesTo(true))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='OriginalFileName']/ns3:value",
            namespaceMapping)
            .evaluatesTo("fileName"))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='FileType']/ns3:value",
            namespaceMapping)
            .evaluatesTo("Misc"))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='cmis:name']/ns3:value",
            namespaceMapping)
            .evaluatesTo("fileName"))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='NPI']/ns3:value",
            namespaceMapping)
            .evaluatesTo("A000055800"))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyId[@propertyDefinitionId='cmis:objectTypeId']/ns3:value",
            namespaceMapping)
            .evaluatesTo("MNITSMailbox"))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='MNITSMailboxTransactionType']/ns3:value",
            namespaceMapping)
            .evaluatesTo("OLA"))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='Source']/ns3:value",
            namespaceMapping)
            .evaluatesTo("MNITS"))
        .andExpect(xpath(
            "//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='Flow']/ns3:value",
            namespaceMapping)
            .evaluatesTo("Inbound"))
        .andRespond(withSoapEnvelope(successResponse));

    String routerRequest = String.format("%s/%s", sftpUploadUrl, filenetIdd);
    Mockito.when(restTemplate.getForObject(routerRequest, String.class)).thenReturn(routerResponse);
    
	filenetWebServiceClient.send(application, new ApplicationFile(fileContent.getBytes(), fileName), olmsted,
			Document.CAF);

    verify(applicationStatusRepository).createOrUpdate(applicationId, Document.CAF, olmsted.getName(),
        DELIVERED, fileName);

    mockWebServiceServer.verify();
  }
  
	@Test
	void sendsTheHealthcareRenewalDocumentToFilenetAndSFTP() {
		ApplicationData applicationData = new ApplicationData();
		new TestApplicationDataBuilder(applicationData).withPageData("healthcareRenewalUpload", "county", "Olmsted")
				.withPageData("healthcareRenewalMatchInfo", "caseNumber", "12345678").build();
		application.setApplicationData(applicationData);
		application.setFlow(FlowType.HEALTHCARE_RENEWAL);
		ZonedDateTime completedAt = ZonedDateTime.of(LocalDateTime.of(2021, 6, 10, 1, 28), ZoneOffset.UTC);
		application.setCompletedAt(completedAt);

		mockWebServiceServer.expect(connectionTo(url))
				.andExpect(xpath("//ns2:createDocument/ns2:repositoryId", namespaceMapping).evaluatesTo("Programs"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyBoolean[@propertyDefinitionId='Read']/ns3:value",
						namespaceMapping).evaluatesTo(true))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='OriginalFileName']/ns3:value",
						namespaceMapping).evaluatesTo("fileName"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='cmis:name']/ns3:value",
						namespaceMapping).evaluatesTo("fileName"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='NPI']/ns3:value",
						namespaceMapping).evaluatesTo("A000055800"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='Source']/ns3:value",
						namespaceMapping).evaluatesTo("MNBenefits"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='CTYMBDocumentType']/ns3:value",
						namespaceMapping).evaluatesTo("RENEWAL"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyDateTime[@propertyDefinitionId='DocumentDate']/ns3:value",
						namespaceMapping).evaluatesTo("2021-06-10T01:28:00.000Z"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='CaseID']/ns3:value",
						namespaceMapping).evaluatesTo("12345678"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='ReferenceNumber']/ns3:value",
						namespaceMapping).evaluatesTo("someId"))
				.andExpect(xpath(
						"//ns2:createDocument/ns2:properties/ns3:propertyId[@propertyDefinitionId='cmis:objectTypeId']/ns3:value",
						namespaceMapping).evaluatesTo("CountyMailbox"))
				.andRespond(withSoapEnvelope(successResponse));

		filenetWebServiceClient.send(application, new ApplicationFile(fileContent.getBytes(), fileName), olmsted,
				Document.UPLOADED_DOC);

		verify(applicationStatusRepository).createOrUpdate(applicationId, Document.UPLOADED_DOC, olmsted.getName(),
				DELIVERED, fileName);

		mockWebServiceServer.verify();
	}

  @Test
  void sendingTheDocumentToFilenetSavesTheFilenetId() {
    mockWebServiceServer.expect(connectionTo(url))
        .andRespond(withSoapEnvelope(successResponse));

    String routerRequest = String.format("%s/%s", sftpUploadUrl, filenetIdd);
    Mockito.when(restTemplate.getForObject(routerRequest, String.class)).thenReturn(routerResponse);

	filenetWebServiceClient.send(application, new ApplicationFile(fileContent.getBytes(), fileName), olmsted,
			Document.CAF);

    verify(applicationStatusRepository).updateFilenetId(applicationId, Document.CAF, olmsted.getName(),
        SENDING, fileName, filenetIdd);
    verify(applicationStatusRepository).createOrUpdate(applicationId, Document.CAF, olmsted.getName(),
        DELIVERED, fileName);

    mockWebServiceServer.verify();
  }

  @Test
  void sendsTheDocumentOnlyToSFTPIfAlreadyInFilenet() {
    when(applicationStatusRepository.find(applicationId, CAF, olmsted.getName(), fileName))
        .thenReturn(new ApplicationStatus(
                applicationId, CAF, olmsted.getName(), SENDING, fileName, filenetIdd )
        );
    Mockito.when(restTemplate.getForObject(sftpUploadUrl + "/" + filenetIdd, String.class))
        .thenReturn(routerResponse);

	filenetWebServiceClient.send(application, new ApplicationFile(fileContent.getBytes(), fileName), olmsted,
			Document.CAF);

    mockWebServiceServer.verify();
  }

  @Test
  void sendingDocumentRetriesIfSOAPExceptionIsThrown() {
    mockWebServiceServer.expect(connectionTo(url))
        .andRespond(withException(
            new RuntimeException(new SOAPException("soap exception ahhh"))));

    mockWebServiceServer.expect(connectionTo(url))
        .andRespond(withSoapEnvelope(successResponse));

	filenetWebServiceClient.send(application, new ApplicationFile(fileContent.getBytes(), fileName), olmsted,
			Document.CAF);

    mockWebServiceServer.verify();
  }

  @Test
  void sendingDocumentRecoveryReportsLastErrorIfSOAPExceptionIsThrown3Times() {
    mockWebServiceServer.expect(connectionTo(url))
        .andRespond(
            withException(new RuntimeException(new SOAPException("initial failure"))));

    mockWebServiceServer.expect(connectionTo(url))
        .andRespond(
            withException(new RuntimeException(new SOAPException("retry 1 failure"))));

    mockWebServiceServer.expect(connectionTo(url))
        .andRespond(withException(
            new RuntimeException(new WebServiceTransportException("retry 2 failure"))));

    RuntimeException exceptionToSend = new RuntimeException(
        mock(SoapFaultClientException.class));
    mockWebServiceServer.expect(connectionTo(url))
        .andRespond(withException(exceptionToSend));

    ApplicationFile applicationFile = new ApplicationFile(fileContent.getBytes(), fileName);

	filenetWebServiceClient.send(application, applicationFile, olmsted, Document.CAF);

    mockWebServiceServer.verify();
  }

  @Test
  void includesAuthenticationCredentials() {
    when(clock.instant()).thenReturn(
        ZonedDateTime.of(LocalDateTime.of(1293, 12, 7, 1, 42, 17), ZoneOffset.UTC)
            .toInstant());

    mockWebServiceServer.expect(connectionTo(url))
        .andExpect((uri, request) -> {
          Node soapHeaderNode = extractHeaderNodeFromSoapMessage(
              (SaajSoapMessage) request);
          SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
          namespaceContext.bindNamespaceUri("wsse",
              "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
          namespaceContext.bindNamespaceUri("wsu",
              "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
          MatcherAssert.assertThat(soapHeaderNode, Matchers
              .hasXPath("//wsse:Security/wsu:Timestamp/wsu:Created/text()",
                  namespaceContext,
                  Matchers.equalTo("1293-12-07T01:42:17Z")));
          MatcherAssert.assertThat(soapHeaderNode, Matchers
              .hasXPath("//wsse:Security/wsu:Timestamp/wsu:Expires/text()",
                  namespaceContext,
                  Matchers.equalTo("1293-12-07T01:47:17Z")));
          MatcherAssert.assertThat(soapHeaderNode, Matchers
              .hasXPath("//wsse:Security/wsse:UsernameToken/wsse:Username/text()",
                  namespaceContext,
                  Matchers.equalTo(username)));
          MatcherAssert.assertThat(soapHeaderNode, Matchers
              .hasXPath("//wsse:Security/wsse:UsernameToken/wsse:Password/text()",
                  namespaceContext,
                  Matchers.equalTo(password)));
          MatcherAssert.assertThat(soapHeaderNode, Matchers.hasXPath(
              "//wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']",
              namespaceContext));
        })
        .andRespond(withSoapEnvelope(successResponse));

    String routerRequest = String.format("%s/%s", sftpUploadUrl, filenetIdd);
    Mockito.when(restTemplate.getForObject(routerRequest, String.class)).thenReturn(routerResponse);

	filenetWebServiceClient.send(application, new ApplicationFile("whatever".getBytes(), fileName), hennepin,
			Document.CAF);

    mockWebServiceServer.verify();
    Mockito.verify(restTemplate).getForObject(routerRequest, String.class);
  }

  private Node extractHeaderNodeFromSoapMessage(SaajSoapMessage request) {
    DOMResult domResult = (DOMResult) request.getSoapHeader().getResult();
    return domResult.getNode();
  }

}
