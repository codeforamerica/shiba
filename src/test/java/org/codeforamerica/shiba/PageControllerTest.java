package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.PdfGenerator;
import org.codeforamerica.shiba.xml.FileGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PageControllerTest {
    MockMvc mockMvc;

    FileGenerator xmlGenerator = mock(FileGenerator.class);
    BenefitsApplication benefitsApplication = new BenefitsApplication();
    Screens screens = new Screens();
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new PageController(
                        benefitsApplication,
                        screens,
                        new StaticMessageSource(),
                        xmlGenerator,
                        pdfGenerator))
                .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
                .build();
    }

    @Test
    void shouldPassScreensToServiceToGeneratePdfFile() throws Exception {
        Form form1 = new Form();

        FormInput input1 = new FormInput();
        input1.name = "input 1";
        input1.type = FormInputType.TEXT;

        FormInput input2 = new FormInput();
        input2.name = "input 2";
        input2.type = FormInputType.TEXT;
        input2.setType(FormInputType.INPUT_WITH_FOLLOW_UP);

        FormInputWithFollowUps inputWithFollowUps = new FormInputWithFollowUps();
        inputWithFollowUps.name = "inputWithFollowUps";
        inputWithFollowUps.type = FormInputType.TEXT;

        FormInput input3 = new FormInput();
        input3.name = "input 3";
        input3.type = FormInputType.TEXT;

        inputWithFollowUps.setFollowUps(List.of(input3));
        input2.setInputWithFollowUps(inputWithFollowUps);
        form1.setInputs(List.of(input1, input2));
        screens.put("screen1", form1);

        when(pdfGenerator.generate(any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        mockMvc.perform(
                MockMvcRequestBuilders.get("/download"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate(Map.of(
                "screen1", List.of(input1, inputWithFollowUps, input3)
        ));
    }

    @Test
    void shouldReturnTheGeneratedPdf() throws Exception {
        byte[] pdfBytes = "here is the pdf".getBytes();
        String fileName = "filename.pdf";
        ApplicationFile applicationFile = new ApplicationFile(pdfBytes, fileName);
        when(pdfGenerator.generate(any())).thenReturn(applicationFile);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/download"))
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

        Form form1 = new Form();

        FormInput input1 = new FormInput();
        input1.name = "input 1";
        input1.type = FormInputType.TEXT;

        FormInput input2 = new FormInput();
        input2.name = "input 2";
        input2.type = FormInputType.TEXT;
        input2.setType(FormInputType.INPUT_WITH_FOLLOW_UP);

        FormInputWithFollowUps inputWithFollowUps = new FormInputWithFollowUps();
        inputWithFollowUps.name = "inputWithFollowUps";
        inputWithFollowUps.type = FormInputType.TEXT;

        FormInput input3 = new FormInput();
        input3.name = "input 3";
        input3.type = FormInputType.TEXT;

        inputWithFollowUps.setFollowUps(List.of(input3));
        input2.setInputWithFollowUps(inputWithFollowUps);
        form1.setInputs(List.of(input1, input2));
        screens.put("screen1", form1);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/download-xml"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        verify(xmlGenerator).generate(Map.of(
                "screen1", List.of(input1, inputWithFollowUps, input3)
        ));
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(fileBytes);
    }
}