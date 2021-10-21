package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.Base64;
import java.util.Map;

import javax.xml.soap.SOAPException;
import javax.xml.transform.dom.DOMResult;

import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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
class MnitFilenetWebServiceClientTest {

  
  private final Map<String, String> namespaceMapping = Map
	      .of("ns2", "http://docs.oasis-open.org/ns/cmis/messaging/200908/", "ns3",
	    	  "http://docs.oasis-open.org/ns/cmis/core/200908/",
	    	  "cmism", "http://docs.oasis-open.org/cmis/CMIS/v1.1/errata01/os/schema/CMIS-Messaging.xsd");
  String fileContent = "fileContent";
  String fileName = "fileName";
  StringSource successResponse = new StringSource("" +
      "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'>" +
      "<soapenv:Body>" +
      "<b:createDocumentResponse xmlns:b='http://docs.oasis-open.org/ns/cmis/messaging/200908/'>" +
      "</b:createDocumentResponse>" +
      "</soapenv:Body>" +
      "</soapenv:Envelope>"
  );
  @Autowired
  @Qualifier("filenetWebServiceTemplate")
  private WebServiceTemplate webServiceTemplate;
  @Autowired
  private MnitFilenetWebServiceClient mnitFilenetWebServiceClient;
  @MockBean
  private Clock clock;
  @Value("${mnit-filenet.url}")
  private String url;
  @Value("${mnit-filenet.username}")
  private String username;
  @Value("${mnit-filenet.password}")
  private String password;
  private MockWebServiceServer mockWebServiceServer;
  @MockBean
  private ApplicationRepository applicationRepository;

  @MockBean
  protected FeatureFlagConfiguration featureFlagConfiguration;

  private RoutingDestination olmsted;
  private RoutingDestination hennepin;

  @BeforeEach
  void setUp() {
    when(clock.instant()).thenReturn(Instant.now());
    when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    mockWebServiceServer = MockWebServiceServer.createServer(webServiceTemplate);
    olmsted = new CountyRoutingDestination();
    olmsted.setDhsProviderId("A000055800");
    olmsted.setFolderId("6875aa2f-8852-426f-a618-d394b9a32be5");

    hennepin = new CountyRoutingDestination();
    hennepin.setDhsProviderId("A000027200");
    hennepin.setFolderId("5195b061-9bdc-4d31-9840-90a99902d329");
  }

  //TODO: namespaces change order. Need to figure out how to use a wildcard in the xpath assertions.  
  @Disabled
  @Test
  void sendsTheDocument() {
    mockWebServiceServer.expect(connectionTo(url))
        .andExpect(xpath("//ns2:createDocument/ns2:repositoryId", namespaceMapping)
            .evaluatesTo("Programs"))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyBoolean[@propertyDefinitionId='Read']/ns3:value", namespaceMapping)
            .evaluatesTo(true))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='OriginalFileName']/ns3:value", namespaceMapping)
            .evaluatesTo("fileName"))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='FileType']/ns3:value", namespaceMapping)
                .evaluatesTo("Misc"))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='cmis:name']/ns3:value", namespaceMapping)
                .evaluatesTo("fileName"))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='NPI']/ns3:value", namespaceMapping)
                .evaluatesTo("A000055800"))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyId[@propertyDefinitionId='cmis:objectTypeId']/ns3:value", namespaceMapping)
                .evaluatesTo("MNITSMailbox"))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='MNITSMailboxTransactionType']/ns3:value", namespaceMapping)
                .evaluatesTo("OLA"))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='Source']/ns3:value", namespaceMapping)
                .evaluatesTo("MNITS"))
        .andExpect(xpath("//ns2:createDocument/ns2:properties/ns3:propertyString[@propertyDefinitionId='Flow']/ns3:value", namespaceMapping)
                .evaluatesTo("Inbound"))
        .andExpect(
            xpath("//ns2:createDocument/ns3:ContentStream/ns3:length", namespaceMapping)
                .evaluatesTo(0))
        .andExpect(
            xpath("//ns2:createDocument/ns3:contentStream/ns3:stream", namespaceMapping)
                .evaluatesTo(Base64.getEncoder().encodeToString(fileContent.getBytes())))
        .andRespond(withSoapEnvelope(successResponse));

    RoutingDestination routingDestination = new CountyRoutingDestination();
    routingDestination.setDhsProviderId("A000055800");
    routingDestination.setFolderId("6875aa2f-8852-426f-a618-d394b9a32be5");

    mnitFilenetWebServiceClient.send(
        new ApplicationFile(fileContent.getBytes(), fileName),
        routingDestination, "someId", Document.CAF, FlowType.FULL
    );

    verify(applicationRepository).updateStatus("someId", Document.CAF, DELIVERED);

    mockWebServiceServer.verify();
  }

  @Test
  void sendingDocumentRetriesIfSOAPExceptionIsThrown() {
    mockWebServiceServer.expect(connectionTo(url))
    .andRespond(withException(
            new RuntimeException(new SOAPException("soap exception ahhh"))));

    mockWebServiceServer.expect(connectionTo(url))
        .andRespond(withSoapEnvelope(successResponse));

    mnitFilenetWebServiceClient
        .send(new ApplicationFile(fileContent.getBytes(), fileName), olmsted,
            "someId",
            Document.CAF, any());

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

    ApplicationFile applicationFile = new ApplicationFile(fileContent.getBytes(), "someFile");

    mnitFilenetWebServiceClient
        .send(applicationFile, olmsted, "someId", Document.CAF, any());

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
        });

    mnitFilenetWebServiceClient.send(new ApplicationFile(
        "whatever".getBytes(),
        "someFileName"), hennepin, "someId", Document.CAF, FlowType.FULL);

    mockWebServiceServer.verify();
  }

  private Node extractHeaderNodeFromSoapMessage(SaajSoapMessage request) {
    DOMResult domResult = (DOMResult) request.getSoapHeader().getResult();
    return domResult.getNode();
  }

}
