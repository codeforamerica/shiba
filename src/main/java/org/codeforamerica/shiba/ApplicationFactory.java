package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;

@Component
public class ApplicationFactory {
    private final ApplicationRepository applicationRepository;
    private final Clock clock;


    public ApplicationFactory(ApplicationRepository applicationRepository,
                              Clock clock) {
        this.applicationRepository = applicationRepository;
        this.clock = clock;
    }

    public Application newApplication(ApplicationData applicationData) {
        ApplicationData copy = new ApplicationData();
        copy.setPagesData(applicationData.getPagesData());
        copy.setSubworkflows(applicationData.getSubworkflows());
        copy.setIncompleteIterations(applicationData.getIncompleteIterations());
        return new Application(
                applicationRepository.getNextId(),
                ZonedDateTime.now(clock),
                copy);
    }
}
