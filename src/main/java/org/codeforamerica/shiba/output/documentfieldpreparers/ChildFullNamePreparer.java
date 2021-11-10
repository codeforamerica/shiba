package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.output.FullNameFormatter.getListOfSelectedFullNames;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.DocumentFieldType;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

@Component
public class ChildFullNamePreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {

    List<String> childrenNeedChildcare = getListOfSelectedFullNames(application,
        "childrenInNeedOfCare", "whoNeedsChildCare");

    AtomicInteger i = new AtomicInteger(0);

    return childrenNeedChildcare.stream()
        .map(fullName -> new DocumentField("childNeedsChildcare", "fullName",
            List.of(fullName), DocumentFieldType.SINGLE_VALUE, i.getAndIncrement()))
        .collect(Collectors.toList());
  }
}
