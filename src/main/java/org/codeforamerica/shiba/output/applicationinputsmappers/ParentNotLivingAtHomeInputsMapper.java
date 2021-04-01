package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.*;
import org.codeforamerica.shiba.pages.data.PageData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ParentNotLivingAtHomeInputsMapper implements ApplicationInputsMapper {

    @Override
    public List<ApplicationInput> map(Application application, Document document, Recipient recipient, SubworkflowIterationScopeTracker scopeTracker) {
        Map<String, String> idToChild = application.getApplicationData().getPagesData()
                .safeGetPageInputValue("childrenInNeedOfCare", "whoNeedsChildCare").stream()
                .collect(Collectors.toMap(FullNameFormatter::getId, FullNameFormatter::format));

        PageData pageData = application.getApplicationData().getPageData("parentNotAtHomeNames");

        if (pageData == null) {
            return List.of();
        }

        List<String> parentNames = pageData.get("whatAreTheParentsNames").getValue();
        List<String> childIds = pageData.get("childIdMap").getValue();

        List<ApplicationInput> result = new ArrayList<>();
        for (int i = 0; i < childIds.size(); i++) {
            String parentName = parentNames.get(i);
            String childId = childIds.get(i);

            result.add(new ApplicationInput(
                    "custodyArrangement",
                    "parentNotAtHomeName",
                    List.of(parentName),
                    ApplicationInputType.SINGLE_VALUE, i));
            result.add(new ApplicationInput(
                    "custodyArrangement",
                    "childFullName",
                    List.of(idToChild.get(childId)),
                    ApplicationInputType.SINGLE_VALUE, i));
        }
        return result;
    }
}
