package org.codeforamerica.shiba.output.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;

public class PDFBoxFieldFiller implements PdfFieldFiller {

  private final List<Resource> pdfs;
  private final Resource fontResource;

  public PDFBoxFieldFiller(List<Resource> pdfs, Resource fontResource) {
    this.pdfs = pdfs;
    this.fontResource = fontResource;
  }

  @Override
  public ApplicationFile fill(Collection<PdfField> fields, String applicationId, String fileName) {
    PDFMergerUtility mergerer = new PDFMergerUtility();

    byte[] fileContents = pdfs.stream()
        .map(pdfResource -> fillOutPdfs(fields, pdfResource))
        .reduce(mergePdfs(mergerer))
        .map(this::outputByteArray)
        .orElse(new byte[]{});

    return new ApplicationFile(fileContents, fileName);
  }

  private byte[] outputByteArray(PDDocument pdDocument) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      pdDocument.save(outputStream);
      pdDocument.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return outputStream.toByteArray();
  }

  @NotNull
  private BinaryOperator<@NotNull PDDocument> mergePdfs(PDFMergerUtility mergerer) {
    return (pdDocument1, pdDocument2) -> {
      try {
        mergerer.appendDocument(pdDocument1, pdDocument2);
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
      PDDocument loadedDoc = PDDocument.load(pdfResource.getInputStream());
      PDAcroForm acroForm = loadedDoc.getDocumentCatalog().getAcroForm();
      PDFont font = PDType0Font.load(loadedDoc, fontResource.getInputStream(), false);
      PDResources res = acroForm.getDefaultResources();
      String fontName = res.add(font).getName();

      fillAcroForm(fields, acroForm, fontName);
      return loadedDoc;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private void fillAcroForm(Collection<PdfField> fields, PDAcroForm acroForm, String fontName) {
    fields.forEach(field ->
        Optional.ofNullable(acroForm.getField(field.getName())).ifPresent(pdField -> {
          try {
            if (pdField instanceof PDVariableText) {
              ((PDVariableText) pdField).setDefaultAppearance("/" + fontName + " 10 Tf 0 g");
            }
            String fieldValue = field.getValue();
            if (pdField instanceof PDCheckBox && field.getValue().equals("No")) {
              fieldValue = "Off";
            }

            PDFont font = acroForm.getDefaultResources().getFont(COSName.getPDFName(fontName));
            setPdfFieldWithoutUnsupportedUnicode(fieldValue, pdField, font);
          } catch (Exception e) {
            throw new RuntimeException("Error setting field: " + field.getName(), e);
          }
        }));
  }

  private void setPdfFieldWithoutUnsupportedUnicode(String field, PDField pdField, PDFont font)
      throws IOException {
    try {
      pdField.setValue(field);
    } catch (IllegalArgumentException e) {
      // Might be an unsupported unicode
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < field.length(); i++) {
        int codepoint = field.codePointAt(i);
        if (font == null || font.toUnicode(codepoint) != null) {
          builder.append(field.charAt(i));
        }
      }
      pdField.setValue(builder.toString());
    }
  }
}
