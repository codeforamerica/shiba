package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PagesData;

import java.util.Optional;

public abstract class ApplicationDataParser<T> {
    protected ParsingConfiguration parsingConfiguration;

    public ApplicationDataParser(ParsingConfiguration parsingConfiguration) {
        this.parsingConfiguration = parsingConfiguration;
    }

    public abstract T parse(ApplicationData applicationData);

    protected static Money getMoney(ApplicationData applicationData, PageInputCoordinates pageInputCoordinates) {
        try {
            return Money.parse(parseValue(pageInputCoordinates, applicationData.getPagesData()));
        } catch (NumberFormatException e) {
            return Money.parse(pageInputCoordinates.getDefaultValue());
        }
    }

    public static String parseValue(PageInputCoordinates coordinates, PagesData pagesData) {
        return Optional.ofNullable(pagesData.getPageInputFirstValue(coordinates.getPageName(), coordinates.getInputName()))
                .orElse(coordinates.getDefaultValue());
    }
}
