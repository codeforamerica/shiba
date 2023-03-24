package org.codeforamerica.shiba.output.pdf;

import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
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
  public PdfFieldFiller caseworkerCafWdHouseholdSuppFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:caf-body.pdf") Resource cafBody,
      @Value("classpath:caf-household-member-supplement.pdf") Resource cafBodyWdHouseholdSupp
  ) {
    return new PDFBoxFieldFiller(List.of(
        coverPages, cafBody,cafBodyWdHouseholdSupp
    ));
  }
  
  @Bean
  public PdfFieldFiller caseworkerCafWdHouseholdSuppFiller2(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:caf-body.pdf") Resource cafBody,
      @Value("classpath:caf-household-member-supplement.pdf") Resource cafBodyWdHouseholdSupp,
      @Value("classpath:caf-household-member-supplement1.pdf") Resource cafBodyWdHouseholdSupp1
  ) {
    return new PDFBoxFieldFiller(List.of(
        coverPages, cafBody,cafBodyWdHouseholdSupp, cafBodyWdHouseholdSupp1
    ));
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
  public PdfFieldFiller clientCafWdHouseholdSuppFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:caf-standard-headers.pdf") Resource standardHeaders,
      @Value("classpath:caf-body.pdf") Resource cafBody,
      @Value("classpath:caf-household-member-supplement.pdf") Resource cafBodyWdHouseholdSupp,
      @Value("classpath:caf-standard-footers.pdf") Resource standardFooters
  ) {
    return new PDFBoxFieldFiller(List.of(
        coverPages, standardHeaders, cafBody, standardFooters, cafBodyWdHouseholdSupp
    ));
  }
  
  @Bean
  public PdfFieldFiller clientCafWdHouseholdSuppFiller2(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:caf-standard-headers.pdf") Resource standardHeaders,
      @Value("classpath:caf-body.pdf") Resource cafBody,
      @Value("classpath:caf-household-member-supplement.pdf") Resource cafBodyWdHouseholdSupp,
      @Value("classpath:caf-household-member-supplement1.pdf") Resource cafBodyWdHouseholdSupp2,
      @Value("classpath:caf-standard-footers.pdf") Resource standardFooters
  ) {
    return new PDFBoxFieldFiller(List.of(
        coverPages, standardHeaders, cafBody, standardFooters, cafBodyWdHouseholdSupp, cafBodyWdHouseholdSupp2
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
      PdfFieldFiller uploadedDocCoverPageFilter) {
    return Map.of(
        CASEWORKER, Map.of(
            CAF, caseworkerCafFiller,
            CCAP, caseworkerCcapFiller,
            UPLOADED_DOC, uploadedDocCoverPageFilter
        ),
        CLIENT, Map.of(
            CAF, clientCafFiller,
            CCAP, clientCcapFiller)
    );
  }
  
  @Bean
  public Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldWithCAFHHSuppFillers(
      PdfFieldFiller caseworkerCafWdHouseholdSuppFiller,
      PdfFieldFiller clientCafWdHouseholdSuppFiller) {
    return Map.of(
        CASEWORKER, Map.of(
            CAF, caseworkerCafWdHouseholdSuppFiller
        ),
        CLIENT, Map.of(
            CAF, clientCafWdHouseholdSuppFiller )
    );
  }
  
  @Bean
  public Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldWithCAFHHSuppFillers2(
      PdfFieldFiller caseworkerCafWdHouseholdSuppFiller2,
      PdfFieldFiller clientCafWdHouseholdSuppFiller2) {
    return Map.of(
        CASEWORKER, Map.of(
            CAF, caseworkerCafWdHouseholdSuppFiller2
        ),
        CLIENT, Map.of(
            CAF, clientCafWdHouseholdSuppFiller2 )
    );
  } 
  
}
