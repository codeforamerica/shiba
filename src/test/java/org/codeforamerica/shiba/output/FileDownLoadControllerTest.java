package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Utils;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.TestUtils.getAbsoluteFilepath;
import static org.codeforamerica.shiba.TestUtils.getFileContentsAsByteArray;
import static org.codeforamerica.shiba.output.Document.*;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FileDownLoadControllerTest {
    MockMvc mockMvc;

    XmlGenerator xmlGenerator = mock(XmlGenerator.class);
    ApplicationData applicationData;
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);
    ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    @BeforeEach
    void setUp() {
        applicationData = new ApplicationData();
        applicationData.setId("some-app-id");
        mockMvc = MockMvcBuilders.standaloneSetup(
                new FileDownLoadController(
                        xmlGenerator,
                        pdfGenerator,
                        applicationEventPublisher,
                        applicationData,
                        applicationRepository))
                .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
                .build();

    }

    @Test
    void shouldPassScreensToServiceToGeneratePdfFile() throws Exception {
        when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        mockMvc.perform(
                get("/download"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate(applicationData.getId(), CAF, CLIENT);
    }

    @Test
    void shouldAcceptApplicationIdToGeneratePdfFile() throws Exception {
        when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        mockMvc.perform(
                get("/download-caf/9870000123"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate("9870000123", CAF, CASEWORKER);
    }

    @Test
    void shouldAcceptApplicationIdToGenerateCCAPPdfFile() throws Exception {
        when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        mockMvc.perform(
                get("/download-ccap/9870000123"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate("9870000123", CCAP, CASEWORKER);
    }

    @ParameterizedTest
    @MethodSource
    void shouldPublishEventWhenDownloadCafIsInvoked(
            String expectedIp, String requestHeader
    ) throws Exception {
        when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        String confirmationNumber = "9870000123";
        mockMvc.perform(
                get("/download-caf/" + confirmationNumber)
                        .with(request -> {
                            request.addHeader("X-FORWARDED-FOR", requestHeader);
                            return request;
                        })
        ).andExpect(status().is2xxSuccessful());

        DownloadCafEvent event = new DownloadCafEvent(confirmationNumber, expectedIp);
        verify(applicationEventPublisher).publishEvent(event);
    }

    @SuppressWarnings("unused")
    static List<Arguments> shouldPublishEventWhenDownloadCafIsInvoked() {
        return List.of(
                Arguments.of("123.123.123", "ip, someIp, 123.123.123, someOtherIp"),
                Arguments.of("<blank>", "")
        );
    }

    @Test
    void shouldReturnBlankIpWhenRequestHeaderIsNull() throws Exception {
        when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        String confirmationNumber = "9870000123";
        mockMvc.perform(
                get("/download-caf/" + confirmationNumber).with(request -> request)
        ).andExpect(status().is2xxSuccessful());

        DownloadCafEvent event = new DownloadCafEvent(confirmationNumber, "<blank>");
        verify(applicationEventPublisher).publishEvent(event);
    }

    @Test
    void shouldReturnTheGeneratedPdf() throws Exception {
        byte[] pdfBytes = "here is the pdf".getBytes();
        String fileName = "filename.pdf";
        ApplicationFile applicationFile = new ApplicationFile(pdfBytes, fileName);
        when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(applicationFile);

        MvcResult result = mockMvc.perform(
                get("/download"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(pdfBytes);
    }

    @Test
    void shouldGenerateXMLForTheApplication() throws Exception {
        byte[] fileBytes = "some file content".getBytes();
        String fileName = "some.xml";
        when(xmlGenerator.generate(anyString(), any(), any())).thenReturn(new ApplicationFile(fileBytes, fileName));

        MvcResult result = mockMvc.perform(
                get("/download-xml"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
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
        when(pdfGenerator.generate(any(Application.class), eq(UPLOADED_DOC), eq(CASEWORKER))).thenReturn(imageFile);
        when(pdfGenerator.generateForUploadedDocument(any(UploadedDocument.class), eq(0), any(Application.class), any())).thenReturn(imageFile);
        MvcResult result = mockMvc.perform(
                get("/download-docs/9870000123"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"",applicationId+".zip" )))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        verify(pdfGenerator).generate(application, UPLOADED_DOC, CASEWORKER);
        verify(pdfGenerator).generateForUploadedDocument(uploadedDoc, 0, application, imageFile.getFileBytes());


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

        when(pdfGenerator.generate(any(Application.class), eq(UPLOADED_DOC), eq(CASEWORKER))).thenReturn(new ApplicationFile(null, null));

        MvcResult result = mockMvc.perform(
                get("/download-docs/9870000123"))
                .andExpect(status().is4xxClientError())
                .andReturn();



        byte[] actualBytes = result.getResponse().getContentAsByteArray();

        assertThat(actualBytes).hasSizeLessThanOrEqualTo(22);
    }
}