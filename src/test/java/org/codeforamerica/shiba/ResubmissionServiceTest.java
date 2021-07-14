package org.codeforamerica.shiba;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.Status;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.emails.MailGunEmailClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.output.Document.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResubmissionServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private MailGunEmailClient emailClient;

    @Mock
    private PdfGenerator pdfGenerator;

    private CountyMap<MnitCountyInformation> countyMap = new CountyMap<>();

    private ResubmissionService resubmissionService;

    final String defaultEmail = "defaultEmail";

    @BeforeEach
    void setUp() {
        countyMap.setDefaultValue(MnitCountyInformation.builder()
                .folderId("defaultFolderId")
                .dhsProviderId("defaultDhsProviderId")
                .email(defaultEmail)
                .build());
        resubmissionService = new ResubmissionService(applicationRepository, emailClient, countyMap, pdfGenerator);
    }

    @Test
    void itResubmitsCafs() {
        String appId = "myappid";
        Application application = Application.builder().id(appId).county(Olmsted).build();
        when(applicationRepository.getApplicationIdsToResubmit()).thenReturn(Map.of(CAF, List.of(appId)));
        when(applicationRepository.find(appId)).thenReturn(application);

        ApplicationFile applicationFile = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
        when(pdfGenerator.generate(application, CAF, Recipient.CASEWORKER)).thenReturn(applicationFile);

        resubmissionService.resubmitFailedApplications();

        verify(emailClient).resubmitFailedEmail(defaultEmail, CAF, applicationFile, application, ENGLISH);
        verify(applicationRepository).updateStatus(appId, CAF, Status.DELIVERED);
    }

    @Test
    void itResubmitsUploadedDocuments() {
        String appId = "myappid";
        ApplicationData applicationData = new ApplicationData();
        MockMultipartFile image = new MockMultipartFile("image", "test".getBytes());
        applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");
        applicationData.addUploadedDoc(image, "someS3FilePath2", "someDataUrl2", "image/jpeg");

        Application application = Application.builder().id(appId).county(Olmsted).applicationData(applicationData).build();
        when(applicationRepository.getApplicationIdsToResubmit()).thenReturn(Map.of(UPLOADED_DOC, List.of(appId)));
        when(applicationRepository.find(appId)).thenReturn(application);

        ApplicationFile applicationFile1 = new ApplicationFile("test".getBytes(), "fileName.txt");
        ApplicationFile applicationFile2 = new ApplicationFile("test".getBytes(), "fileName.txt");
        var coverPage = "someCoverPageText".getBytes();
        when(pdfGenerator.generate(application, UPLOADED_DOC, Recipient.CASEWORKER)).thenReturn(new ApplicationFile(coverPage,"coverPage"));
        var uploadedDocs = applicationData.getUploadedDocs();
        when(pdfGenerator.generateForUploadedDocument(uploadedDocs.get(0),0, application, coverPage)).thenReturn(applicationFile1);
        when(pdfGenerator.generateForUploadedDocument(uploadedDocs.get(1),1, application, coverPage)).thenReturn(applicationFile2);

        resubmissionService.resubmitFailedApplications();

        ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
        verify(emailClient, times(2)).resubmitFailedEmail(eq(defaultEmail), eq(UPLOADED_DOC), captor.capture(), eq(application), eq(ENGLISH));

        List<ApplicationFile> applicationFiles = captor.getAllValues();
        assertThat(applicationFiles).containsExactlyElementsOf(List.of(applicationFile1, applicationFile2));
        verify(applicationRepository).updateStatus(appId, UPLOADED_DOC, Status.DELIVERED);
    }

    @Test
    void itResubmitsCCAPAndUploadedDocuments() {
        String appId = "myappid";
        ApplicationData applicationData = new ApplicationData();
        MockMultipartFile image = new MockMultipartFile("image", "test".getBytes());
        applicationData.addUploadedDoc(image, "someS3FilePath", "someDataUrl", "image/jpeg");

        Application application = Application.builder().id(appId).county(Olmsted).applicationData(applicationData).build();
        Map<Document, List<String>> map = new HashMap<>();
        map.put(CCAP, List.of(appId));
        map.put(UPLOADED_DOC, List.of(appId));
        when(applicationRepository.getApplicationIdsToResubmit()).thenReturn(map);
        when(applicationRepository.find(appId)).thenReturn(application);

        ApplicationFile applicationFile1 = new ApplicationFile("test".getBytes(), "fileName.txt");
        var coverPage = "someCoverPageText".getBytes();
        when(pdfGenerator.generate(application, UPLOADED_DOC, Recipient.CASEWORKER)).thenReturn(new ApplicationFile(coverPage,"coverPage"));
        var uploadedDocs = applicationData.getUploadedDocs();
        when(pdfGenerator.generateForUploadedDocument(uploadedDocs.get(0),0, application, coverPage)).thenReturn(applicationFile1);

        ApplicationFile applicationFile2 = new ApplicationFile("fileContent".getBytes(), "fileName.txt");
        when(pdfGenerator.generate(application, CCAP, Recipient.CASEWORKER)).thenReturn(applicationFile2);

        resubmissionService.resubmitFailedApplications();

        ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
        ArgumentCaptor<Document> captor2 = ArgumentCaptor.forClass(Document.class);
        verify(emailClient, times(2)).resubmitFailedEmail(eq(defaultEmail), captor2.capture(), captor.capture(), eq(application), eq(ENGLISH));

        List<ApplicationFile> applicationFiles = captor.getAllValues();
        List<Document> documents = captor2.getAllValues();
        assertThat(applicationFiles).containsExactlyInAnyOrder(applicationFile1, applicationFile2);
        assertThat(documents).containsExactlyInAnyOrder(UPLOADED_DOC, CCAP);

        ArgumentCaptor<Document> captor3 = ArgumentCaptor.forClass(Document.class);
        verify(applicationRepository, times(2)).updateStatus(eq(appId), captor3.capture(), eq(Status.DELIVERED));
        List<Document> docs = captor3.getAllValues();
        assertThat(docs).containsExactlyInAnyOrder(UPLOADED_DOC, CCAP);

    }
}