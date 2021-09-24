package org.codeforamerica.shiba.output.applicationinputsmappers;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getValues;
import static org.codeforamerica.shiba.output.ApplicationInputType.ENUMERATED_SINGLE_VALUE;

import java.util.ArrayList;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.PagesData;

/**
 * Handle checkbox selections that correspond to individual inputs on the presented application.
 */
public abstract class OneToManyApplicationInputsMapper implements ApplicationInputsMapper {

  @Override
  public List<ApplicationInput> map(Application application, Document document, Recipient recipient,
      SubworkflowIterationScopeTracker scopeTracker) {
    return map(application.getApplicationData().getPagesData());
  }

  protected abstract OneToManyParams getParams();

  protected List<ApplicationInput> map(PagesData pagesData) {
    var params = getParams();
    return createApplicationInputs(params, pagesData);
  }

  private List<ApplicationInput> createApplicationInputs(OneToManyParams params,
      PagesData pagesData) {
    String pageName = params.getPageName();
    List<ApplicationInput> results = new ArrayList<>();
    if (pagesData.containsKey(pageName)) {
      List<String> selectedValues = getValues(pagesData, params.getField());
      params.getYesNoOptions().stream()
          .map(option -> new ApplicationInput(pageName,
              option,
              String.valueOf(selectedValues.contains(option)),
              ENUMERATED_SINGLE_VALUE))
          .forEach(results::add);
    }
    return results;
  }

}
