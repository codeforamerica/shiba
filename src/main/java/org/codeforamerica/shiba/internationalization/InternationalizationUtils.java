package org.codeforamerica.shiba.internationalization;

import java.util.List;

public class InternationalizationUtils {

  private InternationalizationUtils() {
    throw new AssertionError("Cannot instantiate utility class");
  }

  /**
   * Takes a list of strings and returns an English- or Spanish-language string representation of
   * that list e.g. ["a", "b", "c"] -> "a, b and c"
   * <p>
   * The Oxford comma is omitted for Spanish-language compatibility. Spanish does not use the Oxford
   * comma.
   * <p>
   * If we ever add a language which represents lists differently, this method will need to be
   * updated.
   */
  public static String listToString(List<String> list, LocaleSpecificMessageSource lms) {
	for (String item : list) {
		if (item==null || item.isBlank()) {
			list.remove(item);
		}
	}
    if (list.isEmpty()) {
      return "";
    }

    int lastIdx = list.size() - 1;
    String lastElement = list.get(lastIdx);
    if (list.size() == 1) {
      return lastElement;
    }

    String firstPart = String.join(", ", list.subList(0, lastIdx));
    String and = lms.getMessage("general.and");

    return "%s %s %s".formatted(firstPart, and, lastElement);
  }
}
