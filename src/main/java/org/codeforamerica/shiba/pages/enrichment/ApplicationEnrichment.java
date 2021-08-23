package org.codeforamerica.shiba.pages.enrichment;

import java.util.Map;
import org.springframework.stereotype.Component;

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
