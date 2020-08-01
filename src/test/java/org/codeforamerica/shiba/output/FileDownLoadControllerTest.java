package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMappers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FileDownLoadControllerTest {
    MockMvc mockMvc;

    XmlGenerator xmlGenerator = mock(XmlGenerator.class);
    ApplicationData data = new ApplicationData();
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);
    ApplicationInputsMappers mappers = mock(ApplicationInputsMappers.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new FileDownLoadController(
                        data,
                        pdfGenerator,
                        xmlGenerator,
                        mappers
                ))
                .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
                .build();
    }

    @Test
    void shouldPassScreensToServiceToGeneratePdfFile() throws Exception {
        when(pdfGenerator.generate(any())).thenReturn(new ApplicationFile("".getBytes(), ""));
        ApplicationInput applicationInput1 = new ApplicationInput("screen1", "input 1", List.of("input1Value"), ApplicationInputType.SINGLE_VALUE);
        ApplicationInput applicationInput2 = new ApplicationInput("screen1", "input 1", List.of("something"), ApplicationInputType.SINGLE_VALUE);
        when(mappers.map(data)).thenReturn(List.of(applicationInput1, applicationInput2));

        mockMvc.perform(
                get("/download"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate(List.of(applicationInput1, applicationInput2));
    }

    @Test
    void shouldReturnTheGeneratedPdf() throws Exception {
        byte[] pdfBytes = "here is the pdf".getBytes();
        String fileName = "filename.pdf";
        ApplicationFile applicationFile = new ApplicationFile(pdfBytes, fileName);
        when(pdfGenerator.generate(any())).thenReturn(applicationFile);

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
        when(xmlGenerator.generate(any())).thenReturn(new ApplicationFile(fileBytes, fileName));

        ApplicationInput applicationInput1 = new ApplicationInput("screen1", "input 1", List.of("input1Value"), ApplicationInputType.SINGLE_VALUE);
        ApplicationInput applicationInput2 = new ApplicationInput("screen1", "input 1", List.of("something"), ApplicationInputType.SINGLE_VALUE);
        when(mappers.map(data)).thenReturn(List.of(applicationInput1, applicationInput2));

        MvcResult result = mockMvc.perform(
                get("/download-xml"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        verify(xmlGenerator).generate(List.of(applicationInput1, applicationInput2));
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(fileBytes);
    }
}