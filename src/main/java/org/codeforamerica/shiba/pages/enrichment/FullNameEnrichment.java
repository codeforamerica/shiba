package org.codeforamerica.shiba.pages.enrichment;

import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.Field.WHOSE_JOB_IS_IT;
import static org.codeforamerica.shiba.application.parsers.ApplicationDataParser.getFirstValue;

import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.output.FullNameFormatter;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

/**
 * Add formatted full name (without iteration ID) to write to the PDFs.
 */
@Component
public class FullNameEnrichment implements Enrichment {

  @Override
  public PageData process(PagesData pagesData) {
    String nameWithId = getFirstValue(pagesData, WHOSE_JOB_IS_IT);
    if (nameWithId == null || nameWithId.isBlank()) {
      return new PageData();
    }

    return new PageData(Map.of("whoseJobIsItFormatted",
        new InputData(List.of(FullNameFormatter.format(nameWithId)))));
  }
}
