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

public class PDFBoxFieldFiller implements PdfFieldFiller {
    private final List<Resource> additionalPDFs;
    private final Resource destination;

    public PDFBoxFieldFiller(Resource destination, List<Resource> additionalPDFs) {
        this.additionalPDFs = additionalPDFs;
        this.destination = destination;
    }

    @Override
    public ApplicationFile fill(Collection<PdfField> fields, String applicationId) {
        try {
            PDFMergerUtility mergerer = new PDFMergerUtility();
            PDDocument document = PDDocument.load(destination.getInputStream());
            additionalPDFs.forEach(pdf -> {
                try {
                    mergerer.appendDocument(
                            document,
                            PDDocument.load(pdf.getInputStream())
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

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
                    String.format("cfa-%s-CAF.pdf", applicationId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
