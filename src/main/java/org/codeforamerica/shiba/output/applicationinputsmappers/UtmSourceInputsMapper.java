package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;

@Component
public class UtmSourceInputsMapper implements ApplicationInputsMapper {
    public static final String CHILDCARE_WAITING_LIST_UTM_SOURCE = "childcare_waiting_list";
    private static final Map<String, String> UTM_SOURCE_MAPPING = Map.of(
            CHILDCARE_WAITING_LIST_UTM_SOURCE, "FROM BSF WAITING LIST"
    );

    @Override
    public List<ApplicationInput> map(Application application, Recipient _recipient, SubworkflowIterationScopeTracker _scopeTracker) {

        String applicationUtmSource = (application.getApplicationData().getUtmSource() != null) ? application.getApplicationData().getUtmSource() : "";
        return List.of(
                new ApplicationInput("nonPagesData", "utmSource", List.of(UTM_SOURCE_MAPPING.getOrDefault(applicationUtmSource, "")), SINGLE_VALUE)
        );
    }
}
