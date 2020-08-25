package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProgramsListMapper implements ApplicationInputsMapper {
    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        List<String> programs = data.getPagesData()
                .getPage("choosePrograms")
                .get("programs")
                .getValue();
        return List.of(new ApplicationInput(
                "coverPage",
                "programs",
                List.of(String.join(", ", programs)),
                ApplicationInputType.SINGLE_VALUE
        ));
    }
}
