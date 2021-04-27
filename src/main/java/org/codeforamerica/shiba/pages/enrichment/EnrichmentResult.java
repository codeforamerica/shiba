package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.pages.data.InputData;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class EnrichmentResult extends HashMap<String, InputData> {
    @Serial
    private static final long serialVersionUID = -2222210916080608467L;

    public EnrichmentResult() {
    }

    public EnrichmentResult(Map<String, InputData> inputDataMap) {
        super(inputDataMap);
    }
}
