package org.codeforamerica.shiba;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ApplicationEnrichment {
    private final Map<String, Enrichment> enrichmentMap;

    public ApplicationEnrichment(Map<String, Enrichment> enrichmentMap) {
        this.enrichmentMap = enrichmentMap;
    }

    public Enrichment getEnrichment(String enrichment) {
        return this.enrichmentMap.get(enrichment);
    }
}
