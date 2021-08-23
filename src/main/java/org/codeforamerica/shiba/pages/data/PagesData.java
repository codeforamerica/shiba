package org.codeforamerica.shiba.pages.data;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.pages.config.OptionsWithDataSourceTemplate.createOptionsWithDataSourceTemplate;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.ConditionalValue;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.config.FormInput;
import org.codeforamerica.shiba.pages.config.FormInputTemplate;
import org.codeforamerica.shiba.pages.config.PageConfiguration;
import org.codeforamerica.shiba.pages.config.PageDatasource;
import org.codeforamerica.shiba.pages.config.PageTemplate;
import org.codeforamerica.shiba.pages.config.PageWorkflowConfiguration;
import org.codeforamerica.shiba.pages.config.Value;

@EqualsAndHashCode(callSuper = true)
@Data
public class PagesData extends HashMap<String, PageData> {

  @Serial
  private static final long serialVersionUID = 5350174349257543992L;

  public PagesData() {
    super();
  }

  public PagesData(Map<String, PageData> map) {
    super(map);
  }

  public PageData getPage(String pageName) {
    return get(pageName);
  }

  public PageData getPageDataOrDefault(String pageName, PageConfiguration pageConfiguration) {
    PageData defaultPageData = PageData.initialize(pageConfiguration);

    return this.getOrDefault(pageName, defaultPageData);
  }

  public void putPage(String pageName, PageData pageData) {
    this.put(pageName, pageData);
  }

  public boolean satisfies(Condition condition) {
    if (condition.getConditions() != null) {
      Stream<Condition> conditionStream = condition.getConditions().stream();
      return switch (condition.getLogicalOperator()) {
        case AND -> conditionStream.allMatch(this::satisfies);
        case OR -> conditionStream.anyMatch(this::satisfies);
      };
    }

    PageData pageData = get(condition.getPageName());
    return condition.matches(pageData, this);
  }

  public DatasourcePages getDatasourcePagesBy(List<PageDatasource> datasources) {
    return new DatasourcePages(new PagesData(datasources.stream()
        .filter(datasource -> datasource.getPageName() != null)
        .map(datasource -> Map.entry(
            datasource.getPageName(),
            getOrDefault(datasource.getPageName(), new PageData())))
        .collect(toMap(Entry::getKey, Entry::getValue))));
  }

  public DatasourcePages getDatasourceGroupBy(List<PageDatasource> datasources,
      Subworkflows subworkflows) {
    Map<String, PageData> pages = new HashMap<>();
    datasources.stream()
        .filter(datasource -> datasource.getGroupName() != null && subworkflows
            .containsKey(datasource.getGroupName()))
        .forEach(datasource -> {
          PageData value = new PageData();
          subworkflows.get(datasource.getGroupName()).stream()
              .map(iteration -> iteration.getPagesData().getPage(datasource.getPageName()))
              .forEach(value::mergeInputDataValues);
          pages.put(datasource.getPageName(), value);
        });
    return new DatasourcePages(new PagesData(pages));
  }

  public List<String> safeGetPageInputValue(String pageName, String inputName) {
    return Optional.ofNullable(get(pageName))
        .flatMap(pageData -> Optional.ofNullable(pageData.get(inputName)))
        .map(InputData::getValue)
        .orElse(List.of());
  }

  /**
   * Get the first element in the inputData values for the given pageName and inputName or null if
   * it doesn't exist.
   *
   * @param pageName  page that the element is stored on
   * @param inputName input name of the element
   * @return first element stored at that pageName > inputName or null if it doesn't exist
   */
  public String getPageInputFirstValue(String pageName, String inputName) {
    PageData pageData = get(pageName);
    if (pageData != null) {
      InputData inputData = pageData.get(inputName);
      if (inputData != null && !inputData.getValue().isEmpty()) {
        return inputData.getValue(0);
      }
    }
    return null;
  }

  /**
   * Defaults to {@code value.getDefaultValue()} if all {@code value.getConditionalValues()} and
   * flags evaluate to "false".
   */
  private String resolve(FeatureFlagConfiguration featureFlags,
      PageWorkflowConfiguration pageWorkflowConfiguration, Value value) {
    if (value == null) {
      return "";
    }
    return value.getConditionalValues().stream()
        .filter(conditionalValue -> {
          // Check flag
          String flag = conditionalValue.getFlag();
          if (flag != null && featureFlags.get(flag).isOff()) {
            return false;
          }

          // Check condition
          Condition condition = conditionalValue.getCondition();
          if (condition == null) {
            return true;
          }
          Objects.requireNonNull(pageWorkflowConfiguration.getDatasources(),
              "Configuration mismatch! Conditional value cannot be evaluated without a datasource.");
          DatasourcePages datasourcePages = this
              .getDatasourcePagesBy(pageWorkflowConfiguration.getDatasources());
          return datasourcePages.satisfies(condition);
        })
        .findFirst()
        .map(ConditionalValue::getValue)
        .orElse(value.getDefaultValue());
  }

  public PageTemplate evaluate(FeatureFlagConfiguration featureFlags,
      PageWorkflowConfiguration pageWorkflowConfiguration, ApplicationData applicationData) {
    PageConfiguration pageConfiguration = pageWorkflowConfiguration.getPageConfiguration();
    DatasourcePages datasourcePages = this
        .getDatasourcePagesBy(pageWorkflowConfiguration.getDatasources());

    return new PageTemplate(
        pageConfiguration.getInputs().stream()
            .filter(input -> Optional.ofNullable(input.getCondition())
                .map(datasourcePages::satisfies)
                .orElse(true))
            .map(formInput -> convert(pageConfiguration.getName(), formInput, applicationData))
            .collect(Collectors.toList()),
        pageConfiguration.getName(),
        resolve(featureFlags, pageWorkflowConfiguration, pageConfiguration.getPageTitle()),
        resolve(featureFlags, pageWorkflowConfiguration, pageConfiguration.getHeaderKey()),
        resolve(featureFlags, pageWorkflowConfiguration,
            pageConfiguration.getHeaderHelpMessageKey()),
        pageConfiguration.getPrimaryButtonTextKey(),
        resolve(featureFlags, pageWorkflowConfiguration, pageConfiguration.getSubtleLinkTextKey()),
        pageWorkflowConfiguration.getSubtleLinkTargetPage(),
        pageConfiguration.getHasPrimaryButton(),
        pageConfiguration.getContextFragment(),
        pageConfiguration.getAlertBox()
    );
  }

  private FormInputTemplate convert(String pageName, FormInput formInput,
      ApplicationData applicationData) {
    List<String> errorMessageKeys = Optional.ofNullable(this.getPage(pageName))
        .map(pageData -> pageData.get(formInput.getName()).errorMessageKeys(pageData))
        .orElse(List.of());

    return new FormInputTemplate(
        formInput.getType(),
        formInput.getName(),
        formInput.getCustomInputFragment(),
        formInput.getPromptMessage(),
        formInput.getHelpMessageKey(),
        formInput.getPlaceholder(),
        errorMessageKeys,
        createOptionsWithDataSourceTemplate(formInput, applicationData),
        formInput.getFollowUps().stream()
            .map(followup -> convert(pageName, followup, applicationData))
            .collect(Collectors.toList()),
        formInput.getFollowUpValues(),
        formInput.getReadOnly(),
        formInput.getDefaultValue(),
        formInput.getMax(),
        formInput.getMin(),
        formInput.getDatasources()
    );
  }
}
