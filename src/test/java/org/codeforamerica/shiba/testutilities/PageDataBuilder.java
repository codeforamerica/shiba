package org.codeforamerica.shiba.testutilities;


import java.util.List;
import java.util.Map;
import lombok.Value;

@Value
public class PageDataBuilder {

  String pageName;
  Map<String, List<String>> pageDataMap;
}
