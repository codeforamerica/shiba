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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

/**
 * CMIS Service to handle operations within the session.
 * 
 * @author aborroy
 *
 */
@Service
public class MnitCmisFilenetClient
{

    // Set values from "application.properties" file
    @Value("${mnit-filenet.url}")
    String filenetUrl;
    @Value("${mnit-filenet.username}")
    String filenetUsername;
    @Value("${mnit-filenet.password}")
    String filenetPassword;

    // CMIS living session
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
        parameters.put(SessionParameter.REPOSITORY_ID, "Programs");

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
        properties.put("Read",  false);
        properties.put("OriginalFileName", applicationFile.getFileName());
        properties.put(PropertyIds.NAME, applicationFile.getFileName());
        properties.put("FileType", "Misc");
        properties.put("NPI", routingDestination.getDhsProviderId());
        properties.put("MNITSMailboxTransactionType", "OLA");
        properties.put("Source", "MNITS");
        properties.put("Flow", "Inbound");
        properties.put(PropertyIds.OBJECT_TYPE_ID, "MNITSMailbox");
        properties.put("Description", generateDocumentDescription(applicationFile, applicationDocument, applicationNumber, flowType));
        
        //stream
        byte[] documentBytes = applicationFile.getFileBytes();
        InputStream stream = new ByteArrayInputStream(documentBytes);
        ContentStream contentStream = new ContentStreamImpl(applicationFile.getFileName(), BigInteger.valueOf(documentBytes.length),
        		MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE, stream);
        
    	Folder filenetFolder = session.getRootFolder();
    	Document document = null;
    	try {
    		document = filenetFolder.createDocument(properties, contentStream, VersioningState.NONE);
    	} catch(Exception e) {
    		System.out.println("Filenet exception: " + e.getMessage());
    	}
    	if (document != null) {
	        String docId = document.getId();
	        System.out.println("filenet document ID: " + docId);
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