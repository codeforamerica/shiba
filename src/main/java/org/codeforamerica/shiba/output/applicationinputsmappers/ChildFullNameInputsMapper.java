package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;

@Component
public class ChildFullNameInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Document document, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {

        List<String> childrenNeedChildcare = getListOfSelectedFullNames(application, "childrenInNeedOfCare", "whoNeedsChildCare");

        AtomicInteger i = new AtomicInteger(0);

        return childrenNeedChildcare.stream()
                .map(fullName -> new ApplicationInput("childNeedsChildcare", "fullName",
                        List.of(fullName), ApplicationInputType.SINGLE_VALUE, i.getAndIncrement())).collect(Collectors.toList());
    }
}
