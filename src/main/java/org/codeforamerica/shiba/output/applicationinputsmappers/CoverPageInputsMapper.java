package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CoverPageInputsMapper implements ApplicationInputsMapper {
    private final Map<County, String> countyInstructionsMapping;

    public CoverPageInputsMapper(Map<County, String> countyInstructionsMapping) {
        this.countyInstructionsMapping = countyInstructionsMapping;
    }

    @Override
    public List<ApplicationInput> map(Application application) {
        ApplicationData data = application.getApplicationData();
        List<String> programs = data.getPagesData()
                .getPage("choosePrograms")
                .get("programs")
                .getValue();

        return List.of(
                new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of(String.join(", ", programs)),
                        ApplicationInputType.SINGLE_VALUE),
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(countyInstructionsMapping.get(application.getCounty())),
                        ApplicationInputType.SINGLE_VALUE));
    }
}
