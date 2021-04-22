package org.codeforamerica.shiba.output;

import de.redsix.pdfcompare.PdfComparator;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.MonitoringService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser;
import org.codeforamerica.shiba.application.parsers.DocumentListParser;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.mnit.MnitEsbWebServiceClient;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.mockito.Mockito.*;

class MnitDocumentConsumerTest {
    MnitEsbWebServiceClient mnitClient = mock(MnitEsbWebServiceClient.class);
    XmlGenerator xmlGenerator = mock(XmlGenerator.class);
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);
    ApplicationDataParser<List<Document>> documentListParser = mock(DocumentListParser.class);
    ApplicationData appData = new ApplicationData();
    MonitoringService monitoringService = mock(MonitoringService.class);
    DocumentRepositoryService documentRepositoryService = mock(DocumentRepositoryService.class);
    FileNameGenerator fileNameGenerator = mock(FileNameGenerator.class);
    MnitDocumentConsumer documentConsumer = new MnitDocumentConsumer(
            mnitClient,
            xmlGenerator,
            pdfGenerator,
            documentListParser,
            monitoringService,
            documentRepositoryService,
            fileNameGenerator,
            "test");

    @BeforeEach
    void setUp() {
        appData.setPagesData(new PagesData(Map.of("somePage", new PageData())));
    }

    @Test
    void generatesThePDFFromTheApplicationData() {
        when(documentListParser.parse(any())).thenReturn(List.of(CAF));
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        documentConsumer.process(application);
        verify(pdfGenerator).generate(application.getId(), CAF, CASEWORKER);
    }

    @Test
    void generatesTheXmlFromTheApplicationData() {
        when(documentListParser.parse(any())).thenReturn(List.of(CAF));
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        documentConsumer.process(application);
        verify(xmlGenerator).generate(application.getId(), CAF, CASEWORKER);
    }

    @Test
    void sendsTheGeneratedXmlAndPdf() {
        ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
        when(pdfGenerator.generate(any(), any(), any())).thenReturn(pdfApplicationFile);
        ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
        when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);
        when(documentListParser.parse(any())).thenReturn(List.of(CAF));
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        documentConsumer.process(application);
        verify(mnitClient).send(pdfApplicationFile, County.Olmsted, application.getId(), Document.CAF);
        verify(mnitClient).send(xmlApplicationFile, County.Olmsted, application.getId(), Document.CAF);
    }

    @Test
    void sendsTheCcapPdfIfTheApplicationHasCCAP() {
        ApplicationFile pdfApplicationFile = new ApplicationFile("my pdf".getBytes(), "someFile.pdf");
        when(pdfGenerator.generate(any(), eq(CCAP), any())).thenReturn(pdfApplicationFile);
        ApplicationFile xmlApplicationFile = new ApplicationFile("my xml".getBytes(), "someFile.xml");
        when(xmlGenerator.generate(any(), any(), any())).thenReturn(xmlApplicationFile);
        when(documentListParser.parse(any())).thenReturn(List.of(CCAP, CAF));
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        PageData chooseProgramsPage = new PageData();
        chooseProgramsPage.put("programs", InputData.builder().value(List.of("CCAP")).build());
        pagesData.put("choosePrograms", chooseProgramsPage);
        applicationData.setPagesData(pagesData);
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        documentConsumer.process(application);
        verify(mnitClient).send(pdfApplicationFile, County.Olmsted, application.getId(), Document.CCAP);
    }

    @Test
    void sendsApplicationIdToMonitoringService() {
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        documentConsumer.process(application);
        verify(monitoringService).setApplicationId(application.getId());
    }

    @Test
    void sendsBothImageAndDocumentUploadsSuccessfully() throws IOException {
        var applicationData = new ApplicationData();

        var shibaJpgContents = Files.readAllBytes(getAbsoluteFilepath("shiba+file.jpg"));
        var shibaImageS3Filepath = "someS3FilePath";
        when(documentRepositoryService.get(shibaImageS3Filepath)).thenReturn(shibaJpgContents);
        applicationData.addUploadedDoc(
                new MockMultipartFile("image", "someImage.jpg", MediaType.IMAGE_JPEG_VALUE, shibaJpgContents),
                shibaImageS3Filepath,
                "someDataUrl",
                "image/jpeg");

        var testCafPdfContents = Files.readAllBytes(getAbsoluteFilepath("test-caf.pdf"));
        var pdfApplicationFile = new ApplicationFile(testCafPdfContents, "pdf2of2.pdf");
        var testCafPdfS3Filepath = "coolS3FilePath";
        applicationData.addUploadedDoc(
                new MockMultipartFile("pdf", "somePdf.pdf", MediaType.APPLICATION_PDF_VALUE, testCafPdfContents),
                testCafPdfS3Filepath,
                "documentDataUrl",
                "application/pdf");
        when(documentRepositoryService.get(testCafPdfS3Filepath)).thenReturn(pdfApplicationFile.getFileBytes());

        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(applicationData)
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        when(fileNameGenerator.generateUploadedDocumentName(application, 0, "pdf")).thenReturn("pdf1of2.pdf");
        when(fileNameGenerator.generateUploadedDocumentName(application, 1, "pdf")).thenReturn("pdf2of2.pdf");

        documentConsumer.processUploadedDocuments(application);

        ArgumentCaptor<ApplicationFile> captor = ArgumentCaptor.forClass(ApplicationFile.class);
        verify(mnitClient, times(2)).send(captor.capture(), eq(County.Olmsted), eq(application.getId()), nullable(Document.class));

        // Assert that converted file contents are as expected
        try (var actual = new ByteArrayInputStream(captor.getAllValues().get(0).getFileBytes());
             var expected = Files.newInputStream(getAbsoluteFilepath("shiba+file.pdf"))) {
            assertThatCode(() -> new PdfComparator<>(expected, actual).compare()).doesNotThrowAnyException();
        }
    }

    private Path getAbsoluteFilepath(String resourceFilename) {
        URL resource = this.getClass().getClassLoader().getResource(resourceFilename);
        if (resource != null) {
            return Paths.get((new File(resource.getFile())).getAbsolutePath());
        }
        return Paths.get("");
    }
}