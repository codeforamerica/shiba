package org.codeforamerica.shiba;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

@Component
public class CAFFieldFiller implements PDFFieldFiller {
    private final Resource applicationPDF;

    public CAFFieldFiller() {
        this.applicationPDF = new ClassPathResource("DHS-5223.pdf");
    }

    @Override
    public PdfFile fill(Collection<PDFField> fields) {
        try {
            PDDocument document = PDDocument.load(applicationPDF.getInputStream());
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            fields.stream()
                    .flatMap(field -> field.getInputBindings().entrySet().stream())
                    .forEach(entry -> {
                        try {
                            acroForm.getField(entry.getKey()).setValue(entry.getValue());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();
            return new PdfFile(outputStream.toByteArray(), applicationPDF.getFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
