package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDVariableText;
import org.codeforamerica.shiba.output.ApplicationFile;
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
    public static final BinaryOperator<PDDocument> NOT_USED_FOR_SEQUENTIAL_STREAM = (a, b) -> new PDDocument();

    public PDFBoxFieldFiller(List<Resource> pdfs, Resource fontResource) {
        this.pdfs = pdfs;
        this.fontResource = fontResource;
    }

    @Override
    public ApplicationFile fill(Collection<PdfField> fields, String applicationId, String fileName) {
        try {
            PDFMergerUtility mergerer = new PDFMergerUtility();

            PDDocument document = pdfs.stream().reduce(
                    new PDDocument(),
                    (pdDocument, resource) -> {
                        try (PDDocument loadedDoc = PDDocument.load(resource.getInputStream())){
                            mergerer.appendDocument(pdDocument, loadedDoc);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return pdDocument;
                    },
                    NOT_USED_FOR_SEQUENTIAL_STREAM
            );

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            PDFont font = PDType0Font.load(document, fontResource.getInputStream(), false);
            PDResources res = acroForm.getDefaultResources();
            String fontName = res.add(font).getName();

            fields.forEach(field ->
                    Optional.ofNullable(acroForm.getField(field.getName())).ifPresent(pdField -> {
                        try {
                            if(pdField instanceof PDVariableText) {
                                ((PDVariableText) pdField).setDefaultAppearance("/" + fontName + " 10 Tf 0 g");
                            }
                            pdField.setValue(field.getValue());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();

            return new ApplicationFile(
                    outputStream.toByteArray(),
                    String.format("%s.pdf", fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
