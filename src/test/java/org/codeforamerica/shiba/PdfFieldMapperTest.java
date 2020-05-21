package org.codeforamerica.shiba;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.BenefitProgram.FOOD;

class PdfFieldMapperTest {

    @Test
    void shouldMapStringsToSimpleFields() {
        Map<String, String> configMap = new HashMap<>();
        String fieldName = "someName";
        configMap.put("personalInfo.firstName", fieldName);
        PdfFieldMapper subject = new PdfFieldMapper(configMap);

        BenefitsApplication benefitApplication = new BenefitsApplication();
        String firstName = "Roger";
        PersonalInfo personalInfo = PersonalInfo.builder().firstName(firstName).build();
        benefitApplication.setPersonalInfo(personalInfo);

        List<PDFField> fields = subject.map(benefitApplication);
        assertThat(fields).contains(new SimplePDFField(fieldName, firstName));
    }

    @Test
    void shouldMapBooleansToToggleFields() {
        Map<String, String> configMap = new HashMap<>();
        String fieldName = "someName";
        configMap.put("languagePreferences.needInterpreter", fieldName);
        PdfFieldMapper subject = new PdfFieldMapper(configMap);

        BenefitsApplication benefitsApplication = new BenefitsApplication();
        LanguagePreferences languagePreferences = new LanguagePreferences();
        languagePreferences.setNeedInterpreter(true);
        benefitsApplication.setLanguagePreferences(languagePreferences);
        List<PDFField> fields = subject.map(benefitsApplication);

        assertThat(fields).contains(new TogglePDFField(fieldName, true));
    }

    @Test
    void shouldMapEnumsToSimpleFields() {
        Map<String, String> configMap = new HashMap<>();
        String fieldName = "someName";
        configMap.put("personalInfo.maritalStatus", fieldName);
        BenefitsApplication benefitsApplication = new BenefitsApplication();
        PersonalInfo personalInfo = PersonalInfo.builder().maritalStatus(MaritalStatus.MARRIED_LIVING_WITH_SPOUSE).build();
        benefitsApplication.setPersonalInfo(personalInfo);
        PdfFieldMapper subject = new PdfFieldMapper(configMap);

        List<PDFField> fields = subject.map(benefitsApplication);

        assertThat(fields).contains(new SimplePDFField(fieldName, MaritalStatus.MARRIED_LIVING_WITH_SPOUSE.toString()));
    }

    @Test
    void shouldMapDatesToDateFields() {
        Map<String, String> configMap = new HashMap<>();
        String fieldName = "someName";
        configMap.put("personalInfo.dateOfBirth", fieldName);
        BenefitsApplication benefitsApplication = new BenefitsApplication();
        LocalDate localDate = LocalDate.of(2020, 1, 31);
        PersonalInfo personalInfo = PersonalInfo.builder()
                .dateOfBirth(localDate)
                .build();
        benefitsApplication.setPersonalInfo(personalInfo);
        PdfFieldMapper subject = new PdfFieldMapper(configMap);

        List<PDFField> fields = subject.map(benefitsApplication);

        assertThat(fields).contains(new DatePDFField(fieldName, localDate));
    }

    @Test
    void shouldHandleNullExpressionResult() {
        Map<String, String> configMap = new HashMap<>();
        String fieldName = "someName";
        configMap.put("null", fieldName);

        BenefitsApplication benefitsApplication = new BenefitsApplication();

        PdfFieldMapper subject = new PdfFieldMapper(configMap);
        List<PDFField> fields = subject.map(benefitsApplication);
        assertThat(fields).isEmpty();
    }

    @Test
    void shouldMapListElementsToBinaryFields() {
        HashMap<String, String> configMap = new HashMap<>();
        configMap.put("programSelection.programs.?[#this.name() == 'FOOD']", "FOOD_PDF_FIELD");
        PdfFieldMapper subject = new PdfFieldMapper(configMap);

        BenefitsApplication benefitsApplication = new BenefitsApplication();
        ProgramSelection programSelection = new ProgramSelection();
        programSelection.setPrograms(List.of(FOOD));
        benefitsApplication.setProgramSelection(programSelection);
        List<PDFField> fields = subject.map(benefitsApplication);

        assertThat(fields).contains(new BinaryPDFField("FOOD_PDF_FIELD"));
    }
}