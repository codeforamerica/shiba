package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Document.XML;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.xml.messaging.saaj.soap.name.NameImpl;
import java.math.BigInteger;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.filenetwsdl.CmisContentStreamType;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertiesType;
import org.codeforamerica.shiba.filenetwsdl.CmisProperty;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyBoolean;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyId;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyString;
import org.codeforamerica.shiba.filenetwsdl.CreateDocument;
import org.codeforamerica.shiba.filenetwsdl.CreateDocumentResponse;
import org.codeforamerica.shiba.filenetwsdl.ObjectFactory;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

@Component
@Slf4j
public class FilenetWebServiceClient {

  private static final String APPLICATION_PDF = "application/pdf";
  private static final String APPLICATION_XML = "application/xml";
  private final WebServiceTemplate filenetWebServiceTemplate;
  private final Clock clock;
  private final String username;
  private final String password;
  private final String sftpUploadUrl;
  private final ApplicationStatusRepository applicationStatusRepository;

  @Autowired
  private RestTemplate restTemplate;

  public FilenetWebServiceClient(
      @Qualifier("filenetWebServiceTemplate") WebServiceTemplate webServiceTemplate,
      Clock clock,
      @Value("${mnit-filenet.username}") String username,
      @Value("${mnit-filenet.password}") String password,
      @Value("${mnit-filenet.sftp-upload-url}") String sftpUploadUrl,
      ApplicationStatusRepository applicationStatusRepository) {
    this.filenetWebServiceTemplate = webServiceTemplate;
    this.clock = clock;
    this.username = username;
    this.password = password;
    this.sftpUploadUrl = sftpUploadUrl;
    this.applicationStatusRepository = applicationStatusRepository;
  }

  @Retryable(
      value = {Exception.class},
      maxAttemptsExpression = "#{${mnit-filenet.max-attempts}}",
      backoff = @Backoff(
          delayExpression = "#{${mnit-filenet.delay}}",
          multiplierExpression = "#{${mnit-filenet.multiplier}}",
          maxDelayExpression = "#{${mnit-filenet.max-delay}}"
      ),
      listeners = {"esbRetryListener"}
  )
  public void send(ApplicationFile applicationFile,
      RoutingDestination routingDestination,
      String applicationNumber,
      Document applicationDocument, FlowType flowType) {
    log.info("Now sending %s to recipient %s for application %s".formatted(
        applicationDocument.name(),
        routingDestination.getName(),
        applicationNumber));
    MDC.put("applicationFile", applicationFile.getFileName());
    MDC.put("applicationId", applicationNumber);
    CreateDocument createDocument = new CreateDocument();
    createDocument.setRepositoryId("Programs");
    setPropertiesOnDocument(applicationFile, routingDestination, applicationNumber,
        applicationDocument, flowType, createDocument);
    setContentStreamOnDocument(applicationFile, createDocument);

    // Create the document in Filenet
    CreateDocumentResponse response = (CreateDocumentResponse) filenetWebServiceTemplate
        .marshalSendAndReceive(createDocument, message -> {
          SOAPMessage soapMessage = ((SaajSoapMessage) message).getSaajMessage();
          try {
            SOAPHeader soapHeader = soapMessage.getSOAPHeader();
            QName securityQName = new QName(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
                "Security", "wsse");
            QName timestampQName = new QName(
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
                "Timestamp", "wsu");
            SOAPHeaderElement securityElement = soapHeader.addHeaderElement(securityQName);

            SOAPElement timestampElement = securityElement.addChildElement(timestampQName);
            SOAPElement createdElement = timestampElement.addChildElement("Created", "wsu");
            ZonedDateTime createdTimestamp = ZonedDateTime.now(clock);
            createdElement.setTextContent(createdTimestamp.format(DateTimeFormatter.ISO_INSTANT));
            SOAPElement expiresElement = timestampElement.addChildElement("Expires", "wsu");
            expiresElement
                .setTextContent(
                    createdTimestamp.plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT));

            SOAPElement usernameTokenElement = securityElement
                .addChildElement("UsernameToken", "wsse");
            SOAPElement usernameElement = usernameTokenElement
                .addChildElement("Username", "wsse");
            usernameElement.setTextContent(username);
            SOAPElement passwordElement = usernameTokenElement
                .addChildElement("Password", "wsse");
            passwordElement.addAttribute(NameImpl.createFromUnqualifiedName("Type"),
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
            passwordElement.setTextContent(password);
          } catch (SOAPException e) {
            throw new IllegalStateException(e);
          }
        });

    // Now route a copy of the document from Filenet to SFTP
    String idd = response.getObjectId();
    String routerRequest = String.format("%s/%s", sftpUploadUrl, idd);
    log.info(String.format("Upload to SFTP request: %s", routerRequest));
    String routerResponse = restTemplate.getForObject(routerRequest, String.class);

    log.info(String.format("Upload to SFTP response: %s", routerResponse));
    JsonObject jsonObject = new Gson().fromJson(routerResponse, JsonObject.class);

    // Throw exception if this isnt a successful response
    String eMessage = String
        .format("The MNIT Router did not respond with a \"Success\" message for %s", idd);
    if (jsonObject != null) {
      JsonElement messageElement = jsonObject.get("message");
      if (messageElement.isJsonNull()
          || !messageElement.getAsString().equalsIgnoreCase("Success")) {
        throw new IllegalStateException(eMessage);
      }
    } else {
      throw new IllegalStateException(eMessage);
    }

    applicationStatusRepository.createOrUpdate(applicationNumber, applicationDocument,
        routingDestination.getName(),
        DELIVERED, applicationFile.getFileName());
  }

