package org.codeforamerica.shiba.research;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ResearchDataRepositoryBackfillController {

    private final ResearchDataRepository researchDataRepository;
    private final ApplicationRepository applicationRepository;
    private final ResearchDataParser researchDataParser;

    public ResearchDataRepositoryBackfillController(ResearchDataRepository researchDataRepository, ApplicationRepository applicationRepository, ResearchDataParser researchDataParser) {
        this.researchDataRepository = researchDataRepository;
        this.applicationRepository = applicationRepository;
        this.researchDataParser = researchDataParser;
    }

    @GetMapping("/researchdb/backfill")
    ModelAndView getPage() {
        List<String> existingResearchData = researchDataRepository.findAllIds().stream().map(ResearchData::getApplicationId).collect(Collectors.toList());
        List<Application> existingApplicationData = applicationRepository.findAll();

        existingApplicationData.stream()
                .filter(application -> application.getId() != null && !existingResearchData.contains(application.getId()))
                .forEach(application -> {
                    ApplicationData applicationData = application.getApplicationData();
                    if (!application.getId().equals(application.getApplicationData().getId())) {
                        applicationData.setId(application.getId());
                        applicationRepository.save(application);
                    }
                    researchDataRepository.save(researchDataParser.parse(applicationData));
                });

        return new ModelAndView("backfill", new HashMap<>(Map.of()));
    }
}