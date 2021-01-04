package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;

@Component
public class AdultRequestingChildcareFullNameInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        List<ApplicationInput> applicationInputs = new ArrayList<>();

        List<String> adultsLookingForAJob = getListOfSelectedFullNames(application, "whoIsLookingForAJob", "whoIsLookingForAJob");
        AtomicInteger iLookingForAJob = new AtomicInteger(0);
        adultsLookingForAJob.forEach(fullName -> applicationInputs.add(new ApplicationInput("adultRequestingChildcareLookingForJob", "fullName",
                        List.of(fullName), ApplicationInputType.SINGLE_VALUE, iLookingForAJob.getAndIncrement())));

        return applicationInputs;
    }
    
}
