package org.codeforamerica.shiba.testutilities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.configurations.SecurityConfiguration.ADMIN_EMAILS;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.jetbrains.annotations.NotNull;

public class TestUtils {

  public static final String ADMIN_EMAIL = ADMIN_EMAILS.get(0);

  public static Path getAbsoluteFilepath(String resourceFilename) {
    return Paths.get(getAbsoluteFilepathString(resourceFilename));
  }

  public static String getAbsoluteFilepathString(String resourceFilename) {
    URL resource = TestUtils.class.getClassLoader().getResource(resourceFilename);
    if (resource != null) {
      return (new File(resource.getFile())).getAbsolutePath();
    }
    return "";
  }

  public static byte[] getFileContentsAsByteArray(String filename) throws IOException {
    return Files.readAllBytes(getAbsoluteFilepath(filename));
  }

  public static void assertPdfFieldEquals(String fieldName, String expectedVal, PDAcroForm pdf) {
    PDField field = pdf.getField(fieldName);
    assertThat(field).isNotNull();
    String pdfFieldText = field.getValueAsString();
    assertThat(pdfFieldText).isEqualTo(expectedVal);
  }

  public static void assertPdfFieldContains(String fieldName, String expectedVal, PDAcroForm pdf) {
    PDField field = pdf.getField(fieldName);
    assertThat(field).isNotNull();
    String pdfFieldText = field.getValueAsString();
    assertThat(pdfFieldText).contains(expectedVal);
  }

  public static void assertPdfFieldIsEmpty(String fieldName, PDAcroForm pdf) {
    var pdfFieldText = pdf.getField(fieldName).getValueAsString();
    assertThat(pdfFieldText).isEmpty();
  }

  public static void assertPdfFieldIsNull(String fieldName, PDAcroForm pdf) {
    PDField pdfField = pdf.getField(fieldName);
    assertThat(pdfField).isNull();
  }

  public static void resetApplicationData(ApplicationData applicationData) {
    applicationData.setId(null);
    applicationData.setUtmSource(null);
    applicationData.setPagesData(new PagesData());
    applicationData.setSubworkflows(new Subworkflows());
    applicationData.setIncompleteIterations(new HashMap<>());
    applicationData.setUploadedDocs(new ArrayList<>());
    applicationData.setFlow(FlowType.UNDETERMINED);
    applicationData.setSubmitted(false);
  }

  @NotNull
  public static DocumentField createApplicationInput(String groupName, String name,
      String value) {
    return new DocumentField(groupName, name, List.of(value), ENUMERATED_SINGLE_VALUE);
  }
  
  @NotNull
  public static DocumentField createApplicationInputSingleValue(String groupName, String name,
      String value) {
    return new DocumentField(groupName, name, List.of(value), SINGLE_VALUE);
  }
}
