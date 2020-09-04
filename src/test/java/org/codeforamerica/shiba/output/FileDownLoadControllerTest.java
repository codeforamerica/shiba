package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.ConfirmationData;
import org.codeforamerica.shiba.County;
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

import java.time.ZonedDateTime;
import java.util.List;

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
    ApplicationInputsMappers mappers = mock(ApplicationInputsMappers.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new FileDownLoadController(
                        xmlGenerator,
                        mappers,
                        confirmationData,
                        applicationRepository,
                        pdfGenerator
                ))
                .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
                .build();
    }

    @Test
    void shouldPassScreensToServiceToGeneratePdfFile() throws Exception {
        when(pdfGenerator.generate(any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));
        ApplicationInput applicationInput1 = new ApplicationInput("screen1", "input 1", List.of("input1Value"), ApplicationInputType.SINGLE_VALUE);
        ApplicationInput applicationInput2 = new ApplicationInput("screen1", "input 1", List.of("something"), ApplicationInputType.SINGLE_VALUE);
        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        when(applicationRepository.find(confirmationData.getId())).thenReturn(application);
        List<ApplicationInput> applicationInputs = List.of(applicationInput1, applicationInput2);
        when(mappers.map(application, CLIENT)).thenReturn(applicationInputs);

        mockMvc.perform(
                get("/download"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate(applicationInputs, confirmationData.getId());
    }

    @Test
    void shouldAcceptApplicationIdToGeneratePdfFile() throws Exception {
        when(pdfGenerator.generate(any(), any())).thenReturn(new ApplicationFile("".getBytes(), ""));
        ApplicationInput applicationInput1 = new ApplicationInput("screen1", "input 1", List.of("input1Value"), ApplicationInputType.SINGLE_VALUE);
        ApplicationInput applicationInput2 = new ApplicationInput("screen1", "input 1", List.of("something"), ApplicationInputType.SINGLE_VALUE);
        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        when(applicationRepository.find("9870000123")).thenReturn(application);
        List<ApplicationInput> applicationInputs = List.of(applicationInput1, applicationInput2);
        when(mappers.map(application, CASEWORKER)).thenReturn(applicationInputs);

        mockMvc.perform(
                get("/download-caf/9870000123"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate(applicationInputs, "9870000123");
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

        ApplicationInput applicationInput1 = new ApplicationInput("screen1", "input 1", List.of("input1Value"), ApplicationInputType.SINGLE_VALUE);
        ApplicationInput applicationInput2 = new ApplicationInput("screen1", "input 1", List.of("something"), ApplicationInputType.SINGLE_VALUE);
        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        when(applicationRepository.find(confirmationData.getId())).thenReturn(application);
        when(mappers.map(application, CLIENT)).thenReturn(List.of(applicationInput1, applicationInput2));

        MvcResult result = mockMvc.perform(
                get("/download-xml"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        verify(xmlGenerator).generate(List.of(applicationInput1, applicationInput2), confirmationData.getId());
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(fileBytes);
    }
}