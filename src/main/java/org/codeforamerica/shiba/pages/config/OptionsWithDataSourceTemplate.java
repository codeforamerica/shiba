package org.codeforamerica.shiba.pages.config;

import java.util.ArrayList;
import java.util.List;
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
        return removeLimitedOptions(applicationData, formInput);
      }
    } else {
      return null;
    }
  }

  private static SelectableOptionsTemplate removeLimitedOptions(ApplicationData applicationData,
      FormInput formInput) {
    List<Option> limitedOptions = formInput.getOptions().getSelectableOptions().stream().filter(
        Option::isLimitSelection).toList();

    List<String> valuesToBeRemoved = new ArrayList<>();

    applicationData.getSubworkflows().forEach((groupName, subworkflow) -> {
      subworkflow.forEach(iteration -> {
        iteration.getPagesData().forEach((pageName, pageData) -> {
          limitedOptions.forEach(option -> {
            pageData.forEach((inputName, inputData) -> {
              if (inputData.getValue().contains(option.getValue())) {
                valuesToBeRemoved.add(option.getValue());
              }
            });
          });
        });
      });
    });

    List<Option> selectableOptions = new ArrayList<>(formInput.getOptions().getSelectableOptions());
    List<String> selectableOptionValues = selectableOptions.stream().map(Option::getValue).toList();
    valuesToBeRemoved.forEach(value -> {
      selectableOptions.remove(selectableOptionValues.indexOf(value));
    });

    SelectableOptionsTemplate optionsTemplate = new SelectableOptionsTemplate();
    optionsTemplate.setSelectableOptions(selectableOptions);
    return optionsTemplate;
  }
}
