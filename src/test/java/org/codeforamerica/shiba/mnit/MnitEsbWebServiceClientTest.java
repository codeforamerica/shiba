package org.codeforamerica.shiba.mnit;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.ws.client.WebServiceTransportException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Node;

import javax.xml.soap.SOAPException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.SchemaFactory;
import java.io.StringWriter;
import java.net.URL;
import java.time.*;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;
import static org.springframework.ws.test.client.RequestMatchers.connectionTo;
import static org.springframework.ws.test.client.RequestMatchers.xpath;
import static org.springframework.ws.test.client.ResponseCreators.*;

@SpringBootTest(properties = {
        "mnit-esb.url=some-url",
        "mnit-esb.username=someUsername",
        "mnit-esb.password=somePassword",
        "test.counties.Hennepin.folderId=hennepin-folder-id",
        "test.counties.Hennepin.dhsProviderId=whatever-dhs",
        "test.counties.Olmsted.folderId=olmsted-folder-id",
        "test.counties.Olmsted.dhsProviderId=olmsted-dhs-provider-id"
})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class MnitEsbWebServiceClientTest {
    @Autowired
    private WebServiceTemplate webServiceTemplate;

    @Autowired
    private MnitEsbWebServiceClient mnitEsbWebServiceClient;

    @MockBean
    private Clock clock;

    @Value("${mnit-esb.url}")
    private String url;

    @Value("${mnit-esb.username}")
    private String username;

    @Value("${mnit-esb.password}")
    private String password;

    @Value("classpath:object-service-port.wsdl")
    private Resource bodySchema;

    private MockWebServiceServer mockWebServiceServer;

    @MockBean
    private MonitoringService monitoringService;

    private final Map<String, String> namespaceMapping = Map.of("ns2", "http://www.cmis.org/2008/05");
    String fileContent = "fileContent";
    String fileName = "fileName";
    StringSource successResponse = new StringSource("" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'>" +
                "<SOAP-ENV:Body xmlns='http://www.cmis.org/2008/05'>" +
                    "<createDocumentResponse></createDocumentResponse>" +
                "</SOAP-ENV:Body>" +
            "</SOAP-ENV:Envelope>"
    );

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        mockWebServiceServer = MockWebServiceServer.createServer(webServiceTemplate);
    }

    @Test
    void sendsTheDocument() {
        mockWebServiceServer.expect(connectionTo(url))
                .andExpect(xpath("//ns2:createDocument/ns2:folderId", namespaceMapping)
                        .evaluatesTo("workspace://SpacesStore/olmsted-folder-id"))
                .andExpect(xpath("//ns2:createDocument/ns2:properties/ns2:propertyString[@ns2:name='Name']/ns2:value", namespaceMapping)
                        .evaluatesTo(fileName))
                .andExpect(xpath("//ns2:createDocument/ns2:properties/ns2:propertyString[@ns2:name='dhsProviderId']/ns2:value", namespaceMapping)
                        .evaluatesTo("olmsted-dhs-provider-id"))
                .andExpect(xpath("//ns2:createDocument/ns2:repositoryId", namespaceMapping)
                        .evaluatesTo("<Unknown"))
                .andExpect(xpath("//ns2:createDocument/ns2:typeId", namespaceMapping)
                        .evaluatesTo("document"))
                .andExpect(xpath("//ns2:createDocument/ns2:contentStream/ns2:length", namespaceMapping)
                        .evaluatesTo(0))
                .andExpect(xpath("//ns2:createDocument/ns2:contentStream/ns2:stream", namespaceMapping)
                        .evaluatesTo(Base64.getEncoder().encodeToString(fileContent.getBytes())))
                .andRespond(withSoapEnvelope(successResponse));

        mnitEsbWebServiceClient.send(
                new ApplicationFile(fileContent.getBytes(), fileName),
                County.Olmsted
        );

        mockWebServiceServer.verify();
    }

    @Test
    void sendingDocumentRetriesIfSOAPExceptionIsThrown() {
        mockWebServiceServer.expect(connectionTo(url))
                .andRespond(withException(new RuntimeException(new SOAPException("soap exception ahhh"))));

        mockWebServiceServer.expect(connectionTo(url))
                .andRespond(withSoapEnvelope(successResponse));

        mnitEsbWebServiceClient.send(new ApplicationFile(fileContent.getBytes(), fileName), County.Olmsted);

        mockWebServiceServer.verify();
        verifyNoInteractions(monitoringService);
    }

    @Test
    void sendingDocumentRecoveryReportsLastErrorIfSOAPExceptionIsThrown3Times() {
        mockWebServiceServer.expect(connectionTo(url))
                .andRespond(withException(new RuntimeException(new SOAPException("initial failure"))));

        mockWebServiceServer.expect(connectionTo(url))
                .andRespond(withException(new RuntimeException(new SOAPException("retry 1 failure"))));

        mockWebServiceServer.expect(connectionTo(url))
                .andRespond(withException(new RuntimeException(new WebServiceTransportException("retry 2 failure"))));

        RuntimeException exceptionToSend = new RuntimeException(mock(SoapFaultClientException.class));
        mockWebServiceServer.expect(connectionTo(url))
                .andRespond(withException(exceptionToSend));

        mnitEsbWebServiceClient.send(new ApplicationFile(fileContent.getBytes(), fileName), County.Olmsted);

        mockWebServiceServer.verify();
        verify(monitoringService).sendException(exceptionToSend);
    }

    @Test
    void includesAuthenticationCredentials() {
        when(clock.instant()).thenReturn(ZonedDateTime.of(LocalDateTime.of(1293, 12, 7, 1, 42, 17), ZoneOffset.UTC).toInstant());
        mockWebServiceServer.expect(connectionTo(url))
                .andExpect((uri, request) -> {
                    Node soapHeaderNode = extractHeaderNodeFromSoapMessage((SaajSoapMessage) request);
                    SimpleNamespaceContext namespaceContext = new SimpleNamespaceContext();
                    namespaceContext.bindNamespaceUri("wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
                    namespaceContext.bindNamespaceUri("wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
                    MatcherAssert.assertThat(soapHeaderNode, Matchers.hasXPath("//wsse:Security/wsu:Timestamp/wsu:Created/text()", namespaceContext, Matchers.equalTo("1293-12-07T01:42:17Z")));
                    MatcherAssert.assertThat(soapHeaderNode, Matchers.hasXPath("//wsse:Security/wsu:Timestamp/wsu:Expires/text()", namespaceContext, Matchers.equalTo("1293-12-07T01:47:17Z")));
                    MatcherAssert.assertThat(soapHeaderNode, Matchers.hasXPath("//wsse:Security/wsse:UsernameToken/wsse:Username/text()", namespaceContext, Matchers.equalTo(username)));
                    MatcherAssert.assertThat(soapHeaderNode, Matchers.hasXPath("//wsse:Security/wsse:UsernameToken/wsse:Password/text()", namespaceContext, Matchers.equalTo(password)));
                    MatcherAssert.assertThat(soapHeaderNode, Matchers.hasXPath("//wsse:Security/wsse:UsernameToken/wsse:Password[@Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText']", namespaceContext));
                })
                .andExpect((uri, request) -> {
                    Node soapHeaderNode = extractHeaderNodeFromSoapMessage((SaajSoapMessage) request);
                    URL schemaURL = UriComponentsBuilder.fromHttpUrl("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd")
                            .build()
                            .toUri()
                            .toURL();
                    assertThatCode(() -> {
                        Node wsSecurityNode = soapHeaderNode.getFirstChild();
                        String xmlAsString = xmlNodeToXmlString(wsSecurityNode);
                        SchemaFactory.newDefaultInstance()
                                .newSchema(schemaURL)
                                .newValidator()
                                .validate(new StringSource(xmlAsString));
                    }).doesNotThrowAnyException();
                });


        mnitEsbWebServiceClient.send(new ApplicationFile(
                "whatever".getBytes(),
                "someFileName"), County.Hennepin);

        mockWebServiceServer.verify();
    }

    private Node extractHeaderNodeFromSoapMessage(SaajSoapMessage request) {
        DOMResult domResult = (DOMResult) request.getSoapHeader().getResult();
        return domResult.getNode();
    }

    private String xmlNodeToXmlString(Node node) throws TransformerException {
        StringWriter stringWriter = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        return stringWriter.toString();
    }
}