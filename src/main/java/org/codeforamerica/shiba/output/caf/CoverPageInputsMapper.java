package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.CountyMap;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.applicationinputsmappers.ApplicationInputsMapper;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class CoverPageInputsMapper implements ApplicationInputsMapper {
    private final CountyMap<Map<Recipient, String>> countyInstructionsMapping;

    public CoverPageInputsMapper(CountyMap<Map<Recipient, String>> countyInstructionsMapping) {
        this.countyInstructionsMapping = countyInstructionsMapping;
    }

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {
        ApplicationInput programsInput = Optional.ofNullable(application.getApplicationData().getPagesData().getPage("choosePrograms"))
                .flatMap(pageData -> Optional.ofNullable(pageData.get("programs")))
                .map(InputData::getValue)
                .map(values -> String.join(", ", values))
                .map(value -> new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of(value),
                        SINGLE_VALUE))
                .orElse(null);
        ApplicationInput fullNameInput = Optional.ofNullable(application.getApplicationData().getPagesData().getPage("personalInfo"))
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

        return Stream.concat(
                Stream.of(new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(countyInstructionsMapping.get(application.getCounty()).get(recipient)),
                        SINGLE_VALUE)),
                Stream.concat(Stream.ofNullable(programsInput), Stream.ofNullable(fullNameInput)))
                .collect(Collectors.toList());
    }
}
