package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.codeforamerica.shiba.CountyMap;
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

  private CountyMap<Map<Recipient, String>> countyInstructionsMapping;
  private CoverPagePreparer preparer;
  private ApplicationData applicationData;
  @MockBean
  private RoutingDecisionService routingDecisionService;
  @MockBean
  private RoutingDestinationMessageService routingDestinationMessageService;

  @BeforeEach
  public void setUp() {

    countyInstructionsMapping = new CountyMap<>();
    CountyMap<CountyRoutingDestination> countyInformationMapping = new CountyMap<>();
    StaticMessageSource staticMessageSource = new StaticMessageSource();
    applicationData = new ApplicationData();
    preparer = new CoverPagePreparer(countyInstructionsMapping,
        countyInformationMapping, staticMessageSource, routingDecisionService,
        routingDestinationMessageService);
    countyInstructionsMapping.getCounties().put(Other, Map.of(
        Recipient.CLIENT, "county-to-instructions.default-client",
        Recipient.CASEWORKER, "county-to-instructions.default-caseworker"));
    CountyRoutingDestination countyRoutingDestination = CountyRoutingDestination.builder()
        .dhsProviderId("someDhsProviderId")
        .email("someEmail")
        .phoneNumber("555-123-4567")
        .folderId("someFolderId")
        .build();
    when(routingDecisionService.getRoutingDestinations(any(ApplicationData.class),
        any(Document.class)))
        .thenReturn(List.of(countyRoutingDestination));
    when(routingDestinationMessageService.generatePhrase(any(), any(), anyBoolean(),
        any())).thenReturn("");
    countyInformationMapping.setDefaultValue(countyRoutingDestination);
    staticMessageSource
        .addMessage("county-to-instructions.default-client",
            LocaleContextHolder.getLocale(),
            "Default client");
    staticMessageSource
        .addMessage("county-to-instructions.default-caseworker",
            LocaleContextHolder.getLocale(),
            "Default caseworker");
    staticMessageSource
        .addMessage("county-to-instructions.olmsted-caseworker",
            LocaleContextHolder.getLocale(),
            "Olmsted caseworker");
    staticMessageSource
        .addMessage("county-to-instructions.olmsted-client",
            LocaleContextHolder.getLocale(),
            "Olmsted client");
    staticMessageSource.addMessage("county-to-instructions.olmsted-client", new Locale("es"),
        "Olmsted client instructions in spanish");
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
  void shouldIncludeCountyInstructionsInputWithMatchingCountyInstructions() {
    Application application = Application.builder()
        .id("someId")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(Olmsted)
        .timeToComplete(null)
        .build();
    countyInstructionsMapping.getCounties().put(Olmsted, Map.of(
        Recipient.CLIENT, "county-to-instructions.olmsted-client",
        Recipient.CASEWORKER, "county-to-instructions.olmsted-caseworker"
    ));

    List<DocumentField> documentFields = preparer
        .prepareDocumentFields(application, CAF, Recipient.CASEWORKER);
    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "countyInstructions",
            "Olmsted caseworker",
            DocumentFieldType.SINGLE_VALUE
        ));

    documentFields = preparer.prepareDocumentFields(application, CAF, Recipient.CLIENT
    );
    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "countyInstructions",
            "Olmsted client",
            DocumentFieldType.SINGLE_VALUE
        ));

    new TestApplicationDataBuilder(applicationData)
        .withPageData("languagePreferences", "writtenLanguage", "SPANISH");
    documentFields = preparer.prepareDocumentFields(application, CAF, Recipient.CLIENT
    );
    assertThat(documentFields).contains(
        new DocumentField(
            "coverPage",
            "countyInstructions",
            "Olmsted client instructions in spanish",
            DocumentFieldType.SINGLE_VALUE
        ));
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
}
