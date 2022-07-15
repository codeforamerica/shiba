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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
  public PdfFieldFiller caseworkerCertainPopsWdAddtlIncomeFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-income.pdf") Resource certainPopsAddIncome
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops,certainPopsAddIncome));
  }

  @Bean
  public PdfFieldFiller clientCertainPopsFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops));
  }
  
  @Bean
  public PdfFieldFiller clientCertainPopsWdAddtlIncomeFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-income.pdf") Resource certainPopsAddIncome
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops,certainPopsAddIncome));
  }
  
  //TODO emj new 
  @Bean
  public PdfFieldFiller clientCertainPopsWithAdditionHHFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers));
  }
  
  @Bean
  public PdfFieldFiller clientCertainPopsWithAdditionHHFiller1(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers, certainPopsAddHHMembers1));
  }
  @Bean
  public PdfFieldFiller clientCertainPopsWithAdditionHHFiller2(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers,
        certainPopsAddHHMembers1, certainPopsAddHHMembers2));
  }
  @Bean
  public PdfFieldFiller clientCertainPopsWithAdditionHHFiller3(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers,
        certainPopsAddHHMembers1, certainPopsAddHHMembers2, certainPopsAddHHMembers3));
  }
  @Bean
  public PdfFieldFiller clientCertainPopsWithAdditionHHFiller4(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4
      
  ) {
    return new PDFBoxFieldFiller(
        List.of(coverPages, certainPops, certainPopsAddHHMembers, certainPopsAddHHMembers1,
            certainPopsAddHHMembers2, certainPopsAddHHMembers3, certainPopsAddHHMembers4));
  }
  @Bean
  public PdfFieldFiller clientCertainPopsWithAdditionHHFiller5(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4,
      @Value("classpath:certain-pops-additional-household-members5.pdf") Resource certainPopsAddHHMembers5
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers,
        certainPopsAddHHMembers1, certainPopsAddHHMembers2, certainPopsAddHHMembers3,
        certainPopsAddHHMembers4, certainPopsAddHHMembers5));
  }
  @Bean
  public PdfFieldFiller clientCertainPopsWithAdditionHHFiller6(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4,
      @Value("classpath:certain-pops-additional-household-members5.pdf") Resource certainPopsAddHHMembers5,
      @Value("classpath:certain-pops-additional-household-members6.pdf") Resource certainPopsAddHHMembers6
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers,
        certainPopsAddHHMembers1, certainPopsAddHHMembers2, certainPopsAddHHMembers3,
        certainPopsAddHHMembers4, certainPopsAddHHMembers5, certainPopsAddHHMembers6));
  }
  
  @Bean
  public PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers));
  }
  
  @Bean
  public PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller1(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers, certainPopsAddHHMembers1));
  }
  @Bean
  public PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller2(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers,
        certainPopsAddHHMembers1, certainPopsAddHHMembers2));
  }
  @Bean
  public PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller3(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers,
        certainPopsAddHHMembers1, certainPopsAddHHMembers2, certainPopsAddHHMembers3));
  }
  @Bean
  public PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller4(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4
      
  ) {
    return new PDFBoxFieldFiller(
        List.of(coverPages, certainPops, certainPopsAddHHMembers, certainPopsAddHHMembers1,
            certainPopsAddHHMembers2, certainPopsAddHHMembers3, certainPopsAddHHMembers4));
  }
  @Bean
  public PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller5(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4,
      @Value("classpath:certain-pops-additional-household-members5.pdf") Resource certainPopsAddHHMembers5
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers,
        certainPopsAddHHMembers1, certainPopsAddHHMembers2, certainPopsAddHHMembers3,
        certainPopsAddHHMembers4, certainPopsAddHHMembers5));
  }
  @Bean
  public PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller6(
      @Value("classpath:cover-pages.pdf") Resource coverPages,
      @Value("classpath:certain-pops.pdf") Resource certainPops,
      @Value("classpath:certain-pops-additional-household-members.pdf") Resource certainPopsAddHHMembers,
      @Value("classpath:certain-pops-additional-household-members1.pdf") Resource certainPopsAddHHMembers1,
      @Value("classpath:certain-pops-additional-household-members2.pdf") Resource certainPopsAddHHMembers2,
      @Value("classpath:certain-pops-additional-household-members3.pdf") Resource certainPopsAddHHMembers3,
      @Value("classpath:certain-pops-additional-household-members4.pdf") Resource certainPopsAddHHMembers4,
      @Value("classpath:certain-pops-additional-household-members5.pdf") Resource certainPopsAddHHMembers5,
      @Value("classpath:certain-pops-additional-household-members6.pdf") Resource certainPopsAddHHMembers6
  ) {
    return new PDFBoxFieldFiller(List.of(coverPages, certainPops, certainPopsAddHHMembers,
        certainPopsAddHHMembers1, certainPopsAddHHMembers2, certainPopsAddHHMembers3,
        certainPopsAddHHMembers4, certainPopsAddHHMembers5, certainPopsAddHHMembers6));
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
  
  @Bean
  public Map<Recipient, Map<Document, PdfFieldFiller>> pdfFieldWithCAFHHSuppFillers(
      PdfFieldFiller caseworkerCafWdHouseholdSuppFiller,
      PdfFieldFiller clientCafWdHouseholdSuppFiller,
      PdfFieldFiller caseworkerCertainPopsWdAddtlIncomeFiller,
      PdfFieldFiller clientCertainPopsWdAddtlIncomeFiller) {
    return Map.of(
        CASEWORKER, Map.of(
            CAF, caseworkerCafWdHouseholdSuppFiller,
            CERTAIN_POPS, caseworkerCertainPopsWdAddtlIncomeFiller
        ),
        CLIENT, Map.of(
            CAF, clientCafWdHouseholdSuppFiller,
            CERTAIN_POPS, clientCertainPopsWdAddtlIncomeFiller )
    );
  }
  
  @Bean
  public Map<Recipient, Map<Document, Map<String, PdfFieldFiller>>> pdfFieldWithCertainPopsAdditionalHHFillers(
      PdfFieldFiller clientCertainPopsWithAdditionHHFiller,
      PdfFieldFiller clientCertainPopsWithAdditionHHFiller1,
      PdfFieldFiller clientCertainPopsWithAdditionHHFiller2,
      PdfFieldFiller clientCertainPopsWithAdditionHHFiller3,
      PdfFieldFiller clientCertainPopsWithAdditionHHFiller4,
      PdfFieldFiller clientCertainPopsWithAdditionHHFiller5,
      PdfFieldFiller clientCertainPopsWithAdditionHHFiller6,
      PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller,
      PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller1,
      PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller2,
      PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller3,
      PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller4,
      PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller5,
      PdfFieldFiller caseworkerCertainPopsWithAdditionHHFiller6
      ) {
    return Map.of(
        CASEWORKER, Map.of(
            CERTAIN_POPS, Map.of("1.0",caseworkerCertainPopsWithAdditionHHFiller,
                                 "2.0",caseworkerCertainPopsWithAdditionHHFiller1,
                                 "3.0",caseworkerCertainPopsWithAdditionHHFiller2,
                                 "4.0",caseworkerCertainPopsWithAdditionHHFiller3,
                                 "5.0",caseworkerCertainPopsWithAdditionHHFiller4,
                                 "6.0",caseworkerCertainPopsWithAdditionHHFiller5,
                                 "7.0",caseworkerCertainPopsWithAdditionHHFiller6)
        ),
        CLIENT, Map.of(
            CERTAIN_POPS, Map.of("1.0",clientCertainPopsWithAdditionHHFiller,
                                 "2.0",clientCertainPopsWithAdditionHHFiller1,
                                 "3.0",clientCertainPopsWithAdditionHHFiller2,
                                 "4.0",clientCertainPopsWithAdditionHHFiller3,
                                 "5.0",clientCertainPopsWithAdditionHHFiller4,
                                 "6.0",clientCertainPopsWithAdditionHHFiller5,
                                 "7.0",clientCertainPopsWithAdditionHHFiller6))
    );
  }
}
