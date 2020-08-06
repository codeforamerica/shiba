package org.codeforamerica.shiba.pages.data;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ApplicationData {
    private PagesData pagesData = new PagesData();
    private Subworkflows subworkflows = new Subworkflows();
    private String submissionTime;
    private Map<String, PagesData> incompleteIterations = new HashMap<>();

    public void clear() {
        this.pagesData.clear();
        this.submissionTime = null;
        this.subworkflows.clear();
    }

    public boolean isSubmitted() {
        return this.submissionTime != null;
    }

    public PageData getInputDataMap(String pageName) {
        return this.pagesData.getPage(pageName);
    }

}
