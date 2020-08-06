package org.codeforamerica.shiba.pages.data;

import java.util.HashMap;

public class Subworkflows extends HashMap<String, Subworkflow> {

    public void addIteration(String groupName, PagesData subflowIteration) {
        Subworkflow subworkflow = this.getOrDefault(groupName, new Subworkflow());
        subworkflow.add(subflowIteration);
        this.put(groupName, subworkflow);
    }
}
