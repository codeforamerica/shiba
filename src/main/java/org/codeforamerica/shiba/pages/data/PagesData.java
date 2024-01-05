package org.codeforamerica.shiba.pages.data;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.pages.config.OptionsWithDataSourceTemplate.createOptionsWithDataSourceTemplate;

import java.io.Serial;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.*;

/**
 * PagesData extends HashMap&lt;String, PageData&gt; 
 *
 */
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

  /**
   * PagesData satisfies method checks if condition contains multiple conditions,
   * which then uses allMatch for AND logicalOperator, or anyMatch for OR logicalOperator.</br>
   * If there are no multiple conditions, it checks if the single condition matches the pageData.</br>
   * This method recursivly calls itself.
   * @param condition
   * @return Boolean
   */
  public boolean satisfies(Condition condition) {
    if (condition.getConditions() != null) {
      Stream<Condition> conditionStream = condition.getConditions().stream();
      return switch (condition.getLogicalOperator()) {
        case AND -> conditionStream.allMatch(this::satisfies);
        case OR -> conditionStream.anyMatch(this::satisfies);
      };
    }

    PageData pageData = get(condition.getPageName()); // this can't handle groups
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
    return new DatasourcePages(pages);
  }

  public List<String> safeGetPageInputValue(String pageName, String inputName) {
    return Optional.ofNullable(get(pageName))
        .map(pageData -> pageData.get(inputName))
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
   * Figure out text based on conditional values in the page workflow configuration
   * <p>
   * Defaults to {@code value.getDefaultValue()} if all {@code value.getConditionalValues()} and
   * flags evaluate to "false".
   */
  private String resolve(FeatureFlagConfiguration featureFlags,
      PageWorkflowConfiguration pageWorkflowConfiguration,
      Value value) {
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

  /**
   * Evaluate this PagesData object for display on the web page.
   * Inputs to be displayed are determined by any conditionals that are applied to them.
   * @param featureFlags
   * @param pageWorkflowConfiguration
   * @param applicationData
   * @return
   */
  public PageTemplate evaluate(FeatureFlagConfiguration featureFlags,
      PageWorkflowConfiguration pageWorkflowConfiguration, ApplicationData applicationData) {
    PageConfiguration pageConfiguration = pageWorkflowConfiguration.getPageConfiguration();
    DatasourcePages datasourcePages = this
        .getDatasourcePagesBy(pageWorkflowConfiguration.getDatasources());

    List<FormInputTemplate> inputs = null;

        inputs = pageConfiguration.getInputs().stream() //list of FormInputs
            .filter(input ->
                Optional.ofNullable(input.getCondition()).map(datasourcePages::satisfies).orElse(true))
            .map(formInput -> convert(pageConfiguration.getName(), formInput, applicationData))
            .collect(Collectors.toList());
 
    return new PageTemplate(
        inputs,
        pageConfiguration.getName(),
        resolve(featureFlags, pageWorkflowConfiguration, pageConfiguration.getPageTitle()),
        resolve(featureFlags, pageWorkflowConfiguration, pageConfiguration.getHeaderKey()),
        resolve(featureFlags, pageWorkflowConfiguration,
            pageConfiguration.getHeaderHelpMessageKey()),
        pageConfiguration.getPrimaryButtonTextKey(),
        resolve(featureFlags, pageWorkflowConfiguration, pageConfiguration.getSubtleLinkTextKey()),
        pageWorkflowConfiguration.getSubtleLinkTargetPage(),
        resolve(featureFlags, pageWorkflowConfiguration, pageConfiguration.getCardFooterTextKey()),
        pageConfiguration.getHasPrimaryButton(),
        pageConfiguration.getExcludeGoBack(),
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
        formInput.getDatasources(),
        formInput.getCustomFollowUps(),
        formInput.getInputPostfix(),
        formInput.getHelpMessageKeyBelow(),
        formInput.getNoticeMessage(),
        formInput.getValidationIcon()
    );
  }
}
