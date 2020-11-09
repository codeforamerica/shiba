package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Component
public class FullNameInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {
        String pageName = "householdSelectionForIncome";
        String whoseJob = "whoseJobIsIt";
        String groupName = "jobs";
        Subworkflow subworkflow = application.getApplicationData().getSubworkflows().get(groupName);

        return ofNullable(subworkflow).orElse(new Subworkflow()).stream()
                .filter(iteration -> iteration.getPagesData().get(pageName) != null)
                .map(iteration -> {
                    PageData pageData = iteration.getPagesData().get(pageName);
                    String fullName = FullNameFormatter.format(pageData.get(whoseJob).getValue(0));

                    return new ApplicationInput(pageName, "employeeFullName",
                            List.of(fullName), ApplicationInputType.SINGLE_VALUE, subworkflow.indexOf(iteration));
                }).collect(Collectors.toList());
    }
}
