package org.codeforamerica.shiba.research;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Component
public class SessionListener implements HttpSessionListener {

    private final ResearchDataRepository researchDataRepository;
    private final ResearchDataParser researchDataParser;

    public SessionListener(ResearchDataRepository researchDataRepository,
                           ResearchDataParser researchDataParser) {
        this.researchDataRepository = researchDataRepository;
        this.researchDataParser = researchDataParser;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        ApplicationData applicationData = ((ApplicationData) sessionEvent.getSession().getAttribute("scopedTarget.applicationData"));
        if (applicationData != null && !applicationData.getPagesData().isEmpty()) {
            researchDataRepository.save(researchDataParser.parse(applicationData));
        }

        // Maybe we could tell sentry the session ended here?
    }

}