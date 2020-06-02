package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.PdfField;
import org.codeforamerica.shiba.pdf.PdfFieldFiller;
import org.codeforamerica.shiba.pdf.PdfFieldMapper;
import org.codeforamerica.shiba.xml.XmlGenerator;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PageControllerTest {
    MockMvc mockMvc;

    PdfFieldFiller pdfFieldFiller = mock(PdfFieldFiller.class);
    PdfFieldMapper pdfFieldMapper = mock(PdfFieldMapper.class);
    XmlGenerator xmlGenerator = mock(XmlGenerator.class);
    BenefitsApplication benefitsApplication = new BenefitsApplication();
    Screens screens = new Screens();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new PageController(
                        benefitsApplication,
                        new StaticMessageSource(),
                        pdfFieldFiller,
                        pdfFieldMapper,
                        xmlGenerator,
                        screens))
                .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
                .build();
    }

    @Test
    void shouldTranslateMonthDayYearIntoDate() throws Exception {
        mockMvc.perform(post("/personal-info")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("firstName", "Robert")
                .param("lastName", "Doolittle")
                .param("birthMonth", "12")
                .param("birthDay", "22")
                .param("birthYear", "1983")
                .param("moveToMNMonth", "02")
                .param("moveToMNDay", "07")
                .param("moveToMNYear", "1988"));

        assertThat(benefitsApplication.getPersonalInfo().getDateOfBirth())
                .isEqualTo(LocalDate.of(1983, 12, 22));
        assertThat(benefitsApplication.getPersonalInfo().getMoveToMNDate())
                .isEqualTo(LocalDate.of(1988, 2, 7));

        mockMvc.perform(post("/personal-info")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("firstName", "Robert")
                .param("lastName", "Doolittle")
                .param("birthMonth", "")
                .param("birthDay", "")
                .param("birthYear", "")
                .param("moveToMNMonth", "")
                .param("moveToMNDay", "")
                .param("moveToMNYear", ""));

        assertThat(benefitsApplication.getPersonalInfo().getDateOfBirth()).isNull();
        assertThat(benefitsApplication.getPersonalInfo().getMoveToMNDate()).isNull();
    }

    @Test
    void shouldTranslateToSeparateFieldsForDates() throws Exception {
        benefitsApplication.setPersonalInfo(PersonalInfo.builder()
                .dateOfBirth(LocalDate.of(2000, 4, 2))
                .moveToMNDate(LocalDate.of(2010, 3, 12))
                .build());
        mockMvc.perform(get("/personal-info"))
                .andExpect(model().attribute("personalInfoForm", hasProperty("birthMonth", Matchers.equalTo("04"))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("birthDay", Matchers.equalTo("02"))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("birthYear", Matchers.equalTo("2000"))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("moveToMNMonth", Matchers.equalTo("03"))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("moveToMNDay", Matchers.equalTo("12"))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("moveToMNYear", Matchers.equalTo("2010"))));

        benefitsApplication.setPersonalInfo(PersonalInfo.builder()
                .dateOfBirth(null)
                .moveToMNDate(null)
                .build());
        mockMvc.perform(get("/personal-info"))
                .andExpect(model().attribute("personalInfoForm", hasProperty("birthMonth", Matchers.equalTo(""))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("birthDay", Matchers.equalTo(""))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("birthYear", Matchers.equalTo(""))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("moveToMNMonth", Matchers.equalTo(""))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("moveToMNDay", Matchers.equalTo(""))))
                .andExpect(model().attribute("personalInfoForm", hasProperty("moveToMNYear", Matchers.equalTo(""))));
    }

    @Test
    void shouldGenerateAPDFForTheApplication() throws Exception {
        PersonalInfo personalInfo = PersonalInfo.builder().firstName("Roger").build();
        benefitsApplication.setPersonalInfo(personalInfo);

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
        byte[] pdfBytes = "here is the pdf".getBytes();
        String fileName = "filename.pdf";

        ApplicationFile applicationFile = new ApplicationFile(pdfBytes, fileName);
        when(pdfFieldFiller.fill(any())).thenReturn(applicationFile);
        List<PdfField> fields = List.of(mock(PdfField.class));
        when(pdfFieldMapper.map(any())).thenReturn(fields);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/download"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        verify(pdfFieldMapper).map(Map.of(
                "screen1", List.of(input1, inputWithFollowUps, input3)
        ));
        verify(pdfFieldFiller).fill(fields);
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(pdfBytes);
    }

    @Test
    void shouldGenerateXMLForTheApplication() throws Exception {
        byte[] fileBytes = "some file content".getBytes();
        String fileName = "some.xml";
        when(xmlGenerator.generate(any())).thenReturn(new ApplicationFile(fileBytes, fileName));
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/download-xml"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, String.format("filename=\"%s\"", fileName)))
                .andReturn();

        verify(xmlGenerator).generate(benefitsApplication);
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(fileBytes);
    }
}