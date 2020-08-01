package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class NonPagesDataMapper implements ApplicationInputsMapper {
    @Override
    public List<ApplicationInput> map(ApplicationData data) {
        return List.of(new ApplicationInput("nonPagesData", "submissionTime", List.of(data.getSubmissionTime()), SINGLE_VALUE));
    }
}
