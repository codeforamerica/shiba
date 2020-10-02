package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;

public interface Enrichment {
    EnrichmentResult process(ApplicationData applicationData);
}
