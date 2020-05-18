package org.codeforamerica.shiba;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PageControllerTest {
    MockMvc mockMvc;

    PDFFieldFiller PDFFieldFiller = mock(PDFFieldFiller.class);
    PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);
    BenefitsApplication benefitsApplication = new BenefitsApplication();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new PageController(
                        benefitsApplication,
                        new StaticMessageSource(),
                        PDFFieldFiller,
                        pdfFieldMapper
                ))
                .build();
    }

    @Test
    void shouldGenerateAPDFForTheApplication() throws Exception {
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setFirstName("Roger");
        benefitsApplication.setPersonalInfo(personalInfo);

        byte[] pdfBytes = "here is the pdf".getBytes();
        String fileName = "filename.pdf";

        PdfFile pdfFile = new PdfFile(pdfBytes, fileName);
        when(PDFFieldFiller.fill(any())).thenReturn(pdfFile);
        List<PDFField> fields = List.of(mock(PDFField.class));
        when(pdfFieldMapper.map(any())).thenReturn(fields);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/download"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        verify(pdfFieldMapper).map(benefitsApplication);
        verify(PDFFieldFiller).fill(fields);
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(pdfBytes);
    }
}