package org.codeforamerica.shiba.output.pdf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.Collections;
import java.util.List;

@Configuration
public class PdfFieldFillersConfiguration {
    @Bean
    public PdfFieldFiller cafFieldFiller(@Value("classpath:DHS-5223.pdf") Resource applicationPDF) {
        return new PDFBoxFieldFiller(applicationPDF, Collections.emptyList());
    }

    @Bean
    public PdfFieldFiller cafWithCoverPageFieldFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:DHS-5223.pdf") Resource applicationPDF
    ) {
        return new PDFBoxFieldFiller(coverPages, List.of(applicationPDF));
    }
}
