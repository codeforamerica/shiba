package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.PdfGenerator;
import org.codeforamerica.shiba.xml.XmlGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FileDownLoadControllerTest {
    MockMvc mockMvc;

    XmlGenerator xmlGenerator = mock(XmlGenerator.class);
    Screens screens = new Screens();
    Map<String, FormData> data = new HashMap<>();
    PdfGenerator pdfGenerator = mock(PdfGenerator.class);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new FileDownLoadController(
                        screens,
                        data,
                        pdfGenerator,
                        xmlGenerator))
                .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
                .build();
    }

    @Test
    void shouldPassScreensToServiceToGeneratePdfFile() throws Exception {
        String screenName = "screen1";

        Page page = new Page();
        FormInput input1 = new FormInput();
        String input1Name = "input 1";
        input1.name = input1Name;
        List<String> input1Value = List.of("input1Value");
        input1.type = FormInputType.TEXT;

        FormInput input2 = new FormInput();
        String input2Name = "input 2";
        input2.name = input2Name;
        List<String> input2Value = List.of("input2Value");
        input2.type = FormInputType.TEXT;

        FormInput input3 = new FormInput();
        String input3Name = "input 3";
        input3.name = input3Name;
        List<String> input3Value = List.of("input3Value");
        input3.type = FormInputType.TEXT;

        input2.followUps = List.of(input3);
        page.setInputs(List.of(input1, input2));
        screens.put(screenName, page);

        data.put(screenName, new FormData(Map.of(
                input1Name, new InputData(Validation.NONE, input1Value),
                input2Name, new InputData(Validation.NONE, input2Value),
                input3Name, new InputData(Validation.NONE, input3Value)
        )));

        when(pdfGenerator.generate(any())).thenReturn(new ApplicationFile("".getBytes(), ""));

        mockMvc.perform(
                get("/download"))
                .andExpect(status().is2xxSuccessful());

        verify(pdfGenerator).generate(List.of(
                new ApplicationInput(screenName, input1Value, input1Name, ApplicationInputType.SINGLE_VALUE),
                new ApplicationInput(screenName, input2Value, input2Name, ApplicationInputType.SINGLE_VALUE),
                new ApplicationInput(screenName, input3Value, input3Name, ApplicationInputType.SINGLE_VALUE)
        ));
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

        String screenName = "screen1";

        Page page = new Page();
        FormInput input1 = new FormInput();
        String input1Name = "input 1";
        input1.name = input1Name;
        List<String> input1Value = List.of("input1Value");
        input1.type = FormInputType.TEXT;

        FormInput input2 = new FormInput();
        String input2Name = "input 2";
        input2.name = input2Name;
        List<String> input2Value = List.of("input2Value");
        input2.type = FormInputType.TEXT;

        FormInput input3 = new FormInput();
        String input3Name = "input 3";
        input3.name = input3Name;
        List<String> input3Value = List.of("input3Value");
        input3.type = FormInputType.TEXT;

        input2.followUps = List.of(input3);
        page.setInputs(List.of(input1, input2));
        screens.put(screenName, page);

        data.put(screenName, new FormData(Map.of(
                input1Name, new InputData(Validation.NONE, input1Value),
                input2Name, new InputData(Validation.NONE, input2Value),
                input3Name, new InputData(Validation.NONE, input3Value)
        )));

        MvcResult result = mockMvc.perform(
                get("/download-xml"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        verify(xmlGenerator).generate(List.of(
                new ApplicationInput(screenName, input1Value, input1Name, ApplicationInputType.SINGLE_VALUE),
                new ApplicationInput(screenName, input2Value, input2Name, ApplicationInputType.SINGLE_VALUE),
                new ApplicationInput(screenName, input3Value, input3Name, ApplicationInputType.SINGLE_VALUE)
        ));
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(fileBytes);
    }
}