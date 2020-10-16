package org.codeforamerica.shiba.pages.data;

import java.util.HashMap;
import java.util.Map;

public class Subworkflows extends HashMap<String, Subworkflow> {

    public Subworkflows() {
        super();
    }

    public Subworkflows(Map<String, Subworkflow> subworkflows) {
        super(subworkflows);
    }

    public void addIteration(String groupName, PagesData subflowIteration) {
        Subworkflow subworkflow = this.getOrDefault(groupName, new Subworkflow());
        subworkflow.add(subflowIteration);
        this.put(groupName, subworkflow);
    }
}
