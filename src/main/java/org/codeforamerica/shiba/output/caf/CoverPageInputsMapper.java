package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.*;
import org.codeforamerica.shiba.mnit.MnitCountyInformation;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.output.applicationinputsmappers.SubworkflowIterationScopeTracker;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.codeforamerica.shiba.application.FlowType.LATER_DOCS;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class CoverPageInputsMapper implements ApplicationInputsMapper {
    private final CountyMap<Map<Recipient, String>> countyInstructionsMapping;
    private final CountyMap<MnitCountyInformation> countyInformationMapping;

    public static final String CHILDCARE_WAITING_LIST_UTM_SOURCE = "childcare_waiting_list";
    private static final Map<String, String> UTM_SOURCE_MAPPING = Map.of(
            CHILDCARE_WAITING_LIST_UTM_SOURCE, "FROM BSF WAITING LIST"
    );

    @Resource
    MessageSource messageSource;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CoverPageInputsMapper(CountyMap<Map<Recipient, String>> countyInstructionsMapping, CountyMap<MnitCountyInformation> countyInformationMapping, MessageSource messageSource) {
        this.countyInstructionsMapping = countyInstructionsMapping;
        this.countyInformationMapping = countyInformationMapping;
        this.messageSource = messageSource;
    }

    @Override
    public List<ApplicationInput> map(Application application, Document document, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        ApplicationInput programsInput = ofNullable(application.getApplicationData().getPagesData().getPage("choosePrograms"))
                .flatMap(pageData -> ofNullable(pageData.get("programs")))
                .map(InputData::getValue)
                .map(values -> String.join(", ", values))
                .map(value -> new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of(value),
                        SINGLE_VALUE))
                .orElse(null);

        String pageName = "personalInfo";
        if (application.getFlow() == LATER_DOCS) {
            pageName = "matchInfo";
        }
        ApplicationInput fullNameInput = ofNullable(application.getApplicationData().getPagesData().getPage(pageName))
                .map(pageData ->
                        Stream.concat(Stream.ofNullable(pageData.get("firstName")), Stream.ofNullable(pageData.get("lastName")))
                                .map(nameInput -> String.join("", nameInput.getValue()))
                                .collect(Collectors.joining(" ")))
                .map(value -> new ApplicationInput(
                        "coverPage",
                        "fullName",
                        List.of(value),
                        SINGLE_VALUE
                ))
                .orElse(null);

        List<ApplicationInput> householdMemberInputs = ofNullable(application.getApplicationData().getSubworkflows().get("household"))
                .stream().map(subworkflow -> IntStream.range(0, subworkflow.size()).mapToObj(i -> {
                            PageData householdMemberInfo = subworkflow.get(i).getPagesData().get("householdMemberInfo");

                            String firstName = householdMemberInfo.get("firstName").getValue(0);
                            String lastName = householdMemberInfo.get("lastName").getValue(0);
                            ApplicationInput fullName = new ApplicationInput("coverPage", "fullName",
                                    List.of(String.join(" ", firstName, lastName)),
                                    SINGLE_VALUE, i);

                            ApplicationInput programs = new ApplicationInput("coverPage", "programs",
                                    List.of(String.join(", ", householdMemberInfo.get("programs").getValue())),
                                    SINGLE_VALUE, i);

                            return List.of(fullName, programs);
                        }).flatMap(Collection::stream).collect(Collectors.toList())
                ).flatMap(Collection::stream).collect(Collectors.toList());

        ApplicationInput countyInstructionsInput = new ApplicationInput(
                "coverPage",
                "countyInstructions",
                List.of(messageSource.getMessage(countyInstructionsMapping.get(application.getCounty()).get(recipient),
                        List.of(application.getCounty().displayName(),
                                Optional.ofNullable(countyInformationMapping.get(application.getCounty()).getPhoneNumber()).orElse(null)
                        ).toArray(),
                        LocaleContextHolder.getLocale())),
                SINGLE_VALUE
        );

        ApplicationInput utmSourceInput = null;
        if (document == Document.CCAP) {
            String applicationUtmSource = (application.getApplicationData().getUtmSource() != null) ? application.getApplicationData().getUtmSource() : "";
            utmSourceInput = new ApplicationInput("nonPagesData", "utmSource", List.of(UTM_SOURCE_MAPPING.getOrDefault(applicationUtmSource, "")), SINGLE_VALUE);
        }

        List<ApplicationInput> inputs = Stream.of(
                of(countyInstructionsInput),
                ofNullable(programsInput),
                ofNullable(fullNameInput),
                ofNullable(utmSourceInput)
        ).flatMap(Optional::stream).collect(Collectors.toList());
        inputs.addAll(householdMemberInputs);

        return inputs;
    }
}