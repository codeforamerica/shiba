package org.codeforamerica.shiba.pages.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.DatasourcePages;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.Subworkflows;

/**
 * Trying to document what this interface does.
 * This is not a functional interface because there is no unimplented method.
 * Java allows interfaces such as this to have static implemented methods.
 *
 */
public interface OptionsWithDataSourceTemplate {

	/**
	 * Returns either a {@link ReferenceOptionsTemplate} if there are no selectable {@link Options}
	 *  or a {@link SelectableOptionsTemplate} if selectableOptions do exist, as configured in pages-config.yaml.
	 * @param formInput
	 * @param applicationData
	 * @return Object that extends this interface
	 */
  static OptionsWithDataSourceTemplate createOptionsWithDataSourceTemplate(FormInput formInput,
      ApplicationData applicationData) {
    if (formInput.getOptions() != null) {
      if (formInput.getOptions().getSelectableOptions().isEmpty()) {
        ReferenceOptionsTemplate optionsTemplate = new ReferenceOptionsTemplate();

        Subworkflows subworkflows = applicationData
            .getSubworkflowsForPageDatasources(formInput.getOptions().getDatasources());
        optionsTemplate.setSubworkflows(subworkflows);

        DatasourcePages datasources = applicationData.getPagesData()
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
    List<Option> limitedOptions = formInput.getOptions().getSelectableOptions().stream()
        .filter(Option::isLimitSelection).toList();

    List<Option> conditionalOptions = formInput.getOptions().getSelectableOptions().stream()
        .filter(option -> option.getCondition() != null).toList();

    List<String> valuesToBeRemoved = new ArrayList<>();

    applicationData.getPagesData().getDatasourcePagesBy(formInput.getOptions().getDatasources())
        .forEach((pageName, pageData) -> {
          conditionalOptions.forEach(option -> {
            pageData.forEach((inputName, inputData) -> {
              if (!satisfies(option, pageData)) {
                valuesToBeRemoved.add(option.getValue());
              }
            });
          });
        });

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
    List<Option> unmodifiedSelectOptions =
        Collections.unmodifiableList(formInput.getOptions().getSelectableOptions());
    List<String> selectableOptionValues = selectableOptions.stream().map(Option::getValue).toList();

    valuesToBeRemoved.forEach(value -> {
      selectableOptions.remove(unmodifiedSelectOptions.get(selectableOptionValues.indexOf(value)));
    });
    SelectableOptionsTemplate optionsTemplate = new SelectableOptionsTemplate();
    optionsTemplate.setSelectableOptions(selectableOptions);
    return optionsTemplate;
  }


  private static boolean satisfies(Option option, PageData pageData) {
    var condition = option.getCondition();
    if (condition.getConditions() != null) {
      Stream<Condition> conditionStream = condition.getConditions().stream();
      return switch (condition.getLogicalOperator()) {
        case AND -> conditionStream.allMatch(cond -> satisfies(pageData, cond));
        case OR -> conditionStream.anyMatch(cond -> satisfies(pageData, cond));
      };
    }
    return condition.satisfies(pageData);
  }

  private static boolean satisfies(PageData pageData, Condition cond) {
    return pageData != null && !pageData.isEmpty()
        && cond.getMatcher().matches(
            pageData.get(cond.getInput()).getValue(),
            cond.getValue());
  }
  
}

