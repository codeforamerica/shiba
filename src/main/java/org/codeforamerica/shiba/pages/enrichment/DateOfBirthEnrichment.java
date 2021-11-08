package org.codeforamerica.shiba.pages.enrichment;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;

public abstract class DateOfBirthEnrichment implements Enrichment {

  /**
   * Adds '0' padding to single digit days and months in given birth date. Ex. {"1", "2", "1999"}
   * would return "01/02/1999"
   *
   * @param dateOfBirth date of birth as array list
   * @return formatted date of birth as String
   */
  private static String formatDateOfBirth(List<String> dateOfBirth) {
    if (dateOfBirth.isEmpty()) {
      return "";
    }
    return StringUtils.leftPad(dateOfBirth.get(0), 2, '0') + '/' +
           StringUtils.leftPad(dateOfBirth.get(1), 2, '0') + '/' +
           dateOfBirth.get(2);
  }

  @Override
  public PageData process(PagesData pagesData) {
    String dobString = formatDateOfBirth(parseDateOfBirth(pagesData));
    return new PageData(Map.of("dobAsDate", new InputData(List.of(dobString))));
  }

  protected abstract List<String> parseDateOfBirth(PagesData pagesData);
}
