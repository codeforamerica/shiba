package org.codeforamerica.shiba;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.codeforamerica.shiba.FormData.datasourceInputDataCreator;
import static org.codeforamerica.shiba.FormData.literalInputDataCreator;

@Data
public class PagesData {
    Map<String, FormData> data = new HashMap<>();

    public FormData getPage(String pageName) {
        return this.data.get(pageName);
    }

    public FormData getPageOrDefault(String pageName, PageConfiguration pageConfiguration) {
        FormData defaultFormData = Optional.ofNullable(pageConfiguration.getDatasource())
                .map(datasource -> FormData.initialize(pageConfiguration, datasourceInputDataCreator(datasource, this)))
                .orElse(FormData.initialize(pageConfiguration, literalInputDataCreator()));

        return this.data.getOrDefault(pageName, defaultFormData);
    }

    public void putPage(String pageName, FormData formData) {
        this.data.put(pageName, formData);
    }
}
