package org.codeforamerica.shiba.pages.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.pages.config.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.codeforamerica.shiba.pages.config.OptionsWithDataSourceTemplate.createOptionsWithDataSourceTemplate;

@EqualsAndHashCode(callSuper = true)
@Data
public class PagesData extends HashMap<String, PageData> {
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

    public DatasourcePages getDatasourcePagesBy(List<PageDatasource> datasources) {
        return new DatasourcePages(datasources.stream()
                .filter(datasource -> datasource.getPageName() != null)
                .map(datasource -> Map.entry(
                        datasource.getPageName(),
                        getOrDefault(datasource.getPageName(), new PageData())))
                .collect(toMap(Entry::getKey, Entry::getValue)));
    }

    public DatasourcePages getDatasourceGroupBy(List<PageDatasource> datasources, Subworkflows subworkflows) {
        Map<String, PageData> pages = new HashMap<>();
        datasources.stream()
                .filter(datasource -> datasource.getGroupName() != null && subworkflows.containsKey(datasource.getGroupName()))
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
                .flatMap(pageData -> Optional.ofNullable(pageData.get(inputName)))
                .map(InputData::getValue)
                .orElse(List.of());
    }

    /**
     * Defaults to {@code value.getValue()} if all {@code value.getConditionalValues()} evaluate to "false".
     */
    private String resolve(PageWorkflowConfiguration pageWorkflowConfiguration, Value value) {
        if (value == null) {
            return "";
        }
        return value.getConditionalValues().stream()
                .filter(conditionalValue -> {
                    Objects.requireNonNull(pageWorkflowConfiguration.getDatasources(),
                            "Configuration mismatch! Conditional value cannot be evaluated without a datasource.");
                    DatasourcePages datasourcePages = this.getDatasourcePagesBy(pageWorkflowConfiguration.getDatasources());
                    if (conditionalValue.isCompositeCondition(conditionalValue.getCompositeCondition())) {
                        return datasourcePages.satisfies(conditionalValue.getCompositeCondition());
                    }
                    return datasourcePages.satisfies(conditionalValue.getCondition());
                })
                .findFirst()
                .map(ConditionalValue::getValue)
                .orElse(value.getValue());
    }

    public PageTemplate evaluate(PageWorkflowConfiguration pageWorkflowConfiguration, ApplicationData applicationData) {
        PageConfiguration pageConfiguration = pageWorkflowConfiguration.getPageConfiguration();
        DatasourcePages datasourcePages = this.getDatasourcePagesBy(pageWorkflowConfiguration.getDatasources());

        return new PageTemplate(
                pageConfiguration.getInputs().stream()
                        .filter(input -> Optional.ofNullable(input.getCondition())
                                .map(datasourcePages::satisfies)
                                .orElse(true))
                        .map(formInput -> convert(pageConfiguration.getName(), formInput, applicationData))
                        .collect(Collectors.toList()),
                pageConfiguration.getName(),
                resolve(pageWorkflowConfiguration, pageConfiguration.getPageTitle()),
                resolve(pageWorkflowConfiguration, pageConfiguration.getHeaderKey()),
                resolve(pageWorkflowConfiguration, pageConfiguration.getHeaderHelpMessageKey()),
                pageConfiguration.getPrimaryButtonTextKey(),
                resolve(pageWorkflowConfiguration, pageConfiguration.getSubtleLinkTextKey()),
                pageWorkflowConfiguration.getSubtleLinkTargetPage(),
                pageConfiguration.getHasPrimaryButton(),
                pageConfiguration.getContextFragment()
        );
    }

    private FormInputTemplate convert(String pageName, FormInput formInput, ApplicationData applicationData) {
        String errorMessageKey = Optional.ofNullable(this.getPage(pageName))
                .map(pageData -> pageData.get(formInput.getName()))
                .flatMap(InputData::errorMessageKey)
                .orElse("");

        return new FormInputTemplate(
                formInput.getType(),
                formInput.getName(),
                formInput.getCustomInputFragment(),
                formInput.getPromptMessage(),
                formInput.getHelpMessageKey(),
                errorMessageKey,
                createOptionsWithDataSourceTemplate(formInput, applicationData),
                formInput.getFollowUps().stream().map(followup -> convert(pageName, followup, applicationData)).collect(Collectors.toList()),
                formInput.getFollowUpValues(),
                formInput.getReadOnly(),
                formInput.getDefaultValue(),
                formInput.getMax(),
                formInput.getMin()
        );
    }
}
