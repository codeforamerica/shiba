package org.codeforamerica.shiba.pages.enrichment;

import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PagesData;

import java.util.List;
import java.util.Map;

public abstract class DateOfBirthEnrichment implements Enrichment {

    @Override
    public EnrichmentResult process(PagesData pagesData) {
        String dobString = formatDateOfBirth(parseDateOfBirth(pagesData));
        return new EnrichmentResult(Map.of("dobAsDate", new InputData(List.of(dobString))));
    }

    protected abstract List<String> parseDateOfBirth(PagesData pagesData);

    /**
     * Adds '0' padding to single digit days and months in given birth date.
     * Ex. {"1", "2", "1999"} would return "01/02/1999"
     *
     * @param dateOfBirth date of birth as array list
     * @return formatted date of birth as String
     */
    private static String formatDateOfBirth(List<String> dateOfBirth) {
        return StringUtils.leftPad(dateOfBirth.get(0), 2, '0') + '/' +
                StringUtils.leftPad(dateOfBirth.get(1), 2, '0') + '/' +
                dateOfBirth.get(2);
    }
}
