package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.ApplicationData;

public abstract class ApplicationDataParser<T> {
    protected ParsingConfiguration parsingConfiguration;

    public ApplicationDataParser(ParsingConfiguration parsingConfiguration) {
        this.parsingConfiguration = parsingConfiguration;
    }

    public abstract T parse(ApplicationData applicationData);

    protected static Money getMoney(ApplicationData applicationData, PageInputCoordinates pageInputCoordinates) {
        try {
            return new Money(Double.parseDouble(applicationData.getValue(pageInputCoordinates).replace(",","")));
        } catch (NumberFormatException e) {
            return new Money(Double.parseDouble(pageInputCoordinates.getDefaultValue().replace(",","")));
        }
    }
}
