package org.codeforamerica.shiba.mnit;

import com.sun.istack.ByteArrayDataSource;
import com.sun.xml.messaging.saaj.soap.name.NameImpl;
import io.sentry.Sentry;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.esbwsdl.CmisContentStreamType;
import org.codeforamerica.shiba.esbwsdl.CmisPropertiesType;
import org.codeforamerica.shiba.esbwsdl.CmisPropertyString;
import org.codeforamerica.shiba.esbwsdl.CreateDocument;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
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

@Component
public class MnitEsbWebServiceClient {
    private final WebServiceTemplate webServiceTemplate;
    private final Clock clock;
    private final String username;
    private final String password;
    private final CountyMap<MnitCountyInformation> countyMap;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public MnitEsbWebServiceClient(WebServiceTemplate webServiceTemplate,
                                   Clock clock,
                                   @Value("${mnit-esb.username}") String username,
                                   @Value("${mnit-esb.password}") String password,
                                   CountyMap<MnitCountyInformation> countyMap) {
        this.webServiceTemplate = webServiceTemplate;
        this.clock = clock;
        this.username = username;
        this.password = password;
        this.countyMap = countyMap;
    }

    public void send(ApplicationFile applicationFile, County county) {
        CreateDocument createDocument = new CreateDocument();
        createDocument.setFolderId("workspace://SpacesStore/" + countyMap.get(county).getFolderId());
        createDocument.setRepositoryId("<Unknown");
        createDocument.setTypeId("document");
        CmisPropertiesType properties = new CmisPropertiesType();
        CmisPropertyString fileNameProperty =
                createCmisPropertyString("Name", applicationFile.getFileName());
        properties.getPropertyUriOrPropertyIdOrPropertyString()
                .add(fileNameProperty);
        CmisPropertyString subject =
                createCmisPropertyString("subject", "MN Benefits Application");
        properties.getPropertyUriOrPropertyIdOrPropertyString()
                .add(subject);
        CmisPropertyString description =
                createCmisPropertyString("description", "Sent by Code for America");
        properties.getPropertyUriOrPropertyIdOrPropertyString()
                .add(description);
        CmisPropertyString dhsProviderId =
                createCmisPropertyString("dhsProviderId", countyMap.get(county).getDhsProviderId());
        properties.getPropertyUriOrPropertyIdOrPropertyString()
                .add(dhsProviderId);
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
                e.printStackTrace();
                Sentry.captureMessage(e.getMessage());
            }
        });
    }

    @NotNull
    private CmisPropertyString createCmisPropertyString(String property, String value) {
        CmisPropertyString fileNameProperty = new CmisPropertyString();
        fileNameProperty.setName(property);
        fileNameProperty.setValue(value);
        return fileNameProperty;
    }


}
