package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getGroup;
import static org.codeforamerica.shiba.output.DocumentFieldType.SINGLE_VALUE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Group;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.Iteration;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.codeforamerica.shiba.pages.data.Subworkflow;

/**
 * Create indexed document fields for a given subworkflow and scope.
 */
public abstract class SubworkflowScopePreparer implements DocumentFieldPreparer {

  protected abstract ScopedParams getParams(Document document, Application application);

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient _recipient) {
    return prepareDocumentFields(application, document);
  }

  protected List<DocumentField> prepareDocumentFields(Application application, Document document) {
    ScopedParams params = getParams(document, application);

    Subworkflow subworkflow = getGroup(application.getApplicationData(), params.group());
    if (subworkflow == null || subworkflow.isEmpty()) {
      return Collections.emptyList();
    }

    List<DocumentField> results = new ArrayList<>();

    int index = 0;
    for (Iteration iteration : subworkflow) {
      if (params.scope().test(iteration.getPagesData())) {
        String key = "";
        Optional<Entry<String, PageData>> y = iteration.getPagesData().entrySet().stream()
            .filter(x -> x.getKey().equals("payPeriod")).findFirst() ;
        if(y.isPresent()) {
          key = "incomePerPayPeriod_" + y.get().getValue().get("payPeriod").getValue(0);
        }
        for (Entry<String, PageData> pageDataEntry : iteration.getPagesData().entrySet()) {
          String groupName = params.prefix() + pageDataEntry.getKey();
          for (Entry<String, InputData> inputDataEntry : pageDataEntry.getValue().entrySet()) {
            results.add(new DocumentField(
                groupName,
                inputDataEntry.getKey(),
                inputDataEntry.getValue().getValue(),
                SINGLE_VALUE,
                index));
            if(inputDataEntry.getKey().equals("incomePerPayPeriod") && !key.isEmpty()) {
              results.add(new DocumentField(
                  groupName,
                  key,
                  inputDataEntry.getValue().getValue(),
                  SINGLE_VALUE,
                  index));
            }
          }
        }
        index++;
      }
    }

    return results;
  }

  protected static record ScopedParams(Predicate<PagesData> scope, Group group, String prefix) {

  }
}
