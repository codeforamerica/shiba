package org.codeforamerica.shiba.pages;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
}
