package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.pages.data.InputData;

import java.util.HashMap;
import java.util.Map;

public class EnrichmentResult extends HashMap<String, InputData> {
    public EnrichmentResult() {
    }

    public EnrichmentResult(Map<String, InputData> inputDataMap) {
        super(inputDataMap);
    }
}
