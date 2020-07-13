package org.codeforamerica.shiba.pages;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
public class PagesData extends HashMap<String, FormData> {
    public FormData getPage(String pageName) {
        return get(pageName);
    }

    public FormData getPageOrDefault(String pageName, PageConfiguration pageConfiguration) {
        FormData defaultFormData = FormData.initialize(pageConfiguration);

        return this.getOrDefault(pageName, defaultFormData);
    }

    public void putPage(String pageName, FormData formData) {
        this.put(pageName, formData);
    }
}
