package org.codeforamerica.shiba.pages.data;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.codeforamerica.shiba.inputconditions.Condition;

// Very similar to PagesData, with some subtle differences. Used for
// - Skip conditions, to hold the data for their datasources
// - Configuration, to represent options on follow ups and inputs, via OptionsWithDataSourceTemplate
// - PagesData#resolve, to create the PageTemplate (done in PagesController#getPage)
// - PagesData#evaluate
public class DatasourcePages extends HashMap<String, PageData> {

  @Serial
  private static final long serialVersionUID = 6366043143114427707L;

  public DatasourcePages(PagesData pagesData) {
    super(pagesData);
  }

  public DatasourcePages(Map<String, PageData> pagesData) {
    super(pagesData);
  }

  public Boolean satisfies(Condition condition) {
    if (condition.getConditions() != null) {
      Stream<Condition> conditionStream = condition.getConditions().stream();
      return switch (condition.getLogicalOperator()) {
        case AND -> conditionStream.allMatch(condition1 -> satisfies(condition1));
        case OR -> conditionStream.anyMatch(this::satisfies);
      };
    }

    PageData pageData = this.get(condition.getPageName());
    if (pageData == null || !pageData.containsKey(condition.getInput())) {
      return false; // The client didn't provide an answer, so this condition can't be satisfied.
    } else {
      return condition.matches(pageData, this);
    }
  }

  public DatasourcePages mergeDatasourcePages(DatasourcePages datasourcePages) {
    datasourcePages.forEach((key, value) -> {
      PageData current = this.get(key);
      if (current != null) {
        current.mergeInputDataValues(value);
      }
    });
    return this;
  }

  public String getPageInputFirstValue(String pageName, String inputName) {
    return new PagesData(this).getPageInputFirstValue(pageName, inputName);
  }
}
