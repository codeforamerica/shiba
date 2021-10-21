package org.codeforamerica.shiba.mnit;

import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


//Commenting out @Service so Spring doesn't auto-discover this component in prod mode and attempt the atompub connection in @PostConstruct. Remove test profile when we switch to this implementation.
//@Service
@Slf4j
public class MnitCmisFilenetClient
{
    private static final String PROGRAMS = "Programs";
    private static final String READ = "Read";
    private static final String ORIGINAL_FILE_NAME = "OriginalFileName";
    private static final String FILE_TYPE = "FileType";
    private static final String MISC = "Misc";
    private static final String NPI = "NPI";
    private static final String MNITS_MAILBOX_TRANSACTION_TYPE = "MNITSMailboxTransactionType";
    private static final String OLA = "OLA";
    private static final String SOURCE = "Source";
    private static final String MNITS = "MNITS";
    private static final String FLOW = "Flow";
    private static final String INBOUND = "Inbound";
    private static final String MNITS_MAILBOX = "MNITSMailbox";
    private static final String DESCRIPTION = "Description";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String APPLICATION_PDF = "application/pdf";
    private static final String APPLICATION_XML = "application/xml";
    
    @Value("${mnit-filenet.url}")
    String filenetUrl;
    @Value("${mnit-filenet.username}")
    String filenetUsername;
    @Value("${mnit-filenet.password}")
    String filenetPassword;

    private Session session;

    @PostConstruct
    public void init()
    {
        Map<String, String> parameters = new HashMap<String, String>();

        // authentication
        parameters.put(SessionParameter.USER, filenetUsername);
        parameters.put(SessionParameter.PASSWORD, filenetPassword);

        // connection settings
        parameters.put(SessionParameter.ATOMPUB_URL, filenetUrl);
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        parameters.put(SessionParameter.REPOSITORY_ID, PROGRAMS);

        SessionFactory factory = SessionFactoryImpl.newInstance();
        session = factory.createSession(parameters);
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
    public void send(ApplicationFile applicationFile, RoutingDestination routingDestination,
    	      String applicationNumber, org.codeforamerica.shiba.output.Document applicationDocument, FlowType flowType) {
    	
    	//properties
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(READ,  false);
        properties.put(ORIGINAL_FILE_NAME, applicationFile.getFileName());
        properties.put(PropertyIds.NAME, applicationFile.getFileName());
        properties.put(FILE_TYPE, MISC);
        properties.put(NPI, routingDestination.getDhsProviderId());
        properties.put(MNITS_MAILBOX_TRANSACTION_TYPE, "OLA");
        properties.put(SOURCE, MNITS);
        properties.put(FLOW, INBOUND);
        properties.put(PropertyIds.OBJECT_TYPE_ID, MNITS_MAILBOX);
        properties.put("Description", generateDocumentDescription(applicationFile, applicationDocument, applicationNumber, flowType));
        
        //stream
        byte[] documentBytes = applicationFile.getFileBytes();
        InputStream stream = new ByteArrayInputStream(documentBytes);
        String fileName = applicationFile.getFileName();
        String mimeType = APPLICATION_OCTET_STREAM;
        if (fileName.toLowerCase().endsWith(".pdf")) {
        	mimeType = APPLICATION_PDF;
        } else if (fileName.toLowerCase().endsWith(".xml")) {
        	mimeType = APPLICATION_XML;
        }
        ContentStream contentStream = new ContentStreamImpl(fileName, BigInteger.valueOf(documentBytes.length), mimeType, stream );
        
    	Folder filenetFolder = session.getRootFolder();
    	Document document = null;
    	try {
    		document = filenetFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
    	} catch(Exception e) {
    		log.error("Filenet exception={}" , e);
    	}
    	if (document != null) {
	        String docId = document.getId();
	        log.info("filenet document ID: " + docId);
    	}
    }
    
    @NotNull
    private String generateDocumentDescription(ApplicationFile applicationFile,
    		org.codeforamerica.shiba.output.Document applicationDocument, String applicationNumber, FlowType flowType) {
      String docDescription = String
          .format("Associated with MNBenefits Application #%s", applicationNumber);
      if (applicationDocument == CAF || applicationDocument == CCAP) {
        if (applicationFile.getFileName().toLowerCase().endsWith(".xml")) {
          docDescription = String.format("XML of MNBenefits Application #%s", applicationNumber);
        } else if (applicationFile.getFileName().toLowerCase().endsWith(".pdf")) {
          docDescription = String
              .format("PDF of MNBenefits %s Application #%s", applicationDocument,
                  applicationNumber);
        }
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