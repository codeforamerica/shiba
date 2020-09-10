package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.ConfirmationData;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FileDownLoadControllerTest {
    MockMvc mockMvc;

    XmlGenerator xmlGenerator = mock(XmlGenerator.class);
    ConfirmationData confirmationData = new ConfirmationData();
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new FileDownLoadController(
                        xmlGenerator,
                        confirmationData,
                        pdfGenerator
                ))
                .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
                .build();
    }

    @Test
    void shouldPassScreensToServiceToGeneratePdfFile() throws Exception {
        when(pdfGenerator.generate(any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        mockMvc.perform(
                get("/download"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate(confirmationData.getId(), CLIENT);
    }

    @Test
    void shouldAcceptApplicationIdToGeneratePdfFile() throws Exception {
        when(pdfGenerator.generate(any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        mockMvc.perform(
                get("/download-caf/9870000123"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate("9870000123", CASEWORKER);
    }

    @Test
    void shouldReturnTheGeneratedPdf() throws Exception {
        byte[] pdfBytes = "here is the pdf".getBytes();
        String fileName = "filename.pdf";
        ApplicationFile applicationFile = new ApplicationFile(pdfBytes, fileName);
        when(pdfGenerator.generate(any(), any())).thenReturn(applicationFile);

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
        when(xmlGenerator.generate(any(), any())).thenReturn(new ApplicationFile(fileBytes, fileName));

        MvcResult result = mockMvc.perform(
                get("/download-xml"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        verify(xmlGenerator).generate(confirmationData.getId(), CLIENT);
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(fileBytes);
    }
}