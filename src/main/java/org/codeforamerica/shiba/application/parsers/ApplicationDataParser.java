package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.Money;
import org.codeforamerica.shiba.pages.data.ApplicationData;

import java.util.OptionalInt;

public abstract class ApplicationDataParser<T> {
    protected ParsingConfiguration parsingConfiguration;

    public ApplicationDataParser(ParsingConfiguration parsingConfiguration) {
        this.parsingConfiguration = parsingConfiguration;
    }

    public abstract T parse(ApplicationData applicationData);

    protected static Integer getMoneyOrDefault(ApplicationData applicationData, PageInputCoordinates pageInputCoordinates) {
        OptionalInt ret = Money.parse(applicationData.getValue(pageInputCoordinates));
        if (ret.isEmpty()) {
            return Money.parse(pageInputCoordinates.getDefaultValue()).orElseThrow(() ->
                    new RuntimeException("Parsing configuration is missing defaultValue for page: %s input: %s".formatted(pageInputCoordinates.getPageName(), pageInputCoordinates.getInputName())));
        }

        return ret.getAsInt();
    }
}
