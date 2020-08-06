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

    @Test
    void shouldAddIterationToFieldNameForInputsWithIterations() {
        String fieldName1 = "someName1";
        String fieldName2 = "someName2";
        String fieldName3 = "someName3";
        String fieldName4 = "someName4";
        String formInputName1 = "some-input1";
        String formInputName2 = "some-input2";
        String formInputName3 = "some-input3";
        String pageName = "some-screen";
        String value1 = "some-value";
        String value2 = "some-other-value";
        List<String> dateValue = List.of("01", "20", "3312");

        ApplicationInput applicationInput1 = new ApplicationInput(
                pageName, formInputName1, List.of(value1, value2), ApplicationInputType.ENUMERATED_MULTI_VALUE, 0
        );
        ApplicationInput applicationInput2 = new ApplicationInput(
                pageName, formInputName2, List.of(value1), ApplicationInputType.SINGLE_VALUE, 1
        );

        ApplicationInput applicationInput3 = new ApplicationInput(
                pageName, formInputName3, dateValue, ApplicationInputType.DATE_VALUE, 2
        );

        Map<String, String> configMap = Map.of(
                pageName + "." + formInputName1 + "." + value1, fieldName1,
                pageName + "." + formInputName1 + "." + value2, fieldName2,
                pageName + "." + formInputName2, fieldName3,
                pageName + "." + formInputName3, fieldName4
        );


        PdfFieldMapper subject = new PdfFieldMapper(configMap);
        List<PdfField> fields = subject.map(List.of(applicationInput1, applicationInput2, applicationInput3));

        assertThat(fields).contains(
                new BinaryPdfField(fieldName1 + "_0"),
                new BinaryPdfField(fieldName2 + "_0"),
                new SimplePdfField(fieldName3 + "_1", value1),
                new SimplePdfField(fieldName4 + "_2", "01/20/3312")
        );
    }
}