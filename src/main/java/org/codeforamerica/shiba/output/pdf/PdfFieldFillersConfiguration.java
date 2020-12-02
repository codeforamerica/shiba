package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.Recipient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.codeforamerica.shiba.output.Recipient.*;

@Configuration
public class PdfFieldFillersConfiguration {
    @Bean
    public PdfFieldFiller caseWorkerFiller(
            @Value("classpath:cover-pages.pdf") Resource coverPages,
            @Value("classpath:caf-body.pdf") Resource cafBody,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(coverPages, cafBody), font);
    }

    @Bean
    public PdfFieldFiller clientFiller(
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
            @Value("classpath:ccap-cover.pdf") Resource ccapCover,
            @Value("classpath:LiberationSans-Regular.ttf") Resource font
    ) {
        return new PDFBoxFieldFiller(List.of(
                coverPages, ccapCover
        ), font);
    }

    @Bean
    public Map<Recipient, PdfFieldFiller> pdfFieldFillers(
            PdfFieldFiller caseWorkerFiller,
            PdfFieldFiller clientFiller,
            PdfFieldFiller ccapFiller
    ) {
        return Map.of(
                CASEWORKER, caseWorkerFiller,
                CLIENT, clientFiller,
                CCAP, ccapFiller
        );
    }
}
