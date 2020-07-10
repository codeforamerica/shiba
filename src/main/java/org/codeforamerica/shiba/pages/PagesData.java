package org.codeforamerica.shiba.pages;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.codeforamerica.shiba.pages.FormData.datasourceInputDataCreator;
import static org.codeforamerica.shiba.pages.FormData.literalInputDataCreator;

@Data
public class PagesData {
    private Map<String, FormData> data = new HashMap<>();
    private boolean submitted = false;

    public FormData getPage(String pageName) {
        return this.data.get(pageName);
    }

    public FormData getPageOrDefault(String pageName, PageConfiguration pageConfiguration) {
        FormData defaultFormData = Optional.ofNullable(pageConfiguration.getDatasources())
                .map(datasource -> FormData.initialize(pageConfiguration, datasourceInputDataCreator(datasource, this)))
                .orElse(FormData.initialize(pageConfiguration, literalInputDataCreator()));

        return this.data.getOrDefault(pageName, defaultFormData);
    }

    public void putPage(String pageName, FormData formData) {
        this.data.put(pageName, formData);
    }

    public void clear() {
        this.data.clear();
        this.submitted = false;
    }
}
