package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

@Component
public class StudentFullNameInputsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    List<String> students = getListOfSelectedFullNames(application, "whoIsGoingToSchool",
        "whoIsGoingToSchool");
    List<String> children = getListOfSelectedFullNames(application, "childrenInNeedOfCare",
        "whoNeedsChildCare");

    students.retainAll(children);
    AtomicInteger i = new AtomicInteger(0);
    return students.stream()
        .map(fullName -> new ApplicationInput("whoIsGoingToSchool", "fullName",
            List.of(fullName), ApplicationInputType.SINGLE_VALUE, i.getAndIncrement()))
        .collect(Collectors.toList());
  }
}
