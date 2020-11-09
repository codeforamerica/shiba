package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class HouseholdPregnancyMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {

        List<String> pregnantHouseholdMembers = Optional.ofNullable(application.getApplicationData().getPageData("whoIsPregnant"))
                .map(pageData -> pageData.get("whoIsPregnant"))
                .map(InputData::getValue)
                .orElse(List.of(""))
                .stream().map(FullNameFormatter::format).collect(Collectors.toList());

        return List.of(
                new ApplicationInput("householdPregnancy", "householdPregnancy",
                        List.of(String.join(", ", pregnantHouseholdMembers)), ApplicationInputType.SINGLE_VALUE, null)
        );
    }
}
