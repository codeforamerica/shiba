package org.codeforamerica.shiba.output.pdf;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PdfFieldMapperTest {

  @ParameterizedTest
  @EnumSource(names = {"SINGLE_VALUE",
      "ENUMERATED_SINGLE_VALUE"}, value = DocumentFieldType.class)
  void shouldMapSingleValueInputsToSimpleFields(DocumentFieldType documentFieldType) {
    String fieldName = "someName";
    String formInputName = "some-input";
    String pageName = "some-screen";
    Map<String, List<String>> configMap = Map
        .of(pageName + "." + formInputName, List.of(fieldName));

    String stringValue = "some-string-value";
    DocumentField documentField = new DocumentField(pageName, formInputName,
        List.of(stringValue), documentFieldType);

    PdfFieldMapper subject = new PdfFieldMapper(configMap, emptyMap());
    List<PdfField> fields = subject.map(List.of(documentField));

    assertThat(fields).contains(new SimplePdfField(fieldName, stringValue));
  }

  @Test
  void shouldMapDateValuesToSimpleFields() {
    String fieldName = "someName";
    String formInputName = "some-input";
    String pageName = "some-screen";
    Map<String, List<String>> configMap = Map
        .of(pageName + "." + formInputName, List.of(fieldName));

    DocumentField documentField = new DocumentField(pageName, formInputName,
        List.of("01", "20", "3312"), DocumentFieldType.DATE_VALUE);

    PdfFieldMapper subject = new PdfFieldMapper(configMap, emptyMap());
    List<PdfField> fields = subject.map(List.of(documentField));

    assertThat(fields).contains(new SimplePdfField(fieldName, "01/20/3312"));
  }

  @Test
  void shouldMapBlankDateValueToBlankSimpleField( ) {
	  String fieldName = "someName";
	  String formInputName = "some-input";
	  String pageName = "some-screen";
	  
	  Map<String, List<String>> configMap = Map.of(pageName + "." + formInputName, List.of(fieldName));

	  DocumentField documentField = new DocumentField(pageName, formInputName,
	  List.of("", "", ""), DocumentFieldType.DATE_VALUE);

	  PdfFieldMapper subject = new PdfFieldMapper(configMap, emptyMap());
	  List<PdfField> fields = subject.map(List.of(documentField));

	  assertThat(fields).contains(new SimplePdfField(fieldName, ""));
  }
  
  @Test
  void shouldNotMapInputsWithoutPdfFieldMappings() {
    String formInputName = "some-input";
    String pageName = "some-screen";

    DocumentField documentField = new DocumentField(pageName, formInputName,
        List.of("someValue"), DocumentFieldType.SINGLE_VALUE);

    PdfFieldMapper subject = new PdfFieldMapper(emptyMap(), emptyMap());
    List<PdfField> fields = subject.map(List.of(documentField));

    assertThat(fields).isEmpty();
  }

  @Test
  void shouldNotMapInputsWithIterationNumberWithoutPdfFieldMappings() {
    String formInputName = "some-input";
    String pageName = "some-screen";

    DocumentField documentField = new DocumentField(pageName, formInputName,
        List.of("someValue"), DocumentFieldType.SINGLE_VALUE, 1);

    PdfFieldMapper subject = new PdfFieldMapper(emptyMap(), emptyMap());
    List<PdfField> fields = subject.map(List.of(documentField));

    assertThat(fields).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = DocumentFieldType.class)
  void shouldNotMapInputsWithEmptyValues(DocumentFieldType documentFieldType) {
    String fieldName = "someName";
    String formInputName = "some-input";
    String pageName = "some-screen";
    Map<String, List<String>> configMap = Map
        .of(pageName + "." + formInputName, List.of(fieldName));

    DocumentField documentField = new DocumentField(pageName, formInputName, List.of(),
        documentFieldType);

    PdfFieldMapper subject = new PdfFieldMapper(configMap, emptyMap());
    List<PdfField> fields = subject.map(List.of(documentField));

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
    DocumentField documentField = new DocumentField(pageName, formInputName,
        List.of(value1, value2), DocumentFieldType.ENUMERATED_MULTI_VALUE);
    Map<String, List<String>> configMap = Map.of(
        pageName + "." + formInputName + "." + value1, List.of(fieldName1),
        pageName + "." + formInputName + "." + value2, List.of(fieldName2)
    );

    PdfFieldMapper subject = new PdfFieldMapper(configMap, emptyMap());
    List<PdfField> fields = subject.map(List.of(documentField));

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

    DocumentField documentField1 = new DocumentField(
        pageName, formInputName1, List.of(value1, value2),
        DocumentFieldType.ENUMERATED_MULTI_VALUE, 0
    );
    DocumentField documentField2 = new DocumentField(
        pageName, formInputName2, List.of(value1), DocumentFieldType.SINGLE_VALUE, 1
    );

    DocumentField documentField3 = new DocumentField(
        pageName, formInputName3, dateValue, DocumentFieldType.DATE_VALUE, 2
    );

    Map<String, List<String>> configMap = Map.of(
        pageName + "." + formInputName1 + "." + value1, List.of(fieldName1),
        pageName + "." + formInputName1 + "." + value2, List.of(fieldName2),
        pageName + "." + formInputName2, List.of(fieldName3),
        pageName + "." + formInputName3, List.of(fieldName4)
    );

    PdfFieldMapper subject = new PdfFieldMapper(configMap, emptyMap());
    List<PdfField> fields = subject
        .map(List.of(documentField1, documentField2, documentField3));

    assertThat(fields).contains(
        new BinaryPdfField(fieldName1 + "_0"),
        new BinaryPdfField(fieldName2 + "_0"),
        new SimplePdfField(fieldName3 + "_1", value1),
        new SimplePdfField(fieldName4 + "_2", "01/20/3312")
    );
  }

  @Test
  void shouldMapToEnumValue() {
    String fieldName = "someName";
    String formInputName = "some-input";
    String pageName = "some-screen";
    Map<String, List<String>> configMap = Map
        .of(pageName + "." + formInputName, List.of(fieldName));

    String stringValue = "some-string-value";
    DocumentField documentField = new DocumentField(pageName, formInputName,
        List.of(stringValue), DocumentFieldType.SINGLE_VALUE);

    HashMap<String, String> outputMapping = new HashMap<>();
    String resultValue = "some string value";
    outputMapping.put(stringValue, resultValue);
    PdfFieldMapper subject = new PdfFieldMapper(configMap, outputMapping);

    List<PdfField> fields = subject.map(List.of(documentField));

    assertThat(fields).contains(new SimplePdfField(fieldName, resultValue));
  }
}
