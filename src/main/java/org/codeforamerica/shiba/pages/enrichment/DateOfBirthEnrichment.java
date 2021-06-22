package org.codeforamerica.shiba.pages.enrichment;

import java.util.List;
import java.util.Map;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;

public abstract class DateOfBirthEnrichment implements Enrichment {

    @Override
    public EnrichmentResult process(ApplicationData applicationData) {
    	String dobString = String.join("/", parseDateOfBirth(applicationData));
        return new EnrichmentResult(Map.of("dobAsDate", new InputData(List.of(dobString))));
    }
    
    protected abstract List<String> parseDateOfBirth(ApplicationData applicationData); 
}
