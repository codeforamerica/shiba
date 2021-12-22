package org.codeforamerica.shiba.output.pdf;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Document.CERTAIN_POPS;
import static org.codeforamerica.shiba.output.Document.UPLOADED_DOC;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class PdfFieldFillersConfiguration {

  @Bean
  public PdfFieldFiller caseworkerCafFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:caf-body.pdf") Resource cafBody
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, cafBody));
  }

  @Bean
  public PdfFieldFiller clientCafFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:caf-standard-headers.pdf") Resource standardHeaders,
      @Value("classpath:caf-body.pdf") Resource cafBody,
      @Value("classpath:caf-standard-footers.pdf") Resource standardFooters
  ) {
    return new PDFBoxFieldFiller(List.of(
        coverPages, standardHeaders, cafBody, standardFooters
    ));
  }

  @Bean
  public PdfFieldFiller caseworkerCcapFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:ccap-body-caseworker-page1.pdf") Resource ccapPage1,
      @Value("classpath:ccap-body.pdf") Resource ccapBody,
      @Value("classpath:ccap-body-perjury-and-general-declarations.pdf") Resource ccapDeclarations,
      @Value("classpath:ccap-body-additional-room.pdf") Resource ccapAdditionalRoom
  ) {
    return new PDFBoxFieldFiller(List.of(
        coverPages, ccapPage1, ccapBody, ccapDeclarations, ccapAdditionalRoom
    ));
  }

  @Bean
  public PdfFieldFiller clientCcapFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:ccap-headers.pdf") Resource ccapHeaders,
      @Value("classpath:ccap-body-client-page1.pdf") Resource ccapPage1,
      @Value("classpath:ccap-body.pdf") Resource ccapBody,
      @Value("classpath:ccap-body-authorize-info-sharing.pdf") Resource ccapInfoSharing,
      @Value("classpath:ccap-body-perjury-and-general-declarations.pdf") Resource ccapDeclarations,
      @Value("classpath:ccap-body-additional-room.pdf") Resource ccapAdditionalRoom,
      @Value("classpath:ccap-footers.pdf") Resource ccapFooters
  ) {
    return new PDFBoxFieldFiller(List.of(
        coverPages, ccapHeaders, ccapPage1, ccapBody, ccapInfoSharing, ccapDeclarations,
        ccapAdditionalRoom, ccapFooters
    ));
  }

  @Bean
  public PdfFieldFiller caseworkerCertainPopsFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops));
  }

  @Bean
  public PdfFieldFiller clientCertainPopsFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops));
  }

  @Bean
  public PdfFieldFiller uploadedDocCoverPageFilter(
      @Value("classpath:uploaded-document-cover-page.pdf") Resource coverPage
  ) {
    return new PDFBoxFieldFiller(List.of(coverPage));
  }

  @Bean
  public Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldFillers(
      PdfFieldFiller caseworkerCafFiller,
      PdfFieldFiller clientCafFiller,
      PdfFieldFiller caseworkerCcapFiller,
      PdfFieldFiller clientCcapFiller,
      PdfFieldFiller caseworkerCertainPopsFiller,
      PdfFieldFiller clientCertainPopsFiller,
      PdfFieldFiller uploadedDocCoverPageFilter) {
    return Map.of(
        CASEWORKER, Map.of(
            CAF, caseworkerCafFiller,
            CCAP, caseworkerCcapFiller,
            UPLOADED_DOC, uploadedDocCoverPageFilter,
            CERTAIN_POPS, caseworkerCertainPopsFiller
        ),
        CLIENT, Map.of(
            CAF, clientCafFiller,
            CCAP, clientCcapFiller,
            CERTAIN_POPS, clientCertainPopsFiller)
    );
  }
}