  @SuppressWarnings("unused")
  @Recover
  public void logErrorToSentry(Exception e, ApplicationFile applicationFile,
      RoutingDestination routingDestination,
      String applicationNumber, Document applicationDocument, FlowType flowType) {
    applicationStatusRepository.createOrUpdate(applicationNumber, applicationDocument,
        routingDestination.getName(),
        DELIVERY_FAILED, applicationFile.getFileName());
    log.error("Application failed to send: " + applicationFile.getFileName(), e);
  }

  private void setPropertiesOnDocument(ApplicationFile applicationFile,
      RoutingDestination routingDestination, String applicationNumber,
      Document applicationDocument, FlowType flowType,
      CreateDocument createDocument) {
    CmisPropertiesType properties = new CmisPropertiesType();
    List<CmisProperty> propertiesList = properties.getProperty();

    CmisPropertyBoolean read = createCmisPropertyBoolean();
    CmisPropertyString originalFileName = createCmisPropertyString("OriginalFileName",
        applicationFile.getFileName());
    CmisPropertyString cmisName = createCmisPropertyString("cmis:name",
        applicationFile.getFileName());
    CmisPropertyString fileType = createCmisPropertyString("FileType", "Misc");
    CmisPropertyString npi = createCmisPropertyString("NPI", routingDestination.getDhsProviderId());
    CmisPropertyString mnitsMailboxTransactionType = createCmisPropertyString(
        "MNITSMailboxTransactionType", "OLA");
    CmisPropertyString description = createCmisPropertyString("Description",
        generateDocumentDescription(applicationDocument, applicationNumber, flowType));
    CmisPropertyString source = createCmisPropertyString("Source", "MNITS");
    CmisPropertyString flow = createCmisPropertyString("Flow", "Inbound");
    CmisPropertyId cmisObjectTypeId = createCmisPropertyId();

    propertiesList
        .addAll(
            List.of(read, originalFileName, cmisName, fileType, npi, mnitsMailboxTransactionType,
                source, flow, cmisObjectTypeId, description));
    createDocument.setProperties(properties);
  }

  @NotNull
  private CmisPropertyString createCmisPropertyString(String propertyDefinitionId,
      String propertyValue) {
    CmisPropertyString stringProperty = new CmisPropertyString();
    stringProperty.setPropertyDefinitionId(propertyDefinitionId);
    stringProperty.getValue().add(propertyValue);
    return stringProperty;
  }

  @NotNull
  private CmisPropertyBoolean createCmisPropertyBoolean() {
    CmisPropertyBoolean booleanProperty = new CmisPropertyBoolean();
    booleanProperty.setPropertyDefinitionId("Read");
    booleanProperty.getValue().add(false);
    return booleanProperty;
  }

  @NotNull
  private CmisPropertyId createCmisPropertyId() {
    CmisPropertyId idProperty = new CmisPropertyId();
    idProperty.setPropertyDefinitionId("cmis:objectTypeId");
    idProperty.getValue().add("MNITSMailbox");
    return idProperty;
  }

  private void setContentStreamOnDocument(ApplicationFile applicationFile,
      CreateDocument createDocument) {

    String fileName = applicationFile.getFileName();
    String mimeType = MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
    if (fileName.toLowerCase().endsWith(".pdf")) {
      mimeType = APPLICATION_PDF;
    } else if (fileName.toLowerCase().endsWith(".xml")) {
      mimeType = APPLICATION_XML;
    }

    CmisContentStreamType contentStream = new CmisContentStreamType();
    contentStream.setLength(BigInteger.ZERO);
    contentStream.setStream(new DataHandler(new ByteArrayDataSource(applicationFile.getFileBytes(),
        mimeType)));

    ObjectFactory ob = new ObjectFactory();
    JAXBElement<CmisContentStreamType> jaxbContentStream = ob.createCreateDocumentContentStream(
        contentStream);
    jaxbContentStream.getValue().setMimeType(mimeType);
    createDocument.setContentStream(jaxbContentStream);
  }

  @NotNull
  private String generateDocumentDescription(Document applicationDocument, String applicationNumber,
      FlowType flowType) {
    String docDescription = String
        .format("Associated with MNBenefits Application #%s", applicationNumber);
    if (applicationDocument == CAF || applicationDocument == CCAP) {
      docDescription = String
          .format("PDF of MNBenefits %s Application #%s", applicationDocument, applicationNumber);
    } else if (applicationDocument == XML) {
      docDescription = String.format("XML of MNBenefits Application #%s", applicationNumber);
    } else if (applicationDocument == UPLOADED_DOC) {
      if (flowType == LATER_DOCS) {
        docDescription = String
            .format("PDF of Later Docs of MNbenefits Application #%s", applicationNumber);
      } else {
        docDescription = String
            .format("PDF of Documents of MNbenefits Application #%s", applicationNumber);
      }
    }
    return docDescription;
  }

}
