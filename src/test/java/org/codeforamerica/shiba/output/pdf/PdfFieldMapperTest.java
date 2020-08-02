package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PdfFieldMapperTest {
    @ParameterizedTest
    @EnumSource(names = {"SINGLE_VALUE", "ENUMERATED_SINGLE_VALUE"}, value = ApplicationInputType.class)
    void shouldMapSingleValueInputsToSimpleFields(ApplicationInputType applicationInputType) {
        String fieldName = "someName";
        String formInputName = "some-input";
        String pageName = "some-screen";
        Map<String, String> configMap = Map.of(pageName + "." + formInputName, fieldName);

        String stringValue = "some-string-value";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(stringValue), applicationInputType);

        PdfFieldMapper subject = new PdfFieldMapper(configMap);
        List<PdfField> fields = subject.map(List.of(applicationInput));

        assertThat(fields).contains(new SimplePdfField(fieldName, stringValue));
    }

    @Test
    void shouldMapDateValuesToSimpleFields() {
        String fieldName = "someName";
        String formInputName = "some-input";
        String pageName = "some-screen";
        Map<String, String> configMap = Map.of(pageName + "." + formInputName, fieldName);

        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of("01", "20", "3312"), ApplicationInputType.DATE_VALUE);

        PdfFieldMapper subject = new PdfFieldMapper(configMap);
        List<PdfField> fields = subject.map(List.of(applicationInput));

        assertThat(fields).contains(new SimplePdfField(fieldName, "01/20/3312"));
    }

    @Test
    void shouldNotMapInputsWithoutPdfFieldMappings() {
        String formInputName = "some-input";
        String pageName = "some-screen";

        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of("someValue"), ApplicationInputType.SINGLE_VALUE);

        PdfFieldMapper subject = new PdfFieldMapper(Map.of());
        List<PdfField> fields = subject.map(List.of(applicationInput));

        assertThat(fields).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ApplicationInputType.class)
    void shouldNotMapInputsWithEmptyValues(ApplicationInputType applicationInputType) {
        String fieldName = "someName";
        String formInputName = "some-input";
        String pageName = "some-screen";
        Map<String, String> configMap = Map.of(pageName + "." + formInputName, fieldName);

        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(), applicationInputType);

        PdfFieldMapper subject = new PdfFieldMapper(configMap);
        List<PdfField> fields = subject.map(List.of(applicationInput));

        assertThat(fields).isEmpty();
    }

    @Test
    void shouldMapMultiValueInputsToBinaryFields() {
        String fieldName1 = "someName1";
        String fieldName2 = "someName2";
        String formInputName = "some-input";
        String pageName = "some-screen";
        String value1 = "some-value";
        String value2 = "some-other-value";
        ApplicationInput applicationInput = new ApplicationInput(pageName, formInputName, List.of(value1, value2), ApplicationInputType.ENUMERATED_MULTI_VALUE);
        Map<String, String> configMap = Map.of(
                pageName + "." + formInputName + "." + value1, fieldName1,
                pageName + "." + formInputName + "." + value2, fieldName2
        );

        PdfFieldMapper subject = new PdfFieldMapper(configMap);
        List<PdfField> fields = subject.map(List.of(applicationInput));

        assertThat(fields).contains(
                new BinaryPdfField(fieldName1),
                new BinaryPdfField(fieldName2));
    }
}