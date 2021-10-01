package org.codeforamerica.shiba.output.caf;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.APPLICANT_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_FIRST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_LAST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.RoutingDestinationMessageService;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.mnit.CountyRoutingDestination;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CoverPageInputsMapper implements ApplicationInputsMapper {

  public static final String CHILDCARE_WAITING_LIST_UTM_SOURCE = "childcare_waiting_list";
  private static final Map<String, String> UTM_SOURCE_MAPPING = Map
      .of(CHILDCARE_WAITING_LIST_UTM_SOURCE, "FROM BSF WAITING LIST");
  private final CountyMap<Map<Recipient, String>> countyInstructionsMapping;
  private final CountyMap<CountyRoutingDestination> countyInformationMapping;
  private final MessageSource messageSource;
  private final RoutingDecisionService routingDecisionService;
  private final RoutingDestinationMessageService routingDestinationMessageService;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  public CoverPageInputsMapper(CountyMap<Map<Recipient, String>> countyInstructionsMapping,
      CountyMap<CountyRoutingDestination> countyInformationMapping,
      MessageSource messageSource,
      RoutingDecisionService routingDecisionService,
      RoutingDestinationMessageService routingDestinationMessageService) {
    this.countyInstructionsMapping = countyInstructionsMapping;
    this.countyInformationMapping = countyInformationMapping;
    this.messageSource = messageSource;
    this.routingDecisionService = routingDecisionService;
    this.routingDestinationMessageService = routingDestinationMessageService;
  }

  @Override
  public List<ApplicationInput> map(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    var programsInput = getPrograms(application);
    var fullNameInput = getFullName(application);
    var householdMemberInputs = getHouseholdMembers(application);
    var countyInstructionsInput = getCountyInstructions(application, recipient, document);
    var utmSourceInput = getUtmSource(application, document);
    return combineCoverPageInputs(programsInput, fullNameInput, countyInstructionsInput,
        utmSourceInput, householdMemberInputs);
  }

  @Nullable
  private ApplicationInput getUtmSource(Application application, Document document) {
    ApplicationInput utmSourceInput = null;
    if (document == Document.CCAP) {
      var utmSource = application.getApplicationData().getUtmSource();
      var applicationUtmSource = utmSource != null ? utmSource : "";
      utmSourceInput = new ApplicationInput("nonPagesData", "utmSource",
          UTM_SOURCE_MAPPING.getOrDefault(applicationUtmSource, ""), SINGLE_VALUE);
    }
    return utmSourceInput;
  }

  @NotNull
  private List<ApplicationInput> combineCoverPageInputs(ApplicationInput programsInput,
      ApplicationInput fullNameInput, ApplicationInput countyInstructionsInput,
      ApplicationInput utmSourceInput, List<ApplicationInput> householdMemberInputs) {
    var everythingExceptHouseholdMembers = new ArrayList<ApplicationInput>();
    everythingExceptHouseholdMembers.add(programsInput);
    everythingExceptHouseholdMembers.add(fullNameInput);
    everythingExceptHouseholdMembers.add(countyInstructionsInput);
    everythingExceptHouseholdMembers.add(utmSourceInput);
    everythingExceptHouseholdMembers.addAll(householdMemberInputs);
    return everythingExceptHouseholdMembers.stream().filter(Objects::nonNull).toList();
  }

  private ApplicationInput getPrograms(Application application) {
    List<String> programs = getValues(application.getApplicationData().getPagesData(),
        APPLICANT_PROGRAMS);
    if (isTribalTANF(application)) {
      programs.add("TRIBAL TANF");
    }

    if (isMFIP(application)) {
      if (!programs.contains("CASH")) {
        programs.add("CASH");
      }
    }

    if (!programs.isEmpty()) {
      return new ApplicationInput("coverPage", "programs", String.join(", ", programs),
          SINGLE_VALUE);
    }
    return null;
  }

  private boolean isTribalTANF(Application application) {
    return application.getApplicationData().getPagesData()
        .safeGetPageInputValue("applyForTribalTANF", "applyForTribalTANF").contains("true");
  }

  private boolean isMFIP(Application application) {
    return application.getApplicationData().getPagesData()
        .safeGetPageInputValue("applyForMFIP", "applyForMFIP").contains("true");
  }

  private ApplicationInput getFullName(Application application) {
    var pageName = application.getFlow() == LATER_DOCS ? "matchInfo" : "personalInfo";
    return ofNullable(application.getApplicationData().getPagesData().getPage(pageName))
        .map(this::getFullNameString)
        .map(value -> new ApplicationInput("coverPage", "fullName", value, SINGLE_VALUE))
        .orElse(null);
  }

  @NotNull
  private String getFullNameString(PageData pageData) {
    var firstName = getValueOrEmptyString(pageData, "firstName");
    var lastName = getValueOrEmptyString(pageData, "lastName");
    return firstName + " " + lastName;
  }

  @NotNull
  private String getValueOrEmptyString(PageData pageData, String firstName) {
    return ofNullable(pageData.get(firstName))
        .map(InputData::getValue)
        .map(val -> String.join("", val))
        .orElse("");
  }

  private List<ApplicationInput> getHouseholdMembers(Application application) {
    var householdSubworkflow = ofNullable(
        getGroup(application.getApplicationData(), Group.HOUSEHOLD));
    return householdSubworkflow.map(this::getApplicationInputsForSubworkflow)
        .orElse(emptyList());
  }

  @NotNull
  private List<ApplicationInput> getApplicationInputsForSubworkflow(Subworkflow subworkflow) {
    List<ApplicationInput> inputsForSubworkflow = new ArrayList<>();
    for (int i = 0; i < subworkflow.size(); i++) {
      var pagesData = subworkflow.get(i).getPagesData();
      var firstName = getFirstValue(pagesData, HOUSEHOLD_INFO_FIRST_NAME);
      var lastName = getFirstValue(pagesData, HOUSEHOLD_INFO_LAST_NAME);
      var fullName = firstName + " " + lastName;
      inputsForSubworkflow
          .add(new ApplicationInput("coverPage", "fullName", fullName, SINGLE_VALUE, i));

      var programs = String.join(", ", getValues(pagesData, HOUSEHOLD_PROGRAMS));
      inputsForSubworkflow
          .add(new ApplicationInput("coverPage", "programs", programs, SINGLE_VALUE, i));
    }
    return inputsForSubworkflow;
  }

  private ApplicationInput getCountyInstructions(Application application, Recipient recipient, Document document) {
    Locale locale = switch (recipient) {
      case CASEWORKER -> LocaleContextHolder.getLocale();
      case CLIENT -> {
        var writtenLanguageSelection = application.getApplicationData().getPagesData()
            .safeGetPageInputValue("languagePreferences", "writtenLanguage");
        yield writtenLanguageSelection.contains("SPANISH") ? new Locale("es")
            : LocaleContextHolder.getLocale();
      }
    };

    var lms = new LocaleSpecificMessageSource(locale, messageSource);

    var messageCode = countyInstructionsMapping.get(application.getCounty()).get(recipient);

    String listOfRoutingDestinationsWithoutPhoneNumbers = RoutingDestinationMessageService
        .generatePhraseWithoutPhoneNumbers(locale, application.getCounty(), routingDecisionService.getRoutingDestinations(
            application.getApplicationData(), document), messageSource);

    String listOfRoutingDestinationsWithPhoneNumbers = RoutingDestinationMessageService
        .generatePhraseWithPhoneNumbers(locale, application.getCounty(), routingDecisionService.getRoutingDestinations(
            application.getApplicationData(), document), messageSource);

    var countyDisplayName = application.getCounty().displayName();
    var countyPhoneNumber = ofNullable(
        countyInformationMapping.get(application.getCounty()).getPhoneNumber())
        .orElse(null);
    var argsCAF = List.of(listOfRoutingDestinationsWithoutPhoneNumbers, listOfRoutingDestinationsWithPhoneNumbers);
    var argsCCAP = List.of(lms.getMessage("general.county", List.of(countyDisplayName)),
        lms.getMessage("general.county-and-phone", List.of(countyDisplayName, countyPhoneNumber)));

    var countyInstructionsCAF = lms.getMessage(messageCode, argsCAF);
    var countInstructionsCCAP = lms.getMessage(messageCode, argsCCAP);

    return document.equals(Document.CCAP) ? new ApplicationInput("coverPage", "countyInstructions", countInstructionsCCAP,
        SINGLE_VALUE) :
        new ApplicationInput("coverPage", "countyInstructions", countyInstructionsCAF,
        SINGLE_VALUE);
  }
}
