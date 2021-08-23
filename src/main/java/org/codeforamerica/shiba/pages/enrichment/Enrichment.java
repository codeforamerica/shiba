package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.pages.data.PagesData;

public interface Enrichment {

  EnrichmentResult process(PagesData pagesData);
}
