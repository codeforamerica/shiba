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

    protected Money getMoney(String pageInput, PagesData pagesData) {
        PageInputCoordinates coordinates = parsingConfiguration.get(pageInput);
        try {
            String pageInputValue = pagesData.getPageInputFirstValue(coordinates.getPageName(), coordinates.getInputName());
            return Money.parse(Optional.ofNullable(pageInputValue).orElse(coordinates.getDefaultValue()));
        } catch (NumberFormatException e) {
            return Money.parse(coordinates.getDefaultValue());
        }
    }

    /**
     * Uses parsing configuration coordinates to get the value input.
     *
     * @param pageInput page input to get the coordinates
     * @param pagesData data from which to retrieve the value
     * @return value at the coordinates or a default value if it doesn't exist
     */
    public final String parseValue(String pageInput, PagesData pagesData) {
        return parseValue(parsingConfiguration.get(pageInput), pagesData);
    }

    public final String parseValue(PageInputCoordinates coordinates, PagesData pagesData) {
        return Optional.ofNullable(pagesData.getPageInputFirstValue(coordinates.getPageName(), coordinates.getInputName()))
                .orElse(coordinates.getDefaultValue());
    }
}
