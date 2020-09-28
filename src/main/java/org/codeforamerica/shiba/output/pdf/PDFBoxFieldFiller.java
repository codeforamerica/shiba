package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;

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

        return pdfs.stream()
                .map(pdfResource -> fillOutPdfs(fields, pdfResource))
                .reduce(mergePdfs(mergerer))
                .map(this::outputByteArray)
                .map(bytes -> new ApplicationFile(
                        bytes,
                        String.format("%s.pdf", fileName)))
                .orElse(new ApplicationFile(
                        new byte[]{},
                        String.format("%s.pdf", fileName)));
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
            } catch (IOException e) {
                e.printStackTrace();
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
                        pdField.setValue(field.getValue());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }
}
