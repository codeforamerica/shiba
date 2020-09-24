package org.codeforamerica.shiba.output.pdf;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
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
    public static final BinaryOperator<PDDocument> NOT_USED_FOR_SEQUENTIAL_STREAM = (a, b) -> new PDDocument();

    public PDFBoxFieldFiller(List<Resource> pdfs) {
        this.pdfs = pdfs;
    }

    @Override
    public ApplicationFile fill(Collection<PdfField> fields, String applicationId, String fileName) {
        try {
            PDFMergerUtility mergerer = new PDFMergerUtility();

            PDDocument document = pdfs.stream().reduce(
                    new PDDocument(),
                    (pdDocument, resource) -> {
                        try {
                            mergerer.appendDocument(
                                    pdDocument,
                                    PDDocument.load(resource.getInputStream())
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        return pdDocument;
                    },
                    NOT_USED_FOR_SEQUENTIAL_STREAM
            );

            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            fields.forEach(field ->
                    Optional.ofNullable(acroForm.getField(field.getName())).ifPresent(pdField -> {
                        try {
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
