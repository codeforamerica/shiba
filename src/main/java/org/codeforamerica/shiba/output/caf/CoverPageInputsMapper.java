package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.internationalization.LocaleSpecificMessageSource;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class CoverPageInputsMapper implements ApplicationInputsMapper {
    private final CountyMap<Map<Recipient, String>> countyInstructionsMapping;
    private final CountyMap<MnitCountyInformation> countyInformationMapping;
    private final MessageSource messageSource;

    public static final String CHILDCARE_WAITING_LIST_UTM_SOURCE = "childcare_waiting_list";
    private static final Map<String, String> UTM_SOURCE_MAPPING = Map.of(CHILDCARE_WAITING_LIST_UTM_SOURCE, "FROM BSF WAITING LIST");

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CoverPageInputsMapper(CountyMap<Map<Recipient, String>> countyInstructionsMapping, CountyMap<MnitCountyInformation> countyInformationMapping, MessageSource messageSource) {
        this.countyInstructionsMapping = countyInstructionsMapping;
        this.countyInformationMapping = countyInformationMapping;
        this.messageSource = messageSource;
    }

    @Override
    public List<ApplicationInput> map(Application application, Document document, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        var programsInput = getPrograms(application);
        var fullNameInput = getFullName(application);
        var householdMemberInputs = getHouseholdMembers(application);
        var countyInstructionsInput = getCountyInstructions(application, recipient);
        var utmSourceInput = getUtmSource(application, document);
        return combineCoverPageInputs(programsInput, fullNameInput, countyInstructionsInput, utmSourceInput, householdMemberInputs);
    }

    @Nullable
    private ApplicationInput getUtmSource(Application application, Document document) {
        ApplicationInput utmSourceInput = null;
        if (document == Document.CCAP) {
            var utmSource = application.getApplicationData().getUtmSource();
            var applicationUtmSource = utmSource != null ? utmSource : "";
            utmSourceInput = new ApplicationInput("nonPagesData", "utmSource", UTM_SOURCE_MAPPING.getOrDefault(applicationUtmSource, ""), SINGLE_VALUE);
        }
        return utmSourceInput;
    }

    @NotNull
    private List<ApplicationInput> combineCoverPageInputs(ApplicationInput programsInput, ApplicationInput fullNameInput, ApplicationInput countyInstructionsInput, ApplicationInput utmSourceInput, List<ApplicationInput> householdMemberInputs) {
        var everythingExceptHouseholdMembers = new ArrayList<ApplicationInput>();
        everythingExceptHouseholdMembers.add(programsInput);
        everythingExceptHouseholdMembers.add(fullNameInput);
        everythingExceptHouseholdMembers.add(countyInstructionsInput);
        everythingExceptHouseholdMembers.add(utmSourceInput);
        everythingExceptHouseholdMembers.addAll(householdMemberInputs);
        return everythingExceptHouseholdMembers.stream().filter(Objects::nonNull).toList();
    }

    private ApplicationInput getPrograms(Application application) {
        var programsPageData = ofNullable(application.getApplicationData().getPagesData().getPage("choosePrograms"));
        return programsPageData
                .map(pd -> pd.get("programs"))
                .map(InputData::getValue)
                .map(values -> String.join(", ", values))
                .map(value -> new ApplicationInput("coverPage", "programs", value, SINGLE_VALUE))
                .orElse(null);
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
        var householdSubworkflow = ofNullable(application.getApplicationData().getSubworkflows().get("household"));
        return householdSubworkflow.map(this::getApplicationInputsForSubworkflow).orElse(emptyList());
    }

    @NotNull
    private List<ApplicationInput> getApplicationInputsForSubworkflow(Subworkflow subworkflow) {
        List<ApplicationInput> inputsForSubworkflow = new ArrayList<>();
        for (int i = 0; i < subworkflow.size(); i++) {
            var householdMemberInfo = subworkflow.get(i).getPagesData().get("householdMemberInfo");
            var firstName = householdMemberInfo.get("firstName").getValue(0);
            var lastName = householdMemberInfo.get("lastName").getValue(0);
            var fullName = firstName + " " + lastName;
            inputsForSubworkflow.add(new ApplicationInput("coverPage", "fullName", fullName, SINGLE_VALUE, i));

            var programs = String.join(", ", householdMemberInfo.get("programs").getValue());
            inputsForSubworkflow.add(new ApplicationInput("coverPage", "programs", programs, SINGLE_VALUE, i));
        }
        return inputsForSubworkflow;
    }

    private ApplicationInput getCountyInstructions(Application application, Recipient recipient) {
        LocaleSpecificMessageSource lms = new LocaleSpecificMessageSource(LocaleContextHolder.getLocale(), messageSource);
        var messageCode = countyInstructionsMapping.get(application.getCounty()).get(recipient);
        var args = List.of(application.getCounty().displayName(), ofNullable(countyInformationMapping.get(application.getCounty()).getPhoneNumber()).orElse(null));
        var countyInstructions = lms.getMessage(messageCode, args);

        return new ApplicationInput("coverPage", "countyInstructions", countyInstructions, SINGLE_VALUE);
    }
}