package org.codeforamerica.shiba.pages.enrichment;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ZipcodeToCountyEnrichment implements Enrichment {
    private final Map<String, County> countyZipCodeMap;

    public ZipcodeToCountyEnrichment(Map<String, County> countyZipCodeMap) {
        this.countyZipCodeMap = countyZipCodeMap;
    }

    @Override
    public EnrichmentResult process(ApplicationData applicationData) {
        String zipcode = applicationData.getPagesData().getPageInputFirstValue("identifyZipcode", "zipCode");
        County county = countyZipCodeMap.get(zipcode);
        if (county == null) {
            county = County.Other;
        }

        return new EnrichmentResult(Map.of("mappedCounty", new InputData(List.of(county.name()))));
    }
}
