package org.codeforamerica.shiba.mnit;

import com.sun.istack.ByteArrayDataSource;
import com.sun.xml.messaging.saaj.soap.name.NameImpl;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.ApplicationStatusUpdater;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.esbwsdl.*;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.math.BigInteger;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.application.Status.DELIVERY_FAILED;
import static org.codeforamerica.shiba.output.Document.*;

@Component
@Slf4j
public class MnitEsbWebServiceClient {
    private final WebServiceTemplate webServiceTemplate;
    private final Clock clock;
    private final String username;
    private final String password;
    private final CountyMap<MnitCountyInformation> countyMap;
    private final ApplicationStatusUpdater applicationStatusUpdater;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public MnitEsbWebServiceClient(WebServiceTemplate webServiceTemplate,
                                   Clock clock,
                                   @Value("${mnit-esb.username}") String username,
                                   @Value("${mnit-esb.password}") String password,
                                   CountyMap<MnitCountyInformation> countyMap,
                                   ApplicationStatusUpdater applicationStatusUpdater) {
        this.webServiceTemplate = webServiceTemplate;
        this.clock = clock;
        this.username = username;
        this.password = password;
        this.countyMap = countyMap;
        this.applicationStatusUpdater = applicationStatusUpdater;
    }

    @Retryable(
            value = {Exception.class},
            maxAttemptsExpression = "#{${mnit-esb.max-attempts}}",
            backoff = @Backoff(
                    delayExpression = "#{${mnit-esb.delay}}",
                    multiplierExpression = "#{${mnit-esb.multiplier}}",
                    maxDelayExpression = "#{${mnit-esb.max-delay}}"
            ),
            listeners = {"esbRetryListener"}
    )
    public void send(ApplicationFile applicationFile, County county, String applicationNumber, Document applicationDocument, FlowType flowType) {
        MDC.put("applicationFile", applicationFile.getFileName());
        CreateDocument createDocument = new CreateDocument();
        createDocument.setFolderId("workspace://SpacesStore/" + countyMap.get(county).getFolderId());
        createDocument.setRepositoryId("<Unknown");
        createDocument.setTypeId("document");

        CmisPropertiesType properties = new CmisPropertiesType();
        List<CmisProperty> propertyUris = properties.getPropertyUriOrPropertyIdOrPropertyString();
        CmisPropertyString fileNameProperty = createCmisPropertyString("Name", applicationFile.getFileName());
        CmisPropertyString subject = createCmisPropertyString("subject", "MN Benefits Application");
        CmisPropertyString description = createCmisPropertyString("description", generateDocumentDescription(applicationFile, applicationDocument, applicationNumber, flowType));
        CmisPropertyString dhsProviderId = createCmisPropertyString("dhsProviderId", countyMap.get(county).getDhsProviderId());
        propertyUris.addAll(List.of(fileNameProperty, subject, description, description, dhsProviderId));
        createDocument.setProperties(properties);

        CmisContentStreamType contentStream = new CmisContentStreamType();
        contentStream.setLength(BigInteger.ZERO);
        contentStream.setStream(new DataHandler(new ByteArrayDataSource(applicationFile.getFileBytes(), MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)));
        createDocument.setContentStream(contentStream);
        webServiceTemplate.marshalSendAndReceive(createDocument, message -> {
            SOAPMessage soapMessage = ((SaajSoapMessage) message).getSaajMessage();
            try {
                SOAPHeader soapHeader = soapMessage.getSOAPHeader();
                QName securityQName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security", "wsse");
                QName timestampQName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Timestamp", "wsu");
                SOAPHeaderElement securityElement = soapHeader.addHeaderElement(securityQName);

                SOAPElement timestampElement = securityElement.addChildElement(timestampQName);
                SOAPElement createdElement = timestampElement.addChildElement("Created", "wsu");
                ZonedDateTime createdTimestamp = ZonedDateTime.now(clock);
                createdElement.setTextContent(createdTimestamp.format(DateTimeFormatter.ISO_INSTANT));
                SOAPElement expiresElement = timestampElement.addChildElement("Expires", "wsu");
                expiresElement.setTextContent(createdTimestamp.plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT));

                SOAPElement usernameTokenElement = securityElement.addChildElement("UsernameToken", "wsse");
                SOAPElement usernameElement = usernameTokenElement.addChildElement("Username", "wsse");
                usernameElement.setTextContent(username);
                SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
                passwordElement.addAttribute(NameImpl.createFromUnqualifiedName("Type"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
                passwordElement.setTextContent(password);
            } catch (SOAPException e) {
                logErrorToSentry(e, applicationFile, county, applicationNumber, applicationDocument, flowType);
            }
        });
        updateMnitEsbDocumentStatus(applicationDocument, applicationNumber, DELIVERED);
    }

    @Recover
    public void logErrorToSentry(Exception e, ApplicationFile applicationFile, County county, String applicationNumber, Document applicationDocument, FlowType flowType) {
        updateMnitEsbDocumentStatus(applicationDocument, applicationNumber, DELIVERY_FAILED);
        log.error("Application failed to send: " + applicationFile.getFileName(), e);
    }

    @NotNull
    private CmisPropertyString createCmisPropertyString(String property, String value) {
        CmisPropertyString fileNameProperty = new CmisPropertyString();
        fileNameProperty.setName(property);
        fileNameProperty.setValue(value);
        return fileNameProperty;
    }

    @NotNull
    private String generateDocumentDescription(ApplicationFile applicationFile, Document applicationDocument, String applicationNumber, FlowType flowType) {
        String docDescription = String.format("Associated with MNBenefits Application #%s", applicationNumber);
        if (applicationDocument == CAF || applicationDocument == CCAP) {
            if (applicationFile.getFileName().toLowerCase().endsWith(".xml")) {
                docDescription = String.format("XML of MNBenefits Application #%s", applicationNumber);
            } else if (applicationFile.getFileName().toLowerCase().endsWith(".pdf")) {
                docDescription = String.format("PDF of MNBenefits %s Application #%s", applicationDocument.toString(), applicationNumber);
            }
        } else if (applicationDocument == UPLOADED_DOC) {
            if (flowType == LATER_DOCS) {
                docDescription = String.format("PDF of Later Docs of MNbenefits Application #%s", applicationNumber);
            } else {
                docDescription = String.format("PDF of Documents of MNbenefits Application #%s", applicationNumber);
            }
        }
        return docDescription;
    }

    private void updateMnitEsbDocumentStatus(Document applicationDocument, String applicationNumber, Status status) {
        switch (applicationDocument) {
            case CCAP -> applicationStatusUpdater.updateCcapApplicationStatus(applicationNumber, applicationDocument, status);
            case CAF -> applicationStatusUpdater.updateCafApplicationStatus(applicationNumber, applicationDocument, status);
            default -> applicationStatusUpdater.updateUploadedDocumentsStatus(applicationNumber, applicationDocument, status);
        }
    }
}
