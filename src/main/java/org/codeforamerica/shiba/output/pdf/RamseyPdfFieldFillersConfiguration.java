package org.codeforamerica.shiba.output.pdf;

import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.codeforamerica.shiba.output.Document.*;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;

@Configuration
public class RamseyPdfFieldFillersConfiguration {

  @Bean
  public ramseyPdfFieldFiller ramseyCaseworkerCafFiller(
      @Value("classpath:ramsey-cover-pages.pdf") Resource coverPages,
      @Value("classpath:caf-body.pdf") Resource cafBody
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, cafBody));
  }
  @Bean
  public ramseyPdfFieldFiller ramseyCaseworkerCafWdHouseholdSuppFiller(
      @Value("classpath:ramsey-cover-pages.pdf") Resource coverPages,
      @Value("classpath:caf-body.pdf") Resource cafBody,
      @Value("classpath:caf-household-member-supplement.pdf") Resource cafBodyWdHouseholdSupp
  ) {
    return new PDFBoxFieldFiller(List.of(
        coverPages, cafBody,cafBodyWdHouseholdSupp
    ));
  }

  @Bean
  public ramseyPdfFieldFiller ramseyClientCafFiller(
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
  public ramseyPdfFieldFiller ramseyClientCafWdHouseholdSuppFiller(
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
  public ramseyPdfFieldFiller ramseyCaseworkerCcapFiller(
      @Value("classpath:ramsey-cover-pages.pdf") Resource coverPages,
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
  public ramseyPdfFieldFiller ramseyClientCcapFiller(
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
  public ramseyPdfFieldFiller ramseyCaseworkerCertainPopsFiller(
      @Value("classpath:ramsey-cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops));
  }

  @Bean
  public ramseyPdfFieldFiller ramseyClientCertainPopsFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops));
  }

  @Bean
  public ramseyPdfFieldFiller ramseyUploadedDocCoverPageFilter(
      @Value("classpath:uploaded-document-cover-page.pdf") Resource coverPage
  ) {
    return new PDFBoxFieldFiller(List.of(coverPage));
  }

  @Bean
  public Map<Recipient, Map<Document, ramseyPdfFieldFiller>> ramseyPdfFieldFillers(
      ramseyPdfFieldFiller ramseyCaseworkerCafFiller,
      ramseyPdfFieldFiller ramseyClientCafFiller,
      ramseyPdfFieldFiller ramseyCaseworkerCcapFiller,
      ramseyPdfFieldFiller ramseyClientCcapFiller,
      ramseyPdfFieldFiller ramseyCaseworkerCertainPopsFiller,
      ramseyPdfFieldFiller ramseyClientCertainPopsFiller,
      ramseyPdfFieldFiller ramseyUploadedDocCoverPageFilter,
      ramseyPdfFieldFiller ramseyClientCafWdHouseholdSuppFiller,
      ramseyPdfFieldFiller ramseyCaseworkerCafWdHouseholdSuppFiller) {
    return Map.of(
        CASEWORKER, Map.of(
            CAF, ramseyCaseworkerCafFiller,
            CCAP, ramseyCaseworkerCcapFiller,
            UPLOADED_DOC, ramseyUploadedDocCoverPageFilter,
            CERTAIN_POPS, ramseyCaseworkerCertainPopsFiller
        ),
        CLIENT, Map.of(
            CAF, ramseyClientCafFiller,
            CCAP, ramseyClientCcapFiller,
            CERTAIN_POPS, ramseyClientCertainPopsFiller )
    );
  }

  @Bean
  public Map<Recipient, Map<Document, ramseyPdfFieldFiller>> ramseyPdfFieldWithCAFHHSuppFillers(
      ramseyPdfFieldFiller ramseyCaseworkerCafWdHouseholdSuppFiller,
      ramseyPdfFieldFiller ramseyClientCafWdHouseholdSuppFiller) {
    return Map.of(
        CASEWORKER, Map.of(
            CAF, ramseyCaseworkerCafWdHouseholdSuppFiller
        ),
        CLIENT, Map.of(
            CAF, ramseyClientCafWdHouseholdSuppFiller )
    );
  }
}
