package org.codeforamerica.shiba.output.caf;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_FIRST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_INFO_LAST_NAME;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.HOUSEHOLD_PROGRAMS;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.LINEAL_DESCENDANT_WEN;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.SELECTED_TRIBAL_NATION;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.TRIBAL_NATION;
import static org.codeforamerica.shiba.TribalNation.WhiteEarthNation;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getBooleanValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.codeforamerica.shiba.output.documentfieldpreparers.ApplicantProgramsPreparer.prepareProgramSelections;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.codeforamerica.shiba.RoutingDestinationMessageService;
import org.codeforamerica.shiba.ServicingAgencyMap;
import org.codeforamerica.shiba.TribalNationRoutingDestination;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.mnit.RoutingDestination;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparer;
import org.codeforamerica.shiba.pages.RoutingDecisionService;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CoverPagePreparer implements DocumentFieldPreparer {

  public static final String CHILDCARE_WAITING_LIST_UTM_SOURCE = "childcare_waiting_list";
  private static final Map<String, String> UTM_SOURCE_MAPPING =
      Map.of(CHILDCARE_WAITING_LIST_UTM_SOURCE, "FROM BSF WAITING LIST");
  private static final Map<Recipient, String> countyInstructionsMapping = Map.of(
      CLIENT, "county-to-instructions.generic-client",
      CASEWORKER, "county-to-instructions.generic-caseworker"
  );
  private final MessageSource messageSource;
  private final RoutingDecisionService routingDecisionService;
  private final RoutingDestinationMessageService routingDestinationMessageService;
  private final ServicingAgencyMap<TribalNationRoutingDestination> tribalNations;

  public CoverPagePreparer(MessageSource messageSource,
      RoutingDecisionService routingDecisionService,
      RoutingDestinationMessageService routingDestinationMessageService, ServicingAgencyMap<TribalNationRoutingDestination> tribalNations) {
    this.messageSource = messageSource;
    this.routingDecisionService = routingDecisionService;
    this.routingDestinationMessageService = routingDestinationMessageService;
    this.tribalNations = tribalNations;
  }

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient) {
    var programsInput = getPrograms(application);
    var fullNameInput = getFullName(application);
    var tribalAffiliationInput = getTribalAffiliation(application);
    var householdMemberInputs = getHouseholdMembers(application);
    var countyInstructionsInput = getCountyInstructions(application, recipient, document);
    var utmSourceInput = getUtmSource(application, document);
    var documentDestinations = getDocumentDestinations(application, recipient, document);
    return combineCoverPageInputs(programsInput, fullNameInput, countyInstructionsInput,
        utmSourceInput, householdMemberInputs, tribalAffiliationInput, documentDestinations);
  }

  @Nullable
  private DocumentField getUtmSource(Application application, Document document) {
    DocumentField utmSourceInput = null;
    if (document == Document.CCAP) {
      var utmSource = application.getApplicationData().getUtmSource();
      var applicationUtmSource = utmSource != null ? utmSource : "";
      utmSourceInput = new DocumentField("nonPagesData", "utmSource",
          UTM_SOURCE_MAPPING.getOrDefault(applicationUtmSource, ""), SINGLE_VALUE);
    }
    return utmSourceInput;
  }

  @NotNull
  private List<DocumentField> combineCoverPageInputs(DocumentField programsInput,
      DocumentField fullNameInput, DocumentField countyInstructionsInput,
      DocumentField utmSourceInput, List<DocumentField> householdMemberInputs,
      DocumentField tribalAffiliationInput, DocumentField documentDestinationsInput) {
    var everythingExceptHouseholdMembers = new ArrayList<DocumentField>();
    everythingExceptHouseholdMembers.add(programsInput);
    everythingExceptHouseholdMembers.add(fullNameInput);
    everythingExceptHouseholdMembers.add(tribalAffiliationInput);
    everythingExceptHouseholdMembers.add(countyInstructionsInput);
    everythingExceptHouseholdMembers.add(utmSourceInput);
    everythingExceptHouseholdMembers.add(documentDestinationsInput);
    everythingExceptHouseholdMembers.addAll(householdMemberInputs);
    return everythingExceptHouseholdMembers.stream().filter(Objects::nonNull).toList();
  }

  private DocumentField getPrograms(Application application) {
    List<String> programs = prepareProgramSelections(application);

    if (!programs.isEmpty()) {
      return new DocumentField("coverPage", "programs", String.join(", ", programs), SINGLE_VALUE);
    }
    return null;
  }

  private DocumentField getFullName(Application application) {
    var value = FullNameFormatter.getFullName(application);
    if (value == null) {
      return null;
    }
    return new DocumentField("coverPage", "fullName", value, SINGLE_VALUE);
  }

	private DocumentField getTribalAffiliation(Application application) {
		String value = null;
		Boolean isTribalNationMember = getBooleanValue(application.getApplicationData().getPagesData(), TRIBAL_NATION);

		if (Boolean.TRUE.equals(isTribalNationMember)) {
			value = getFirstValue(application.getApplicationData().getPagesData(), SELECTED_TRIBAL_NATION);
		}
			if (getBooleanValue(application.getApplicationData().getPagesData(), LINEAL_DESCENDANT_WEN)) {
				value = tribalNations.get(WhiteEarthNation).getName();
			}

		//}
		return new DocumentField("coverPage", "tribal", value, SINGLE_VALUE);
	}

  private List<DocumentField> getHouseholdMembers(Application application) {
    var householdSubworkflow =
        ofNullable(getGroup(application.getApplicationData(), Group.HOUSEHOLD));
    return householdSubworkflow.map(this::getApplicationInputsForSubworkflow).orElse(emptyList());
  }

  @NotNull
  private List<DocumentField> getApplicationInputsForSubworkflow(Subworkflow subworkflow) {
    List<DocumentField> inputsForSubworkflow = new ArrayList<>();
    for (int i = 0; i < subworkflow.size(); i++) {
      var pagesData = subworkflow.get(i).getPagesData();
      var firstName = getFirstValue(pagesData, HOUSEHOLD_INFO_FIRST_NAME);
      var lastName = getFirstValue(pagesData, HOUSEHOLD_INFO_LAST_NAME);
      var fullName = firstName + " " + lastName;
      inputsForSubworkflow
          .add(new DocumentField("coverPage", "fullName", fullName, SINGLE_VALUE, i));

      var programs = String.join(", ", getValues(pagesData, HOUSEHOLD_PROGRAMS));
      inputsForSubworkflow
          .add(new DocumentField("coverPage", "programs", programs, SINGLE_VALUE, i));
    }
    return inputsForSubworkflow;
  }

  private DocumentField getCountyInstructions(Application application, Recipient recipient,
      Document document) {
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
    var messageCode = countyInstructionsMapping.get(recipient);
    var county = application.getCounty();
    var routingDestinations =
        routingDecisionService.getRoutingDestinations(application.getApplicationData(), document);
    var coverPageMessageStrings = List.of(
        routingDestinationMessageService.generatePhrase(locale, county, false, routingDestinations),
        routingDestinationMessageService.generatePhrase(locale, county, true, routingDestinations));

    var countyInstructions = lms.getMessage(messageCode, coverPageMessageStrings);

    return new DocumentField("coverPage", "countyInstructions", countyInstructions, SINGLE_VALUE);
  }
  
  private DocumentField getDocumentDestinations(Application application, Recipient recipient, Document document) {
	List<RoutingDestination> routingDestinations = routingDecisionService
			.getRoutingDestinations(application.getApplicationData(), document);
	Locale locale = LocaleContextHolder.getLocale();
	String destinations = routingDestinationMessageService.generatePhrase(locale, application.getCounty(), false, routingDestinations);

	return new DocumentField("coverPage", "documentDestinations", destinations, SINGLE_VALUE);
  }
}
