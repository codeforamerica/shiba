package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;

import java.util.Optional;

public abstract class AbstractApplicationDataParser<T> {
    protected ParsingConfiguration parsingConfiguration;

    public abstract T parse(ApplicationData applicationData);

    protected static double getDouble(PagesData pagesData, PageInputCoordinates pageInputCoordinates) {
        try {
            return Double.parseDouble(
                    Optional.ofNullable(pagesData.get(pageInputCoordinates.getPageName()))
                            .map(inputDataMap -> inputDataMap.get(pageInputCoordinates.getInputName()).getValue().get(0))
                            .orElse(pageInputCoordinates.getDefaultValue())
            );
        } catch (NumberFormatException e) {
            return Double.parseDouble(pageInputCoordinates.getDefaultValue());
        }
    }
}
