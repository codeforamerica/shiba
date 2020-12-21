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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class ChildFullNameInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {

        List<String> childrenNeedChildcare = Optional.ofNullable(application.getApplicationData().getPageData("childrenInNeedOfCare"))
                .map(pageData -> pageData.get("whoNeedsChildCare"))
                .map(InputData::getValue)
                .orElse(List.of(""))
                .stream().map(FullNameFormatter::format).collect(Collectors.toList());

        AtomicInteger i = new AtomicInteger(0);

        return childrenNeedChildcare.stream()
                .map(fullName -> new ApplicationInput("childNeedsChildcare", "fullName",
                       List.of(fullName), ApplicationInputType.SINGLE_VALUE, i.getAndIncrement())).collect(Collectors.toList());
    }
}
