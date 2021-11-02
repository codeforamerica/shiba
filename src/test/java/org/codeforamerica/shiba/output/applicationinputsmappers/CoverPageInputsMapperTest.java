package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.County.Olmsted;
import static org.codeforamerica.shiba.County.Other;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.caf.CoverPageInputsMapper.CHILDCARE_WAITING_LIST_UTM_SOURCE;
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
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.CoverPageInputsMapper;
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
class CoverPageInputsMapperTest {

  private CountyMap<Map<Recipient, String>> countyInstructionsMapping;
  private CoverPageInputsMapper coverPageInputsMapper;
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
    coverPageInputsMapper = new CoverPageInputsMapper(countyInstructionsMapping,
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

    List<ApplicationInput> applicationInputs = coverPageInputsMapper
        .map(application, CAF, Recipient.CLIENT, null);

    assertThat(applicationInputs).contains(
        new ApplicationInput(
            "coverPage",
            "programs",
            List.of("SNAP, CASH"),
            ApplicationInputType.SINGLE_VALUE
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

    List<ApplicationInput> applicationInputs = coverPageInputsMapper
        .map(application, CAF, Recipient.CLIENT, null);

    assertThat(applicationInputs).contains(
        new ApplicationInput(
            "coverPage",
            "programs",
            List.of("SNAP, CASH"),
            ApplicationInputType.SINGLE_VALUE,
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

    List<ApplicationInput> applicationInputs = coverPageInputsMapper
        .map(application, CAF, Recipient.CLIENT, null);

    assertThat(applicationInputs).contains(
        new ApplicationInput(
            "coverPage",
            "fullName",
            List.of("Jane Testuser"),
            ApplicationInputType.SINGLE_VALUE,
            0
        ));
  }

  @Test
  void shouldNotIncludeProgramsOrFullNameInputsWhenThereIsNoProgramsOrPersonalInfoData() {
    Application application = Application.builder()
        .applicationData(applicationData)
        .county(Other)
        .build();

    List<String> appInputNames = coverPageInputsMapper
        .map(application, CAF, Recipient.CLIENT, null).stream()
        .map(ApplicationInput::getName)
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

    List<ApplicationInput> applicationInputs = coverPageInputsMapper
        .map(application, CAF, Recipient.CASEWORKER, null);
    assertThat(applicationInputs).contains(
        new ApplicationInput(
            "coverPage",
            "countyInstructions",
            "Olmsted caseworker",
            ApplicationInputType.SINGLE_VALUE
        ));

    applicationInputs = coverPageInputsMapper.map(application, CAF, Recipient.CLIENT, null);
    assertThat(applicationInputs).contains(
        new ApplicationInput(
            "coverPage",
            "countyInstructions",
            "Olmsted client",
            ApplicationInputType.SINGLE_VALUE
        ));

    new TestApplicationDataBuilder(applicationData)
        .withPageData("languagePreferences", "writtenLanguage", "SPANISH");
    applicationInputs = coverPageInputsMapper.map(application, CAF, Recipient.CLIENT, null);
    assertThat(applicationInputs).contains(
        new ApplicationInput(
            "coverPage",
            "countyInstructions",
            "Olmsted client instructions in spanish",
            ApplicationInputType.SINGLE_VALUE
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

    List<ApplicationInput> applicationInputs = coverPageInputsMapper
        .map(application, CAF, Recipient.CLIENT, null);

    assertThat(applicationInputs).contains(
        new ApplicationInput("coverPage", "fullName", List.of("someFirstName someLastName"),
            ApplicationInputType.SINGLE_VALUE)
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

    List<ApplicationInput> applicationInputs = coverPageInputsMapper
        .map(application, CAF, Recipient.CLIENT, null);

    assertThat(applicationInputs).contains(
        new ApplicationInput("coverPage", "fullName", List.of("someFirstName someLastName"),
            ApplicationInputType.SINGLE_VALUE)
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
    List<ApplicationInput> result = coverPageInputsMapper
        .map(application, Document.CCAP, Recipient.CLIENT, null);

    assertThat(result).contains(
        new ApplicationInput(
            "nonPagesData",
            "utmSource",
            List.of("FROM BSF WAITING LIST"),
            ApplicationInputType.SINGLE_VALUE));

    result = coverPageInputsMapper.map(application, Document.CCAP, Recipient.CLIENT, null);
    assertThat(result).doesNotContain(
        new ApplicationInput(
            "nonPagesData",
            "utmSource",
            List.of(""),
            ApplicationInputType.SINGLE_VALUE));
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
    List<ApplicationInput> result = coverPageInputsMapper
        .map(application, Document.CCAP, Recipient.CLIENT, null);

    assertThat(result).contains(
        new ApplicationInput(
            "nonPagesData",
            "utmSource",
            List.of(""),
            ApplicationInputType.SINGLE_VALUE));

    result = coverPageInputsMapper.map(application, Document.CCAP, Recipient.CLIENT, null);

    assertThat(result).contains(
        new ApplicationInput(
            "nonPagesData",
            "utmSource",
            List.of(""),
            ApplicationInputType.SINGLE_VALUE));
  }
}
