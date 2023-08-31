package org.codeforamerica.shiba.output.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;

@Slf4j
public class PDFBoxFieldFiller implements PdfFieldFiller {

  private final List<Resource> pdfs;

  public PDFBoxFieldFiller(List<Resource> pdfs) {
    this.pdfs = pdfs;
  }

  @Override
  public ApplicationFile fill(Collection<PdfField> fields, String applicationId, String filename) {
    PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();

    byte[] fileContents = pdfs.stream()
        .map(pdfResource -> fillOutPdfs(fields, pdfResource))
        .reduce(mergePdfs(pdfMergerUtility))
        .map(this::outputByteArray)
        .orElse(new byte[]{});

    return new ApplicationFile(fileContents, filename);
  }

  private byte[] outputByteArray(PDDocument pdDocument) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      pdDocument.save(outputStream);
      pdDocument.close();
    } catch (IOException e) {
      log.error("Unable to save output", e);
    }
    return outputStream.toByteArray();
  }

  @NotNull
  private BinaryOperator<@NotNull PDDocument> mergePdfs(PDFMergerUtility pdfMergerUtility) {
    return (pdDocument1, pdDocument2) -> {
      try {
        pdfMergerUtility.appendDocument(pdDocument1, pdDocument2);
        pdDocument2.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return pdDocument1;
    };
  }

  @NotNull
  private PDDocument fillOutPdfs(Collection<PdfField> fields, Resource pdfResource) {
    try {
      PDDocument loadedDoc = Loader.loadPDF(pdfResource.getInputStream().readAllBytes());
      loadedDoc.getDocumentCatalog().setDocumentOutline(null);//Removes bookmarks
      PDAcroForm acroForm = loadedDoc.getDocumentCatalog().getAcroForm();
      acroForm.setNeedAppearances(true);
      fillAcroForm(fields, acroForm);
      return loadedDoc;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void fillAcroForm(Collection<PdfField> fields, PDAcroForm acroForm) {
    fields.forEach(field ->
        Optional.ofNullable(acroForm.getField(field.getName())).ifPresent(pdField -> {
          try {
            String fieldValue = field.getValue();
            if (pdField instanceof PDCheckBox && field.getValue().equals("No")) {
              fieldValue = "Off";
            }
            setPdfFieldWithoutUnsupportedUnicode(fieldValue, pdField);
          } catch (Exception e) {
            throw new RuntimeException("Error setting field: " + field.getName(), e);
          }
        }));
  }

  private void setPdfFieldWithoutUnsupportedUnicode(String field, PDField pdField)
      throws IOException {
    try {
      pdField.setValue(field);
    } catch (IllegalArgumentException e) {
      log.error(
          "Error setting value '%s' for field %s".formatted(field,
              pdField.getFullyQualifiedName()));
    }
  }
}
