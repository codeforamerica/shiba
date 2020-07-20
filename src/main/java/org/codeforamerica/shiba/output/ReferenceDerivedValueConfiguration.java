package org.codeforamerica.shiba.output;

import lombok.Data;
import org.codeforamerica.shiba.pages.ApplicationData;

import java.util.List;

@Data
public class ReferenceDerivedValueConfiguration implements DerivedValueConfiguration {
    private String pageName;
    private String inputName;

    @Override
    public List<String> resolve(ApplicationData data) {
        return data.getInputDataMap(pageName)
                .get(inputName)
                .getValue();
    }
}
