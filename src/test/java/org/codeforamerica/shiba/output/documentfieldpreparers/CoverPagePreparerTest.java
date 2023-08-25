package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.County.Other;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.caf.CoverPagePreparer.CHILDCARE_WAITING_LIST_UTM_SOURCE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.RoutingDestinationMessageService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.FlowType;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.CoverPagePreparer;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CoverPagePreparerTest {

  private CoverPagePreparer preparer;
  private ApplicationData applicationData;
  @MockBean
  private RoutingDecisionService routingDecisionService;
  @MockBean
  private RoutingDestinationMessageService routingDestinationMessageService;

  @BeforeEach
  public void setUp() {
    StaticMessageSource staticMessageSource = new StaticMessageSource();
    staticMessageSource.addMessages(Map.of(
        "county-to-instructions.generic-client", "Client Instructions",
        "county-to-instructions.generic-caseworker", "Caseworker Instructions"), LocaleContextHolder.getLocale()
    );
    staticMessageSource.addMessage(
        "county-to-instructions.generic-client", new Locale("es"), "Client Instructions En Español"
    );
    applicationData = new ApplicationData();
    preparer = new CoverPagePreparer(staticMessageSource, routingDecisionService,
        routingDestinationMessageService);
    CountyRoutingDestination countyRoutingDestination = new CountyRoutingDestination(
        Hennepin, "someDhsProviderId", "someEmail", "555-123-4567");
    when(routingDecisionService.getRoutingDestinations(any(ApplicationData.class),
        any(Document.class)))
        .thenReturn(List.of(countyRoutingDestination));
    when(routingDestinationMessageService.generatePhrase(any(), any(), anyBoolean(),
        any())).thenReturn("");
  }

  @Test
  void shouldIncludeProgramsInputWithCombinedProgramSelection() {
    new TestApplicationDataBuilder(applicationData)
        .withApplicantPrograms(List.of("SNAP", "CASH"));
    Application application = Application.builder()
        .applicationData(applicationData)
        .county(Other)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, CAF, Recipient.CLIENT);

    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "programs",
            List.of("SNAP, CASH"),
            DocumentFieldType.SINGLE_VALUE
        ));
  }

  @Test
  void shouldIncludeTribalAffiliation() {
    new TestApplicationDataBuilder(applicationData)
        .withPageData("selectTheTribe", "selectedTribe", "Mille Lacs Band of Ojibwe");
    Application application = Application.builder()
        .applicationData(applicationData)
        .county(Other)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, CAF, Recipient.CLIENT);

    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "tribal",
            List.of("Mille Lacs Band of Ojibwe"),
            DocumentFieldType.SINGLE_VALUE
        ));
  }

  @Test
  void shouldIncludeSubworkflowProgramsInputWithCombinedProgramSelection() {
    new TestApplicationDataBuilder(applicationData)
        .withSubworkflow("household", new PagesDataBuilder()
            .withPageData("householdMemberInfo", Map.of(
                "programs", List.of("SNAP", "CASH"),
                "firstName", List.of("Jane"),
                "lastName", List.of("Testuser")
            )));

    Application application = Application.builder()
        .applicationData(applicationData)
        .county(Other)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, CAF, Recipient.CLIENT);

    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "programs",
            List.of("SNAP, CASH"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ));
  }


  @Test
  void shouldIncludeSubworkflowFullNames() {
    new TestApplicationDataBuilder(applicationData)
        .withSubworkflow("household", new PagesDataBuilder()
            .withPageData("householdMemberInfo", Map.of(
                "programs", List.of("SNAP", "CASH"),
                "firstName", List.of("Jane"),
                "lastName", List.of("Testuser")
            )));
    Application application = Application.builder()
        .applicationData(applicationData)
        .county(Other)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, CAF, Recipient.CLIENT);

    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "fullName",
            List.of("Jane Testuser"),
            DocumentFieldType.SINGLE_VALUE,
            0
        ));
  }

  @Test
  void shouldNotIncludeProgramsOrFullNameInputsWhenThereIsNoProgramsOrPersonalInfoData() {
    Application application = Application.builder()
        .applicationData(applicationData)
        .county(Other)
        .build();

    List<String> appInputNames = preparer
        .prepareDocumentFields(application, CAF, Recipient.CLIENT).stream()
        .map(DocumentField::getName)
        .collect(Collectors.toList());

    assertThat(appInputNames).doesNotContain("programs");
    assertThat(appInputNames).doesNotContain("fullName");
  }

  @Test
  void shouldIncludeCombinedFirstNameAndLastNameInput() {
    new TestApplicationDataBuilder(applicationData)
        .withPageData("personalInfo", "firstName", "someFirstName")
        .withPageData("personalInfo", "lastName", "someLastName");
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Other)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, CAF, Recipient.CLIENT);

    assertThat(documentFields).contains(
        new DocumentField("coverPage", "fullName", List.of("someFirstName someLastName"),
            DocumentFieldType.SINGLE_VALUE)
    );
  }

  @Test
  void shouldIncludeCombinedFirstNameAndLastNameInputForLaterDocs() {
    new TestApplicationDataBuilder(applicationData)
        .withPageData("matchInfo", "firstName", "someFirstName")
        .withPageData("matchInfo", "lastName", "someLastName");
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Other)
        .timeToComplete(null)
        .flow(FlowType.LATER_DOCS)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, CAF, Recipient.CLIENT);

    assertThat(documentFields).contains(
        new DocumentField("coverPage", "fullName", List.of("someFirstName someLastName"),
            DocumentFieldType.SINGLE_VALUE)
    );
  }


  @Test
  void shouldMapRecognizedUtmSourceForCCAPOnly() {
    applicationData.setUtmSource(CHILDCARE_WAITING_LIST_UTM_SOURCE);

    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Other)
        .timeToComplete(null)
        .build();
    List<DocumentField> result = preparer
        .prepareDocumentFields(application, Document.CCAP, Recipient.CLIENT);

    assertThat(result).contains(
        new DocumentField(
            "nonPagesData",
            "utmSource",
            List.of("FROM BSF WAITING LIST"),
            DocumentFieldType.SINGLE_VALUE));

    result = preparer.prepareDocumentFields(application, Document.CCAP,
        Recipient.CLIENT);
    assertThat(result).doesNotContain(
        new DocumentField(
            "nonPagesData",
            "utmSource",
            List.of(""),
            DocumentFieldType.SINGLE_VALUE));
  }

  @Test
  void shouldMapUnrecognizedUtmSourceToEmptyForCCAPAndCAF() {
    applicationData.setUtmSource("somewhere_unknown");

    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Other)
        .timeToComplete(null)
        .build();
    List<DocumentField> result = preparer
        .prepareDocumentFields(application, Document.CCAP, Recipient.CLIENT);

    assertThat(result).contains(
        new DocumentField(
            "nonPagesData",
            "utmSource",
            List.of(""),
            DocumentFieldType.SINGLE_VALUE));

    result = preparer.prepareDocumentFields(application, Document.CCAP,
        Recipient.CLIENT);

    assertThat(result).contains(
        new DocumentField(
            "nonPagesData",
            "utmSource",
            List.of(""),
            DocumentFieldType.SINGLE_VALUE));
  }

  @Test
  void shouldIncludeCountyInstructionsInputBasedOnRecipientAndLanguage() {
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Olmsted)
        .timeToComplete(null)
        .build();

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, CAF, Recipient.CASEWORKER);
    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "countyInstructions",
            "Caseworker Instructions",
            DocumentFieldType.SINGLE_VALUE
        ));

    documentFields = preparer.prepareDocumentFields(application, CAF, Recipient.CLIENT);
    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "countyInstructions",
            "Client Instructions",
            DocumentFieldType.SINGLE_VALUE
        ));
    new TestApplicationDataBuilder(applicationData)
        .withPageData("languagePreferences", "writtenLanguage", "SPANISH");

    documentFields = preparer.prepareDocumentFields(application, CAF, Recipient.CLIENT);
    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "countyInstructions",
            "Client Instructions En Español",
            DocumentFieldType.SINGLE_VALUE
        ));
  }

  @Test
  void laterDocsCoverPageShouldIncludeDestinations() {
    Application application = Application.builder()
            .id("someId")
            .completedAt(ZonedDateTime.now())
            .applicationData(applicationData)
            .timeToComplete(null)
            .flow(FlowType.LATER_DOCS)
            .build();
    
    when(routingDestinationMessageService.generatePhrase(any(), any(), anyBoolean(), any())).thenReturn("to infinity and beyond...");

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, Document.UPLOADED_DOC, Recipient.CASEWORKER);

    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "documentDestinations",
            "to infinity and beyond...",
            DocumentFieldType.SINGLE_VALUE
        ));

  }
  
}
