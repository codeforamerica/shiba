package org.codeforamerica.shiba.output.pdf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;

@Configuration
public class PdfFieldFillersConfiguration {
    @Bean
    public PdfFieldFiller cafPdfFieldFiller(
            @Value("classpath:DHS-5223.pdf") Resource applicationPDF
    ) {
        return new PDFBoxFieldFiller(applicationPDF, List.of());
    }
}
