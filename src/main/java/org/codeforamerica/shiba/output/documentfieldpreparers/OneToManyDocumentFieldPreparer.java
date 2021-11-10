package org.codeforamerica.shiba.output.documentfieldpreparers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.DocumentFieldType.ENUMERATED_SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.DocumentField;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;

/**
 * Handle checkbox selections that correspond to individual inputs on the presented application.
 */
public abstract class OneToManyDocumentFieldPreparer implements DocumentFieldPreparer {

  @Override
  public List<DocumentField> prepareDocumentFields(Application application, Document document,
      Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    return map(application.getApplicationData().getPagesData());
  }

  protected abstract OneToManyParams getParams();

  protected List<DocumentField> map(PagesData pagesData) {
    var params = getParams();
    return createApplicationInputs(params, pagesData);
  }

  private List<DocumentField> createApplicationInputs(OneToManyParams params,
      PagesData pagesData) {
    String pageName = params.pageName();
    List<DocumentField> results = new ArrayList<>();
    if (pagesData.containsKey(pageName)) {
      List<String> selectedValues = getValues(pagesData, params.field());
      params.yesNoOptions().stream()
          .map(option -> new DocumentField(pageName,
              option,
              String.valueOf(selectedValues.contains(option)),
              ENUMERATED_SINGLE_VALUE))
          .forEach(results::add);
    }
    return results;
  }

  protected static record OneToManyParams(String pageName, Field field, List<String> yesNoOptions) {

  }
}
