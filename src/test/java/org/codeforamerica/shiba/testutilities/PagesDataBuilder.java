package org.codeforamerica.shiba.testutilities;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;

public class PagesDataBuilder {

  public PagesData build(List<PageDataBuilder> pageDataBuilders) {
    return new PagesData(pageDataBuilders.stream().map(page -> Map.entry(
        page.getPageName(),
        page.getPageDataMap().entrySet().stream().reduce(
            new PageData(),
            (pageData, entry) -> {
              pageData.put(entry.getKey(), InputData.builder().value(entry.getValue()).build());
              return pageData;
            },
            (entry1, entry2) -> {
              entry1.putAll(entry2);
              return entry1;
            }
        )
    )).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }
}
