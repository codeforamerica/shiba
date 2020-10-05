package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface Enrichment {
    EnrichmentResult process(ApplicationData applicationData);
}
