package org.codeforamerica.shiba.pages.data;

import org.codeforamerica.shiba.inputconditions.Condition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasourcePages extends HashMap<String, InputDataMap> {
    public DatasourcePages(Map<String, InputDataMap> pages) {
        super(pages);
    }

    public Boolean satisfies(Condition condition) {
        List<String> inputValue = this.get(condition.getPageName()).get(condition.getInput()).getValue();
        return condition.matches(inputValue);
    }
}
