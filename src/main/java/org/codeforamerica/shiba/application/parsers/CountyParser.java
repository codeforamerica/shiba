package org.codeforamerica.shiba.application.parsers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CountyParser extends ApplicationDataParser<County> {
    public CountyParser(ParsingConfiguration parsingConfiguration) {
        super(parsingConfiguration);
    }

    @Override
    public County parse(ApplicationData applicationData) {
        PageInputCoordinates pageInputCoordinates = parsingConfiguration.get("homeAddress").getPageInputs().get("county");
        String countyName = Optional.ofNullable(applicationData.getPagesData().getPage(pageInputCoordinates.getPageName()))
                .flatMap(pageData -> Optional.ofNullable(pageData.get(pageInputCoordinates.getInputName())))
                .map(inputData -> inputData.getValue().get(0))
                .orElse(pageInputCoordinates.getDefaultValue());
        return County.valueOf(countyName);
    }
}
