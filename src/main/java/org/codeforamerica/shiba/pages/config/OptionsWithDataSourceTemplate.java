package org.codeforamerica.shiba.pages.config;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.DatasourcePages;
import org.codeforamerica.shiba.pages.data.Subworkflows;

public interface OptionsWithDataSourceTemplate {
    static OptionsWithDataSourceTemplate createOptionsWithDataSourceTemplate(
            FormInput formInput,
            ApplicationData applicationData
    ) {
        if (formInput.getOptions() != null) {
            if (formInput.getOptions().getSelectableOptions().isEmpty()) {
                ReferenceOptionsTemplate optionsTemplate = new ReferenceOptionsTemplate();

                Subworkflows subworkflows = applicationData
                        .getSubworkflowsForPageDatasources(formInput.getOptions().getDatasources());
                optionsTemplate.setSubworkflows(subworkflows);

                DatasourcePages datasources = applicationData
                        .getPagesData()
                        .getDatasourcePagesBy(formInput.getOptions().getDatasources());
                optionsTemplate.setDatasources(datasources);

                return optionsTemplate;
            } else {
                SelectableOptionsTemplate optionsTemplate = new SelectableOptionsTemplate();
                optionsTemplate.setSelectableOptions(formInput.getOptions().getSelectableOptions());
                return optionsTemplate;
            }
        } else {
            return null;
        }
    }
}
