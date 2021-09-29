package org.codeforamerica.shiba.inputconditions;

import static org.codeforamerica.shiba.County.Aitkin;
import static org.codeforamerica.shiba.County.Anoka;
import static org.codeforamerica.shiba.County.Benton;
import static org.codeforamerica.shiba.County.CrowWing;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.County.MilleLacs;
import static org.codeforamerica.shiba.County.Morrison;
import static org.codeforamerica.shiba.County.Pine;
import static org.codeforamerica.shiba.County.Ramsey;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.BOIS_FORTE;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.FOND_DU_LAC;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.GRAND_PORTAGE;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.LEECH_LAKE;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.MILLE_LACS_BAND_OF_OJIBWE;
import static org.codeforamerica.shiba.TribalNationRoutingDestination.WHITE_EARTH;

import java.util.List;
import java.util.function.BiFunction;

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
      List.of(Hennepin.displayName(), Anoka.displayName(), Ramsey.displayName())
          .containsAll(testValue)),
  IS_MILLE_LACS_RURAL_COUNTY((testValue, ignoredTargetValue) ->
      List.of(Aitkin.displayName(), Benton.displayName(), CrowWing.displayName(),
          Morrison.displayName(), MilleLacs.displayName(), Pine.displayName()
      ).containsAll(testValue)),
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
