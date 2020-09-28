package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.output.caf.PageInputCoordinates;
import org.codeforamerica.shiba.output.caf.ParsingConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;

public abstract class ApplicationDataParser<T> {
    protected ParsingConfiguration parsingConfiguration;

    public ApplicationDataParser(ParsingConfiguration parsingConfiguration) {
        this.parsingConfiguration = parsingConfiguration;
    }

    public abstract T parse(ApplicationData applicationData);

    protected static double getDouble(ApplicationData applicationData, PageInputCoordinates pageInputCoordinates) {
        try {
            return Double.parseDouble(applicationData.getValue(pageInputCoordinates));
        } catch (NumberFormatException e) {
            return Double.parseDouble(pageInputCoordinates.getDefaultValue());
        }
    }
}
