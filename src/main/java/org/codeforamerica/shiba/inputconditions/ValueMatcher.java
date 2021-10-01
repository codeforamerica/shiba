package org.codeforamerica.shiba.inputconditions;

import static org.codeforamerica.shiba.County.*;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.*;

import java.util.List;
import java.util.function.BiFunction;
import org.codeforamerica.shiba.County;

/* Matcher used for Condition */
public enum ValueMatcher {
  EMPTY((testValue, ignoredTargetValue) -> testValue.stream().allMatch(String::isEmpty)),
  NONE_SELECTED((testValue, ignoredTargetValue) -> testValue.isEmpty()),
  CONTAINS(List::contains),
  NOT_EMPTY((testValue, ignoredTargetValue) -> !String.join("", testValue).isBlank()),
  CONTAINS_STRING_OTHER_THAN((testValue, targetValue) -> testValue.stream()
      .anyMatch(string -> !string.equals(targetValue))),
  DOES_NOT_CONTAIN((testValue, targetValue) -> !testValue.contains(targetValue)),
  CONTAINS_SUBSTRING((testValue, targetValue) -> testValue.stream()
      .anyMatch(string -> string.contains(targetValue))),
  IS_URBAN_TRIBAL_NATION_MEMBER(
      (testValue, ignoredTargetValue) ->
          List.of(BOIS_FORTE, GRAND_PORTAGE, LEECH_LAKE, MILLE_LACS_BAND_OF_OJIBWE,
              WHITE_EARTH, FOND_DU_LAC).containsAll(testValue)
  ),
  IS_URBAN_TRIBAL_NATION_COUNTY((testValue, ignoredTargetValue) ->
      URBAN_COUNTIES.stream().map(County::displayName).anyMatch(testValue::contains)),
  IS_MILLE_LACS_RURAL_COUNTY((testValue, ignoredTargetValue) ->
      List.of(
          Aitkin.displayName(), Benton.displayName(), CrowWing.displayName(),
          Morrison.displayName(), MilleLacs.displayName(), Pine.displayName()
      ).containsAll(testValue)),
  IS_WHITE_EARTH_COUNTY((testValue, ignored) -> COUNTIES_SERVICED_BY_WHITE_EARTH.stream()
      .map(County::displayName)
      .anyMatch(testValue::contains)),
  DOES_NOT_CONTAIN_SUBSTRING((testValue, targetValue) -> testValue.stream()
      .noneMatch(string -> string.contains(targetValue)));

  private final BiFunction<List<String>, String, Boolean> matcher;

  ValueMatcher(BiFunction<List<String>, String, Boolean> matcher) {
    this.matcher = matcher;
  }

  public Boolean matches(List<String> testValue, String targetValue) {
    return this.matcher.apply(testValue, targetValue);
  }
}
