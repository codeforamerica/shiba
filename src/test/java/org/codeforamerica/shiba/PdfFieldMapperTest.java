package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.PdfFieldMapperTest.TestEnum.TEST_ENUM;

class PdfFieldMapperTest {
    static class TestObject {
        public TestEnum enumValue;
        public String stringValue;
        public Boolean booleanValue;
        public LocalDate dateValue;
        public List<Object> list1;
        public List<Object> list2;
    }

    enum TestEnum {
        TEST_ENUM
    }

    @Test
    void shouldMapStringsToSimpleFields() {
        String fieldName = "someName";
        Map<String, String> configMap = Map.of("stringValue", fieldName);
        PdfFieldMapper subject = new PdfFieldMapper(configMap, emptySet());

        TestObject benefitApplication = new TestObject();
        String stringValue = "some-string-value";
        benefitApplication.stringValue = stringValue;

        List<PdfField> fields = subject.map(benefitApplication);
        assertThat(fields).contains(new SimplePdfField(fieldName, stringValue));
    }

    @Test
    void shouldMapBooleansToToggleFields() {
        String fieldName = "someName";
        PdfFieldMapper subject = new PdfFieldMapper(Map.of("booleanValue", fieldName), emptySet());

        TestObject testObject = new TestObject();
        testObject.booleanValue = true;
        List<PdfField> fields = subject.map(testObject);

        assertThat(fields).contains(new TogglePdfField(fieldName, true));
    }

    @Test
    void shouldMapDatesToDateFields() {
        String fieldName = "someName";
        TestObject testObject = new TestObject();
        LocalDate localDate = LocalDate.of(2020, 1, 31);
        PdfFieldMapper subject = new PdfFieldMapper(Map.of("dateValue", fieldName), emptySet());

        testObject.dateValue = localDate;

        List<PdfField> fields = subject.map(testObject);

        assertThat(fields).contains(new DatePdfField(fieldName, localDate));
    }

    @Test
    void shouldHandleNullExpressionResult() {
        String fieldName = "someName";

        TestObject benefitsApplication = new TestObject();

        PdfFieldMapper subject = new PdfFieldMapper(Map.of("null", fieldName), emptySet());
        List<PdfField> fields = subject.map(benefitsApplication);
        assertThat(fields).isEmpty();
    }

    @Test
    void shouldMapListElementsToBinaryFields() {
        String pdfField = "PDF_FIELD";
        String notPresentPdfField = "NOT_PRESENT_PDF_FIELD";
        PdfFieldMapper subject = new PdfFieldMapper(Map.of(
                "list1", pdfField,
                "list2", notPresentPdfField
        ), emptySet());

        TestObject testObject = new TestObject();
        testObject.list1 = List.of("a");
        testObject.list2 = Collections.emptyList();
        List<PdfField> fields = subject.map(testObject);

        assertThat(fields).containsOnly(
                new BinaryPdfField(pdfField, true),
                new BinaryPdfField(notPresentPdfField, false)
        );
    }

    @Test
    void shouldMapEnumsToSimpleFields() {
        String fieldName = "someName";
        Map<String, String> configMap = Map.of("enumValue", fieldName);
        TestObject testObject = new TestObject();
        testObject.enumValue = TEST_ENUM;
        PdfFieldMapper subject = new PdfFieldMapper(configMap, Set.of());

        List<PdfField> fields = subject.map(testObject);

        assertThat(fields).contains(new SimplePdfField(fieldName, TEST_ENUM.toString()));
    }

    @Test
    void shouldNotMapExcludedEnums() {
        TestObject testObject = new TestObject();
        testObject.enumValue = TEST_ENUM;
        Map<String, String> configMap = Map.of("enumValue", "PDF_FIELD");
        PdfFieldMapper subject = new PdfFieldMapper(configMap, Set.of(TEST_ENUM));

        List<PdfField> fields = subject.map(testObject);

        assertThat(fields).isEmpty();
    }
}