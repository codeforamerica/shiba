package org.codeforamerica.shiba.output;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.EA;
import static org.codeforamerica.shiba.Program.GRH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.application.Status.DELIVERED;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.codeforamerica.shiba.testutilities.TestUtils.getFileContentsAsByteArray;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
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
import org.codeforamerica.shiba.application.DocumentStatus;
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

  @BeforeEach
  void setUp() {
    applicationData = new ApplicationData();
    applicationData.setId("some-app-id");
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH, GRH, EA))
        .build());
    application = Application.builder()
        .completedAt(ZonedDateTime.now())
        .documentStatuses(List.of(new DocumentStatus("", CCAP, "", DELIVERED)))
        .build();
    mockMvc = MockMvcBuilders.standaloneSetup(
            new FileDownloadController(
                xmlGenerator,
                pdfGenerator,
                applicationData,
                applicationRepository))
        .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
        .build();

  }
  @Test
  void shouldPassScreensToServiceWithApplicationIDToGeneratePdfFile() throws Exception {
    
    var applicationId = "9870000123";
    ApplicationData applicationData = new ApplicationData();
    applicationData.setId(applicationId);

    Application application = Application.builder().applicationData(applicationData)
         .build();

    when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(new ApplicationFile("TEST".getBytes(), ""));
    when(applicationRepository.find(applicationId)).thenReturn(application);
    MvcResult result = mockMvc.perform(get("/download/9870000123"))
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                    String.format("filename=\"%s\"", "MNB_application_"+applicationId + ".zip")))
            .andExpect(status().is2xxSuccessful()).andReturn();
    
    byte[] actualBytes = result.getResponse().getContentAsByteArray();
    assertThat(actualBytes).hasSizeGreaterThan(22);
  }
  @Test
  void shouldPassScreensToServiceToGeneratePdfFile() throws Exception {
    when(pdfGenerator.generate(anyString(), any(), any()))
        .thenReturn(new ApplicationFile("".getBytes(), ""));

    mockMvc.perform(
            get("/download"))
        .andExpect(status().is2xxSuccessful());

    verify(pdfGenerator).generate(applicationData.getId(), CAF, CLIENT);
  }

  @Test
  void shouldAcceptApplicationIdToGeneratePdfFile() throws Exception {
    application.setDocumentStatuses(List.of(new DocumentStatus("9870000123", CAF, "", DELIVERED)));
    when(applicationRepository.find("9870000123")).thenReturn(application);
    when(pdfGenerator.generate(eq(application), any(), any()))
        .thenReturn(new ApplicationFile("".getBytes(), ""));

    mockMvc.perform(get("/download-caf/9870000123"))
        .andExpect(status().is2xxSuccessful());

    verify(pdfGenerator).generate(application, CAF, CASEWORKER);
  }

  @Test
  void shouldAcceptApplicationIdToGenerateCCAPPdfFile() throws Exception {
    when(applicationRepository.find("9870000123")).thenReturn(application);
    when(pdfGenerator.generate(eq(application), any(), any()))
        .thenReturn(new ApplicationFile("".getBytes(), ""));

    mockMvc.perform(get("/download-ccap/9870000123"))
        .andExpect(status().is2xxSuccessful());

    verify(pdfGenerator).generate(application, CCAP, CASEWORKER);
  }

  @Test
  void shouldNotifyForNonCAFApplication() throws Exception {
    when(applicationRepository.find("9870000123")).thenReturn(application);

    String responseContent = mockMvc.perform(get("/download-caf/9870000123"))
        .andExpect(status().is2xxSuccessful())
        .andReturn()
        .getResponse().getContentAsString();

    assertThat(responseContent).isEqualTo(
        "Could not find a CAF application with this ID for download");
  }

  @Test
  void shouldNotifyForIncompleteApplication() throws Exception {
    application.setCompletedAt(null);
    when(applicationRepository.find("9870000123")).thenReturn(application);

    String responseContent = mockMvc.perform(get("/download-caf/9870000123"))
        .andExpect(status().is2xxSuccessful())
        .andReturn()
        .getResponse().getContentAsString();

    assertThat(responseContent).isEqualTo(
        "Submitted time was not set for this application. It is either still in progress or the submitted time was cleared for some reason.");
  }

  @Test
  void shouldReturnTheGeneratedPdf() throws Exception {
    byte[] pdfBytes = "here is the pdf".getBytes();
    String fileName = "filename.pdf";
    ApplicationFile applicationFile = new ApplicationFile(pdfBytes, fileName);
    when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(applicationFile);

    MvcResult result = mockMvc.perform(get("/download"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
        .andExpect(header()
            .string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
        .andReturn();

    assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(pdfBytes);
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
    var applicationId = "9870000123";
    ApplicationFile imageFile = new ApplicationFile(image, "");
    UploadedDocument uploadedDoc = new UploadedDocument("shiba+file.jpg", "", "", "", image.length);
    ApplicationData applicationData = new ApplicationData();
    applicationData.setId(applicationId);
    applicationData.setUploadedDocs(List.of(uploadedDoc));
    applicationData.setFlow(FlowType.LATER_DOCS);
    Application application = Application.builder()
        .applicationData(applicationData)
        .flow(FlowType.LATER_DOCS)
        .build();

    when(applicationRepository.find(applicationId)).thenReturn(
        application
    );
    when(pdfGenerator.generateCoverPageForUploadedDocs(any(Application.class)))
        .thenReturn(imageFile.getFileBytes());
    when(pdfGenerator
        .generateForUploadedDocument(any(UploadedDocument.class), eq(0), any(Application.class),
            any())).thenReturn(imageFile);
    MvcResult result = mockMvc.perform(
            get("/download-docs/9870000123"))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
            String.format("filename=\"%s\"", "MNB_application_"+applicationId + ".zip")))
        .andExpect(status().is2xxSuccessful())
        .andReturn();

    verify(pdfGenerator).generateCoverPageForUploadedDocs(application);
    verify(pdfGenerator)
        .generateForUploadedDocument(uploadedDoc, 0, application, imageFile.getFileBytes());

    byte[] actualBytes = result.getResponse().getContentAsByteArray();

    assertThat(actualBytes).hasSizeGreaterThan(22);
  }

  @Test
  void shouldReturn404StatusForApplicationIdWithoutDocuments() throws Exception {
    var applicationId = "9870000123";
    ApplicationData applicationData = new ApplicationData();
    applicationData.setId(applicationId);
    applicationData.setFlow(FlowType.LATER_DOCS);
    Application application = Application.builder()
        .applicationData(applicationData)
        .flow(FlowType.LATER_DOCS)
        .build();

    when(applicationRepository.find(applicationId)).thenReturn(
        application
    );

    when(pdfGenerator.generate(any(Application.class), eq(UPLOADED_DOC), eq(CASEWORKER)))
        .thenReturn(new ApplicationFile(null, null));

    MvcResult result = mockMvc.perform(
            get("/download-docs/9870000123"))
        .andExpect(status().is4xxClientError())
        .andReturn();

    byte[] actualBytes = result.getResponse().getContentAsByteArray();

    assertThat(actualBytes).hasSizeLessThanOrEqualTo(22);
  }
}
