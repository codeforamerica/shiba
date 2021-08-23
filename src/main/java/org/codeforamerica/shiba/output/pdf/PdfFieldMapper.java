package org.codeforamerica.shiba.output.pdf;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.jetbrains.annotations.NotNull;

public class PdfFieldMapper {

  private final Map<String, List<String>> pdfFieldMap;
  private final Map<String, String> enumMap;

  public PdfFieldMapper(Map<String, List<String>> pdfFieldMap, Map<String, String> enumMap) {
    this.pdfFieldMap = pdfFieldMap;
    this.enumMap = enumMap;
  }

  public List<PdfField> map(List<ApplicationInput> applicationInputs) {
    return applicationInputs.stream()
        .filter(input -> !input.getValue().isEmpty())
        .flatMap(this::makePdfFieldsForInput)
        .filter(pdfField -> pdfField.getName() != null)
        .collect(Collectors.toList());
  }

  @NotNull
  private Stream<? extends PdfField> makePdfFieldsForInput(ApplicationInput input) {
    return switch (input.getType()) {
      case DATE_VALUE -> simplePdfFields(input, this::getDateFormattedValue);
      case ENUMERATED_MULTI_VALUE -> binaryPdfFields(input);
      case UNUSED -> Stream.of();
      default -> simplePdfFields(input, this::getOrDefaultInputValue);
    };
  }

  @NotNull
  private Stream<SimplePdfField> simplePdfFields(ApplicationInput input,
      Function<ApplicationInput, String> valueMapper) {
    return input.getPdfName(pdfFieldMap).stream()
        .map(pdfName -> new SimplePdfField(pdfName, valueMapper.apply(input)));
  }

  @NotNull
  private Stream<BinaryPdfField> binaryPdfFields(ApplicationInput input) {
    return input.getValue().stream()
        .map(value -> new BinaryPdfField(input.getMultiValuePdfName(pdfFieldMap, value)));
  }

  @NotNull
  private String getDateFormattedValue(ApplicationInput input) {
    return String.join("/", input.getValue());
  }

  @NotNull
  private String getOrDefaultInputValue(ApplicationInput input) {
    return enumMap.getOrDefault(input.getValue(0), input.getValue(0));
  }
}
