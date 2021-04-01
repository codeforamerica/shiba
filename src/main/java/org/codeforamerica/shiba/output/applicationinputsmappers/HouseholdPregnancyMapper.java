package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;

@Component
public class HouseholdPregnancyMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Document document, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        List<String> pregnantHouseholdMembers = getListOfSelectedFullNames(application, "whoIsPregnant", "whoIsPregnant");

        return List.of(
                new ApplicationInput("householdPregnancy", "householdPregnancy",
                        List.of(String.join(", ", pregnantHouseholdMembers)), ApplicationInputType.SINGLE_VALUE, null)
        );
    }
}
