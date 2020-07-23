package org.codeforamerica.shiba.pages.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.codeforamerica.shiba.inputconditions.Condition;
import org.codeforamerica.shiba.pages.config.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@EqualsAndHashCode(callSuper = true)
@Data
public class PagesData extends HashMap<String, InputDataMap> {
    public InputDataMap getPage(String pageName) {
        return get(pageName);
    }

    public InputDataMap getPageDataOrDefault(String pageName, PageConfiguration pageConfiguration) {
        InputDataMap defaultInputDataMap = InputDataMap.initialize(pageConfiguration);

        return this.getOrDefault(pageName, defaultInputDataMap);
    }

    public void putPage(String pageName, InputDataMap inputDataMap) {
        this.put(pageName, inputDataMap);
    }

    InputDataMap getInputDataMapBy(PageDatasource datasource) {
        InputDataMap inputDataMap = get(datasource.getPageName());
        if (inputDataMap == null) {
            return new InputDataMap();
        }
        if (datasource.getInputs().isEmpty()) {
            return inputDataMap;
        }
        return new InputDataMap(datasource.getInputs().stream()
                .map(inputDatasource -> Map.entry(inputDatasource.getName(), inputDataMap.getInputDataBy(inputDatasource)))
                .collect(toMap(Entry::getKey, Entry::getValue)));
    }

    InputDataMap getInputDataMapBy(List<PageDatasource> datasources) {
        return getInputDataMapBy(datasources, datasource -> "");
    }

    public InputDataMap getInputDataMapBy(List<PageDatasource> datasources, Function<PageDatasource, String> namespaceProvider) {
        Map<String, InputData> inputDataMaps = datasources.stream()
                .flatMap(datasource -> {
                    InputDataMap inputDataMap = getInputDataMapBy(datasource);
                    return inputDataMap.entrySet().stream()
                            .map(entry -> Map.entry(namespaceProvider.apply(datasource) + entry.getKey(), entry.getValue()));
                })
                .collect(toMap(Entry::getKey, Entry::getValue));
        return new InputDataMap(inputDataMaps);
    }

    public String getNextPageName(PageWorkflowConfiguration pageWorkflowConfiguration, Integer option) {
        if (!pageWorkflowConfiguration.getConditionalNavigation()) {
            return pageWorkflowConfiguration.getNextPages().get(option).getPageName();
        }
        InputDataMap inputDataMap = this.getPage(pageWorkflowConfiguration.getPageConfiguration().getName());
        if (inputDataMap == null) {
            throw new RuntimeException(String.format("Conditional navigation for %s requires page to have data/inputs.", pageWorkflowConfiguration.getPageConfiguration().getName()));
        }

        return pageWorkflowConfiguration.getNextPages().stream()
                .filter(nextPage -> Optional.ofNullable(nextPage.getCondition())
                        .map(inputDataMap::satisfies)
                        .orElse(true))
                .findFirst()
                .map(NextPage::getPageName)
                .orElseThrow(() -> new RuntimeException("Cannot find suitable next page."));
    }

    public boolean shouldSkip(PageWorkflowConfiguration pageWorkflowConfiguration) {
        Condition skipCondition = pageWorkflowConfiguration.getSkipCondition();
        if (skipCondition == null) {
            return false;
        }
        @NotNull InputDataMap inputDataMap = this.getInputDataMapBy(pageWorkflowConfiguration.getDatasources());
        return inputDataMap.satisfies(skipCondition);
    }

    public String resolveTitle(PageWorkflowConfiguration pageWorkflowConfiguration) {
        PageConfiguration pageConfiguration = pageWorkflowConfiguration.getPageConfiguration();
        return resolve(pageWorkflowConfiguration, pageConfiguration.getPageTitle());
    }

    public String resolveHeader(PageWorkflowConfiguration pageWorkflowConfiguration) {
        PageConfiguration pageConfiguration = pageWorkflowConfiguration.getPageConfiguration();
        return resolve(pageWorkflowConfiguration, pageConfiguration.getHeaderKey());
    }

    private String resolve(PageWorkflowConfiguration pageWorkflowConfiguration, Value value) {
        if (value == null) {
            return "";
        }
        return value.getConditionalValues().stream()
                .filter(conditionalValue -> {
                    Objects.requireNonNull(pageWorkflowConfiguration.getDatasources(),
                            "Configuration mismatch! Conditional value cannot be evaluated without a datasource.");
                    @NotNull InputDataMap inputDataMap = this.getInputDataMapBy(pageWorkflowConfiguration.getDatasources());
                    return inputDataMap.satisfies(conditionalValue.getCondition());
                })
                .findFirst()
                .map(ConditionalValue::getValue)
                .orElse(value.getValue());
    }
}
