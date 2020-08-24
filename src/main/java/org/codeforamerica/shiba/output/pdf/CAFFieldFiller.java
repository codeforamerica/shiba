package org.codeforamerica.shiba.output.pdf;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.util.Collection;

@Component
public class CAFFieldFiller implements PdfFieldFiller {
    private final Resource applicationPDF;
    private final Clock clock;

    public CAFFieldFiller(@Value("classpath:DHS-5223.pdf") Resource applicationPDF,
                          Clock clock) {
        this.applicationPDF = applicationPDF;
        this.clock = clock;
    }

    @Override
    public ApplicationFile fill(Collection<PdfField> fields) {
        try {
            PDDocument document = PDDocument.load(applicationPDF.getInputStream());
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();

            fields.forEach(field -> {
                try {
                    acroForm.getField(field.getName()).setValue(field.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();

            return new ApplicationFile(
                    outputStream.toByteArray(),
                    StringUtils.join(clock.instant().getEpochSecond(), "-", applicationPDF.getFilename()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
