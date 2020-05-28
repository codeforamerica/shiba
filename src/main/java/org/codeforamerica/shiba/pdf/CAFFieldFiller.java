package org.codeforamerica.shiba.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.ApplicationFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

@Component
public class CAFFieldFiller implements PdfFieldFiller {
    private final Resource applicationPDF;

    public CAFFieldFiller(@Value("classpath:DHS-5223.pdf") Resource applicationPDF) {
        this.applicationPDF = applicationPDF;
    }

    @Override
    public ApplicationFile fill(Collection<PdfField> fields) {
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
            return new ApplicationFile(outputStream.toByteArray(), applicationPDF.getFilename());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
