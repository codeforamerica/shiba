package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pdf.PdfField;
import org.codeforamerica.shiba.pdf.PdfFieldMapper;
import org.codeforamerica.shiba.pdf.SimplePdfField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

class PdfFieldMapperTest {
    @ParameterizedTest
    @EnumSource(names = {"TEXT", "NUMBER", "RADIO"}, value = FormInputType.class)
    void shouldMapTextInputToSimpleField(FormInputType formInputType) {
        String fieldName = "someName";
        String formInputName = "some-input";
        String screenName = "some-screen";
        Map<String, String> configMap = Map.of(screenName + "." + formInputName, fieldName);

        String stringValue = "some-string-value";
        FormInput formInput = new FormInput();
        formInput.type = formInputType;
        formInput.value = List.of(stringValue);
        formInput.name = formInputName;

        PdfFieldMapper subject = new PdfFieldMapper(configMap, emptySet());
        List<PdfField> fields = subject.map(Map.of(screenName, List.of(formInput)));

        assertThat(fields).contains(new SimplePdfField(fieldName, stringValue));
    }

    @Test
    void shouldNotMapInputWhenValueIsExcludedFromPdf() {
        String fieldName = "someName";
        String formInputName = "some-input";
        String screenName = "some-screen";
        Map<String, String> configMap = Map.of(screenName + "." + formInputName, fieldName);
        String excludedValue = "excluded radio selection";

        FormInput formInput = new FormInput();
        formInput.type = FormInputType.RADIO;
        formInput.value = List.of(excludedValue);
        formInput.name = formInputName;

        PdfFieldMapper subject = new PdfFieldMapper(configMap, Set.of(excludedValue));
        List<PdfField> fields = subject.map(Map.of(screenName, List.of(formInput)));

        assertThat(fields).isEmpty();
    }

    @Test
    void shouldMapDatesToSimpleFields() {
        String fieldName = "someName";
        String formInputName = "some-input";
        String screenName = "some-screen";
        Map<String, String> configMap = Map.of(screenName + "." + formInputName, fieldName);

        FormInput formInput = new FormInput();
        formInput.type = FormInputType.DATE;
        formInput.value = List.of("01", "20", "3312");
        formInput.name = formInputName;

        PdfFieldMapper subject = new PdfFieldMapper(configMap, emptySet());
        List<PdfField> fields = subject.map(Map.of(screenName, List.of(formInput)));

        assertThat(fields).contains(new SimplePdfField(fieldName, "01/20/3312"));
    }

    @Test
    void shouldNotMapInputsWithoutPdfFieldMappings() {
        String formInputName = "some-input";
        String screenName = "some-screen";

        FormInput formInput = new FormInput();
        formInput.type = FormInputType.TEXT;
        formInput.value = List.of("someValue");
        formInput.name = formInputName;

        PdfFieldMapper subject = new PdfFieldMapper(Map.of(), emptySet());
        List<PdfField> fields = subject.map(Map.of(screenName, List.of(formInput)));

        assertThat(fields).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = FormInputType.class)
    void shouldNotMapInputsWithoutValues(FormInputType formInputType) {
        String fieldName = "someName";
        String formInputName = "some-input";
        String screenName = "some-screen";
        Map<String, String> configMap = Map.of(screenName + "." + formInputName, fieldName);

        FormInput formInput = new FormInput();
        formInput.type = formInputType;
        formInput.value = null;
        formInput.name = formInputName;

        PdfFieldMapper subject = new PdfFieldMapper(configMap, emptySet());
        List<PdfField> fields = subject.map(Map.of(screenName, List.of(formInput)));

        assertThat(fields).isEmpty();
    }
//
//    @Test
//    void shouldMapListElementsToBinaryFields() {
//        String pdfField = "PDF_FIELD";
//        String notPresentPdfField = "NOT_PRESENT_PDF_FIELD";
//        PdfFieldMapper subject = new PdfFieldMapper(Map.of(
//                "list1", pdfField,
//                "list2", notPresentPdfField
//        ), emptySet());
//
//        TestObject testObject = new TestObject();
//        testObject.list1 = List.of("a");
//        testObject.list2 = Collections.emptyList();
//        List<PdfField> fields = subject.map(testObject);
//
//        assertThat(fields).containsOnly(
//                new BinaryPdfField(pdfField, true),
//                new BinaryPdfField(notPresentPdfField, false)
//        );
//    }
}