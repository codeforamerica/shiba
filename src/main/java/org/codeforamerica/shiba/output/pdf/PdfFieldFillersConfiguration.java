package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Configuration
public class PdfFieldFillersConfiguration {
    @Bean
    public PdfFieldFiller caseworkerCafFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:caf-body.pdf") Resource cafBody,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(coverPages, cafBody), font);
    }

    @Bean
    public PdfFieldFiller clientCafFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:caf-standard-headers.pdf") Resource standardHeaders,
            @Value("classpath:caf-body.pdf") Resource cafBody,
            @Value("classpath:caf-standard-footers.pdf") Resource standardFooters,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(
                coverPages, standardHeaders, cafBody, standardFooters
        ), font);
    }

    @Bean
    public PdfFieldFiller ccapFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:ccap-body.pdf") Resource ccapBody,
            @Value("classpath:ccap-footers.pdf") Resource ccapFooters,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(
                coverPages, ccapBody, ccapFooters
        ), font);
    }

    @Bean
    public Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillers(
            PdfFieldFiller caseworkerCafFiller,
            PdfFieldFiller clientCafFiller,
            PdfFieldFiller ccapFiller
    ) {
        return Map.of(
                CASEWORKER, Map.of(
                        CAF, caseworkerCafFiller,
                        CCAP, ccapFiller),
                CLIENT, Map.of(
                        CAF, clientCafFiller,
                        CCAP, ccapFiller)
        );
    }
}
