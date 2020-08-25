package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Map;

@Component
public class ApplicationFactory {
    private final ApplicationRepository applicationRepository;
    private final Clock clock;
    private final Map<String, County> countyZipCode;


    public ApplicationFactory(ApplicationRepository applicationRepository,
                              Clock clock,
                              Map<String, County> countyZipCode) {
        this.applicationRepository = applicationRepository;
        this.clock = clock;
        this.countyZipCode = countyZipCode;
    }

    public Application newApplication(ApplicationData applicationData) {
        ApplicationData copy = new ApplicationData();
        copy.setPagesData(applicationData.getPagesData());
        copy.setSubworkflows(applicationData.getSubworkflows());
        copy.setIncompleteIterations(applicationData.getIncompleteIterations());
        String zipCode = copy.getPagesData().get("homeAddress").get("zipCode").getValue().get(0);
        return new Application(
                applicationRepository.getNextId(),
                ZonedDateTime.now(clock),
                copy,
                countyZipCode.getOrDefault(zipCode, County.OTHER)
        );
    }
}
