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
        .applicationData(applicationData)
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

    mockMvc.perform(get("/download"))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
        .andExpect(header()
            .string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"MNB_application_%s.zip\"", applicationData.getId())))
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
    mockMvc = MockMvcBuilders.standaloneSetup(
            new FileDownloadController(
                xmlGenerator,
                pdfGenerator,
                applicationData,
                applicationRepository))
        .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
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
            get("/download"))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "filename=\"MNB_application_9870000123.zip\""))
        .andExpect(status().is2xxSuccessful())
        .andReturn();

    verify(pdfGenerator).generateCoverPageForUploadedDocs(application);
    verify(pdfGenerator)
        .generateForUploadedDocument(uploadedDoc, 0, application, imageFile.getFileBytes());

    byte[] actualBytes = result.getResponse().getContentAsByteArray();

    assertThat(actualBytes).hasSizeGreaterThan(22);
  }
}
