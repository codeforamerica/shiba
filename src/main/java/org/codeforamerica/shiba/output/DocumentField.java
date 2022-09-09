package org.codeforamerica.shiba.output;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

/**
 * DocumentField maps the web page inputs to the PDF form fields.
 * String groupName refers to the groups in pdf-mapping.yaml
 * String name refers to the entries within each group, which maps to the PDF form field names.
 * value is the actual value that gets entered in the PDF field.
 */
@Value
public class DocumentField {

  String groupName;
  String name;
  @NotNull
  List<String> value;
  DocumentFieldType type;
  Integer iteration;

  public DocumentField(String groupName, String name, @NotNull List<String> value,
      DocumentFieldType type, Integer iteration) {
    this.groupName = groupName;
    this.name = name;
    this.value = value;
    this.type = type;
    this.iteration = iteration;
  }

  public DocumentField(String groupName, String name, @NotNull List<String> value,
      DocumentFieldType type) {
    this(groupName, name, value, type, null);
  }

  // Make an application input with only a single value
  public DocumentField(String groupName, String name, String value, DocumentFieldType type) {
    this(groupName, name, value, type, null);
  }

  // Make an application input for an iteration with only a single value
  public DocumentField(String groupName, String name, String value, DocumentFieldType type,
      Integer iteration) {
    this(groupName, name, value == null ? emptyList() : List.of(value), type, iteration);
  }

  public List<String> getPdfName(Map<String, List<String>> pdfFieldMap) {
    List<String> names = pdfFieldMap.get(String.join(".", this.getGroupName(), this.getName()));
    return this.getNameWithIteration(names);
  }

  public String getMultiValuePdfName(Map<String, List<String>> pdfFieldMap, String value) {
    List<String> names = pdfFieldMap
        .get(String.join(".", this.getGroupName(), this.getName(), value));
    if (getNameWithIteration(names).size() > 0) {
      return getNameWithIteration(names).get(0);
    } else {
      return null;
    }
  }

  private List<String> getNameWithIteration(List<String> names) {
    if (names == null) {
      return emptyList();
    }

    return names.stream()
        .map(name -> this.getIteration() != null ? name + "_" + this.getIteration() : name)
        .collect(Collectors.toList());
  }

  public String getValue(int i) {
    return getValue().get(i);
  }
}
