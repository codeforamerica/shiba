package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.codeforamerica.shiba.testutilities.TestUtils.getFileContentsAsByteArray;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.ApplicationStatus;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class FileDownloadControllerTest {

  MockMvc mockMvc;
  XmlGenerator xmlGenerator = mock(XmlGenerator.class);
  ApplicationData applicationData;
  Application application;
  PdfGenerator pdfGenerator = mock(PdfGenerator.class);
  ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
  UploadedDocsPreparer uploadedDocsPreparer = mock(UploadedDocsPreparer.class);

  @BeforeEach
  void setUp() {
    applicationData = new ApplicationData();
    applicationData.setId("some-app-id");
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH, GRH, EA))
        .build());
    application = Application.builder()
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .applicationStatuses(List.of(new ApplicationStatus("", CCAP, "", DELIVERED, "")))
        .build();
    mockMvc = MockMvcBuilders.standaloneSetup(
            new FileDownloadController(
                xmlGenerator,
                pdfGenerator,
                applicationData,
                applicationRepository,
                uploadedDocsPreparer))
        .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
        .build();
    when(applicationRepository.find(any())).thenReturn(application);

  }

  @Test
  void shouldGenerateExpectedPdfsOnDownload() throws Exception {
    when(pdfGenerator.generate(anyString(), any(), any()))
        .thenReturn(new ApplicationFile("Test".getBytes(), "Test"));

    mockMvc.perform(
            get("/download"))
        .andExpect(status().is2xxSuccessful());

    verify(pdfGenerator).generate(applicationData.getId(), CAF, CLIENT);
  }

  @Test
  void shouldReturnTheGeneratedZip() throws Exception {
    byte[] pdfBytes = "here is the pdf".getBytes();
    String fileName = "filename.pdf";
    ApplicationFile applicationFile = new ApplicationFile(pdfBytes, fileName);
    when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(applicationFile);
    when(uploadedDocsPreparer.prepare(any(), any())).thenReturn(List.of(applicationFile));

    mockMvc.perform(get("/download"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
        .andExpect(header()
            .string(HttpHeaders.CONTENT_DISPOSITION,
                String.format("filename=\"MNB_application_%s.zip\"", applicationData.getId())))
        .andReturn();
  }

  @Test
  void shouldGenerateXMLForTheApplication() throws Exception {
    byte[] fileBytes = "some file content".getBytes();
    String fileName = "some.xml";
    when(xmlGenerator.generate(anyString(), any(), any()))
        .thenReturn(new ApplicationFile(fileBytes, fileName));

    MvcResult result = mockMvc.perform(
            get("/download-xml"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
        .andExpect(header()
            .string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
        .andReturn();

    verify(xmlGenerator).generate(applicationData.getId(), CAF, CLIENT);
    assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(fileBytes);
  }

  @Test
  void shouldReturnDocumentsForApplicationId() throws Exception {
    var image = getFileContentsAsByteArray("shiba+file.jpg");
    var wordDoc = getFileContentsAsByteArray("testWord.docx");
    var coverPage = getFileContentsAsByteArray("test-cover-pages.pdf");
    var applicationId = "9870000123";
    ApplicationFile imageFile = new ApplicationFile(image, "");
    ApplicationFile wordDocFile = new ApplicationFile(wordDoc, "");
    ApplicationFile coverPageFile = new ApplicationFile(coverPage, "");
    UploadedDocument uploadedDoc = new UploadedDocument("shiba+file.jpg", "", "", "", image.length);
    UploadedDocument uploadedWordDoc = new UploadedDocument("testWord.docx", "", "", "",
        wordDoc.length);
    ApplicationData applicationData = new ApplicationData();
    applicationData.setId(applicationId);
    applicationData.setUploadedDocs(List.of(uploadedDoc, uploadedWordDoc));
    applicationData.setFlow(FlowType.LATER_DOCS);
    Application application = Application.builder()
        .applicationData(applicationData)
        .flow(FlowType.LATER_DOCS)
        .build();
    mockMvc = MockMvcBuilders.standaloneSetup(
            new FileDownloadController(
                xmlGenerator,
                pdfGenerator,
                applicationData,
                applicationRepository,
                uploadedDocsPreparer))
        .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
        .build();

    when(applicationRepository.find(applicationId)).thenReturn(application);
    when(pdfGenerator.generateCoverPageForUploadedDocs(any(Application.class)))
        .thenReturn(coverPageFile.getFileBytes());
    when(pdfGenerator
        .generateForUploadedDocument(eq(List.of(uploadedDoc,uploadedWordDoc)), any(Application.class), any()))
        .thenReturn(List.of(imageFile,wordDocFile));

    when(uploadedDocsPreparer.prepare(any(), any())).thenReturn(List.of(imageFile, wordDocFile));
    MvcResult result = mockMvc.perform(
            get("/download"))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
            "filename=\"MNB_application_9870000123.zip\""))
        .andExpect(status().is2xxSuccessful())
        .andReturn();

    byte[] actualBytes = result.getResponse().getContentAsByteArray();
    assertThat(actualBytes).hasSizeGreaterThan(22);
  }

  @Test
  void shouldReturnNotFoundMessageWhenAttemptingToDownloadOldLaterDocsApp() throws Exception {
    var applicationId = "9870000123";

    ApplicationData applicationData = new ApplicationData();
    applicationData.setId(applicationId);
    applicationData.setUploadedDocs(List.of(
        new UploadedDocument("shiba+file.jpg", "", "", "",
            getFileContentsAsByteArray("shiba+file.jpg").length)));
    applicationData.setFlow(FlowType.LATER_DOCS);
    Application application = Application.builder()
        .applicationData(applicationData)
        .completedAt(ZonedDateTime.now().minusDays(60).minusSeconds(1)) // One second too old
        .flow(FlowType.LATER_DOCS)
        .build();
    mockMvc = MockMvcBuilders.standaloneSetup(
            new FileDownloadController(
                xmlGenerator,
                pdfGenerator,
                applicationData,
                applicationRepository,
                uploadedDocsPreparer))
        .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
        .build();

    when(applicationRepository.find(applicationId)).thenReturn(application);
    mockMvc.perform(
            get("/download"))
        .andExpect(content().string("Later Docs application " + applicationId
            + " is older than 60 days, supporting documents have been deleted."))
        .andExpect(status().is2xxSuccessful())
        .andReturn();
  }
}
