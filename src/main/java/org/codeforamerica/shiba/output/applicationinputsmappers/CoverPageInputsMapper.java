package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class CoverPageInputsMapper implements ApplicationInputsMapper {
    private final Map<County, Map<Recipient, String>> countyInstructionsMapping;

    public CoverPageInputsMapper(Map<County, Map<Recipient, String>> countyInstructionsMapping) {
        this.countyInstructionsMapping = countyInstructionsMapping;
    }

    @Override
    public List<ApplicationInput> map(Application application, Recipient recipient) {
        ApplicationData data = application.getApplicationData();
        List<String> programs = data.getPagesData()
                .getPage("choosePrograms")
                .get("programs")
                .getValue();

        PageData personalInfo = application.getApplicationData().getInputDataMap("personalInfo");
        return List.of(
                new ApplicationInput(
                        "coverPage",
                        "programs",
                        List.of(String.join(", ", programs)),
                        SINGLE_VALUE),
                new ApplicationInput(
                        "coverPage",
                        "countyInstructions",
                        List.of(countyInstructionsMapping.get(application.getCounty()).get(recipient)),
                        SINGLE_VALUE),
                new ApplicationInput(
                        "coverPage",
                        "fullName",
                        List.of(String.format("%s %s",
                                String.join("", personalInfo.get("firstName").getValue()),
                                String.join("", personalInfo.get("lastName").getValue())
                        )),
                        SINGLE_VALUE
                ));
    }
}
