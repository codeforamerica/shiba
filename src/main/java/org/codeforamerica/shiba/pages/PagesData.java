package org.codeforamerica.shiba.pages;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Optional;

import static org.codeforamerica.shiba.pages.FormData.datasourceInputDataCreator;
import static org.codeforamerica.shiba.pages.FormData.literalInputDataCreator;

@EqualsAndHashCode(callSuper = true)
@Data
public class PagesData extends HashMap<String, FormData> {
    public FormData getPage(String pageName) {
        return get(pageName);
    }

    public FormData getPageOrDefault(String pageName, PageConfiguration pageConfiguration) {
        FormData defaultFormData = Optional.ofNullable(pageConfiguration.getDatasources())
                .map(datasource -> FormData.initialize(pageConfiguration, datasourceInputDataCreator(datasource, this)))
                .orElse(FormData.initialize(pageConfiguration, literalInputDataCreator()));

        return this.getOrDefault(pageName, defaultFormData);
    }

    public void putPage(String pageName, FormData formData) {
        this.put(pageName, formData);
    }
}
