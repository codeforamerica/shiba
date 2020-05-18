package org.codeforamerica.shiba;

import org.junit.jupiter.api.Test;

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
        PersonalInfo personalInfo = new PersonalInfo();
        String firstName = "Roger";
        personalInfo.setFirstName(firstName);
        benefitApplication.setPersonalInfo(personalInfo);

        List<PDFField> form = subject.map(benefitApplication);
        assertThat(form).contains(new SimplePDFField(fieldName, firstName));
    }

    @Test
    void shouldMapBinaryField() {
        Map<String, String> configMap = new HashMap<>();
        String fieldName = "someName";
        configMap.put("languagePreferences.needInterpreter", fieldName);
        PdfFieldMapper subject = new PdfFieldMapper(configMap);

        BenefitsApplication benefitsApplication = new BenefitsApplication();
        LanguagePreferences languagePreferences = new LanguagePreferences();
        languagePreferences.setNeedInterpreter(true);
        benefitsApplication.setLanguagePreferences(languagePreferences);
        List<PDFField> form = subject.map(benefitsApplication);

        assertThat(form).contains(new TogglePDFField(fieldName, true));
    }

    @Test
    void shouldMapEnum() {
        Map<String, String> configMap = new HashMap<>();
        String fieldName = "someName";
        configMap.put("personalInfo.maritalStatus", fieldName);
        BenefitsApplication benefitsApplication = new BenefitsApplication();
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setMaritalStatus(MaritalStatus.MARRIED_LIVING_WITH_SPOUSE);
        benefitsApplication.setPersonalInfo(personalInfo);
        PdfFieldMapper subject = new PdfFieldMapper(configMap);

        List<PDFField> form = subject.map(benefitsApplication);

        assertThat(form).contains(new SimplePDFField(fieldName, MaritalStatus.MARRIED_LIVING_WITH_SPOUSE.toString()));
    }

    @Test
    void shouldHandleNullExpressionResult() {
        Map<String, String> configMap = new HashMap<>();
        String fieldName = "someName";
        configMap.put("null", fieldName);

        BenefitsApplication benefitsApplication = new BenefitsApplication();

        PdfFieldMapper subject = new PdfFieldMapper(configMap);
        List<PDFField> form = subject.map(benefitsApplication);
        assertThat(form).isEmpty();
    }

    @Test
    void shouldMapMultipleChoiceField() {
        HashMap<String, String> configMap = new HashMap<>();
        configMap.put("programSelection.programs.?[#this.name() == 'FOOD']", "FOOD_PDF_FIELD");
        PdfFieldMapper subject = new PdfFieldMapper(configMap);

        BenefitsApplication benefitsApplication = new BenefitsApplication();
        ProgramSelection programSelection = new ProgramSelection();
        programSelection.setPrograms(List.of(FOOD));
        benefitsApplication.setProgramSelection(programSelection);
        List<PDFField> form = subject.map(benefitsApplication);

        assertThat(form).contains(new BinaryPDFField("FOOD_PDF_FIELD"));
    }
}