package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.application.Status.SENDING;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Document.XML;

import java.math.BigInteger;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.commons.text.StringEscapeUtils;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationStatusRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.filenetwsdl.CmisContentStreamType;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertiesType;
import org.codeforamerica.shiba.filenetwsdl.CmisProperty;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyBoolean;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyDateTime;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyId;
import org.codeforamerica.shiba.filenetwsdl.CmisPropertyString;
import org.codeforamerica.shiba.filenetwsdl.CreateDocument;
import org.codeforamerica.shiba.filenetwsdl.CreateDocumentResponse;
import org.codeforamerica.shiba.filenetwsdl.ObjectFactory;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.pages.data.InputData;
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.xml.messaging.saaj.soap.name.NameImpl;

import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.soap.SOAPHeaderElement;
import jakarta.xml.soap.SOAPMessage;
import lombok.extern.slf4j.Slf4j;

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
      retryFor = {Exception.class},
      maxAttempts = 4,
      maxAttemptsExpression = "#{${mnit-filenet.max-attempts}}",
      backoff = @Backoff(
          delayExpression = "#{${mnit-filenet.delay}}",
          multiplierExpression = "#{${mnit-filenet.multiplier}}",
          maxDelayExpression = "#{${mnit-filenet.max-delay}}"
      ),
      listeners = {"esbRetryListener"}
  )
  public void send(Application application, ApplicationFile applicationFile,
      RoutingDestination routingDestination, Document applicationDocument) {
    String applicationNumber = application.getId();
    String filenetId;
    filenetId = applicationStatusRepository.find(applicationNumber,
        applicationDocument,
        routingDestination.getName(),
        applicationFile.getFileName()).getFilenetId();
    if (filenetId.isEmpty()) {
      log.info("Now sending %s to recipient %s for application %s".formatted(
          applicationDocument.name(),
          routingDestination.getName(),
          applicationNumber));
      MDC.put("applicationFile", applicationFile.getFileName());
      MDC.put("applicationId", applicationNumber);
      CreateDocument createDocument = new CreateDocument();
      createDocument.setRepositoryId("Programs");
      setPropertiesOnDocument(application, applicationFile, routingDestination, applicationNumber,
          applicationDocument, createDocument);
      setContentStreamOnDocument(applicationFile, createDocument);

      // send the document to Filenet
      CreateDocumentResponse response = null;
	try {
		response = (CreateDocumentResponse) filenetWebServiceTemplate
		      .marshalSendAndReceive(createDocument, message -> {
		        SOAPMessage soapMessage = ((SaajSoapMessage) message).getSaajMessage();
		        try {
		          SOAPHeader soapHeader = (SOAPHeader) soapMessage.getSOAPHeader();
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
		          createdElement.setTextContent(
		              createdTimestamp.format(DateTimeFormatter.ISO_INSTANT));
		          SOAPElement expiresElement = timestampElement.addChildElement("Expires", "wsu");
		          expiresElement
		              .setTextContent(
		                  createdTimestamp.plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT));

		          SOAPElement usernameTokenElement = securityElement
		              .addChildElement("UsernameToken", "wsse");
		          SOAPElement usernameElement = usernameTokenElement
		              .addChildElement("Username", "wsse");
		          usernameElement.setTextContent(username);
		          SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
		          passwordElement.addAttribute((Name) NameImpl.createFromUnqualifiedName("Type"),
		              "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
		          passwordElement.setTextContent(password);
		        } catch (SOAPException e) {
		        	log.error("Filenet SOAP Error", e);
		          throw new IllegalStateException(e);
		        }catch (Exception e) {
		        	log.error("Filenet WS Error", e);
		        }
		      });
	} catch (Exception e) {
		// I/O error: Connection reset by peer errors need to be sent to logs
		log.error("ESB Error", e);
	}

      // Save filenetId so we can retry only sending to SFTP
      filenetId = response.getObjectId();
      applicationStatusRepository.updateFilenetId(applicationNumber, applicationDocument,
          routingDestination.getName(),
          SENDING, applicationFile.getFileName(), filenetId);
    }

    // Now route a copy of the document from Filenet to SFTP (unless it is from HEALTHCARE_RENEWAL)
	if (!application.getFlow().equals(FlowType.HEALTHCARE_RENEWAL)) {
		String routerRequest = String.format("%s/%s", sftpUploadUrl, filenetId);
		log.info(StringEscapeUtils.escapeJava(String.format("Upload to SFTP request: %s", routerRequest)));
		String routerResponse = restTemplate.getForObject(routerRequest, String.class);

		log.info(StringEscapeUtils.escapeJava(String.format("Upload to SFTP response: %s", routerResponse)));
		JsonObject jsonObject = new Gson().fromJson(routerResponse, JsonObject.class);

		// Throw exception if this isnt a successful response
		String eMessage = String.format("The MNIT Router did not respond with a \"Success\" message for %s", filenetId);
		if (jsonObject != null) {
			JsonElement messageElement = jsonObject.get("message");
			if (messageElement.isJsonNull() || !messageElement.getAsString().equalsIgnoreCase("Success")) {
				throw new IllegalStateException(eMessage);
			}
		} else {
			throw new IllegalStateException(eMessage);
		}
	}
    
    applicationStatusRepository.createOrUpdate(applicationNumber, applicationDocument,
        routingDestination.getName(),
        DELIVERED, applicationFile.getFileName());
  }

  @SuppressWarnings("unused")
  @Recover
  public void logErrorToSentry(Exception e, Application application, ApplicationFile applicationFile,
	      RoutingDestination routingDestination, Document applicationDocument) {
    applicationStatusRepository.createOrUpdate(application.getId(), applicationDocument,
        routingDestination.getName(),
        DELIVERY_FAILED, applicationFile.getFileName());
    log.error("Application failed to send: " + applicationFile.getFileName(), e);
  }

	private void setPropertiesOnDocument(Application application, ApplicationFile applicationFile,
			RoutingDestination routingDestination, String applicationNumber, Document applicationDocument,
			CreateDocument createDocument) {
		FlowType flowType = application.getFlow();
		if (flowType.equals(FlowType.HEALTHCARE_RENEWAL)) {
			setPropertiesOnHealthcareRenewalDocument(application, applicationFile, routingDestination,
					applicationNumber, applicationDocument, flowType, createDocument);
		} else {
			setPropertiesOnOlaDocument(applicationFile, routingDestination, applicationNumber, applicationDocument,
					flowType, createDocument);
		}
	}

  private void setPropertiesOnOlaDocument(ApplicationFile applicationFile,		  
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
    CmisPropertyId cmisObjectTypeId = createCmisPropertyId("MNITSMailbox");

    propertiesList
        .addAll(
            List.of(read, originalFileName, cmisName, fileType, npi, mnitsMailboxTransactionType,
                source, flow, cmisObjectTypeId, description));
    createDocument.setProperties(properties);
  }

	private void setPropertiesOnHealthcareRenewalDocument(Application application, ApplicationFile applicationFile,
			RoutingDestination routingDestination, String applicationNumber, Document applicationDocument,
			FlowType flowType, CreateDocument createDocument) {
		CmisPropertiesType properties = new CmisPropertiesType();
		List<CmisProperty> propertiesList = properties.getProperty();

		CmisPropertyBoolean read = createCmisPropertyBoolean();
		CmisPropertyString originalFileName = createCmisPropertyString("OriginalFileName",
				applicationFile.getFileName());
		CmisPropertyString cmisName = createCmisPropertyString("cmis:name", applicationFile.getFileName());
		CmisPropertyString fileType = createCmisPropertyString("CTYMBDocumentType", "RENEWAL");
		CmisPropertyDateTime dateTime = createCmisPropertyDateTime("DocumentDate", application.getCompletedAt());
		String caseNumber = "";
		InputData inputData = application.getApplicationData().getPageData("healthcareRenewalMatchInfo").get("caseNumber");
		if (inputData != null) {
			caseNumber = inputData.getValue(0);
		}
		CmisPropertyString caseId = createCmisPropertyString("CaseID", caseNumber);
		CmisPropertyString referenceNumber = createCmisPropertyString("ReferenceNumber", applicationNumber);
		CmisPropertyString npi = createCmisPropertyString("NPI", routingDestination.getDhsProviderId());
		CmisPropertyString source = createCmisPropertyString("Source", "MNBenefits");
		CmisPropertyId cmisObjectTypeId = createCmisPropertyId("CountyMailbox");

		propertiesList.addAll(List.of(read, originalFileName, cmisName, fileType, dateTime, caseId, referenceNumber,
				npi, source, cmisObjectTypeId));
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
  private CmisPropertyId createCmisPropertyId(String value) {
    CmisPropertyId idProperty = new CmisPropertyId();
    idProperty.setPropertyDefinitionId("cmis:objectTypeId");
    idProperty.getValue().add(value);
    return idProperty;
  }

  @NotNull
  private CmisPropertyDateTime createCmisPropertyDateTime(String id, ZonedDateTime value) {
    CmisPropertyDateTime datetimeProperty = new CmisPropertyDateTime();
    datetimeProperty.setPropertyDefinitionId(id);
    // convert date/time to XMLGregorianCalendar type
    List<XMLGregorianCalendar> datetime = datetimeProperty.getValue();
    GregorianCalendar gregorianCalendar = GregorianCalendar.from(value);
    XMLGregorianCalendar xmlGregorianCalendar = null;
    try {
    	xmlGregorianCalendar = DatatypeFactory
            .newInstance()
            .newXMLGregorianCalendar(gregorianCalendar);
    } catch(Exception e) {
    	log.error(StringEscapeUtils.escapeJava(String.format("Failed to convert ZonedDateTime %s to XMLGregorianCalendar", value.toString())), e);
    }
    datetime.add(xmlGregorianCalendar);
    return datetimeProperty;
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
    jakarta.mail.util.ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(applicationFile.getFileBytes(), mimeType);
    DataHandler dataHandler = new DataHandler(byteArrayDataSource);
    contentStream.setStream(dataHandler);
    ObjectFactory ob = new ObjectFactory();
    JAXBElement<CmisContentStreamType> jaxbContentStream = ob.createCreateDocumentContentStream(contentStream);
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
