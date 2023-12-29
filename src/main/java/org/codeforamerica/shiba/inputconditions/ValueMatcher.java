package org.codeforamerica.shiba.inputconditions;

import static org.codeforamerica.shiba.TribalNation.*;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.TribalNation;

/* Matcher used for Condition */
public enum ValueMatcher {
  EMPTY((testValue, ignoredTargetValue) -> testValue.stream().allMatch(String::isEmpty)),

  NONE_SELECTED((testValue, ignore) -> testValue.isEmpty()),

  CONTAINS(List::contains),
  
  CONTAINS_ONLY((testValue, targetValue) -> containsOnly(testValue, targetValue)),

  DOES_NOT_EQUAL(
      (testValue, targetValue) -> testValue.size() != 1 || !testValue.get(0).equals(targetValue)),
  
  DOES_NOT_EQUAL_IGNORE_CASE((testValue, targetValue) -> testValue.stream().noneMatch(s -> s.equalsIgnoreCase(targetValue))),

  NOT_EMPTY((testValue, ignore) -> !String.join("", testValue).isBlank()),

  CONTAINS_STRING_OTHER_THAN((testValue, targetValue) -> testValue.stream()
      .anyMatch(string -> !string.equals(targetValue))),

  DOES_NOT_CONTAIN((testValue, targetValue) -> !testValue.contains(targetValue)),

  CONTAINS_SUBSTRING((testValue, targetValue) ->
      testValue.stream().anyMatch(string -> string.contains(targetValue))),

  IS_URBAN_TRIBAL_NATION_MEMBER((testValue, ignore) ->
      MILLE_LACS_SERVICED_TRIBES.stream().map(TribalNation::toString).collect(Collectors.toSet())
          .containsAll(testValue)),

  IS_URBAN_TRIBAL_NATION_COUNTY((testValue, ignoredTargetValue) ->
      URBAN_COUNTIES.stream().map(County::toString).anyMatch(testValue::contains)),

  IS_MILLE_LACS_RURAL_COUNTY((testValue, ignoredTargetValue) ->
      MILLE_LACS_RURAL_COUNTIES.stream().map(County::toString).anyMatch(testValue::contains)),

  IS_WHITE_EARTH_COUNTY((testValue, ignored) -> COUNTIES_SERVICED_BY_WHITE_EARTH.stream()
      .map(County::toString)
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
  
  private static Boolean containsOnly(List<String> testValue, String targetValue) {
	List<String> programNoneOnly = testValue.stream().filter(program -> !program.equals(targetValue)).toList();
	List<String> programOnly = testValue.stream().filter(program -> program.equals(targetValue)).toList();
	return (programNoneOnly.stream().allMatch(string -> string.contains("NONE")) && programOnly.stream().allMatch(string -> string.contains(targetValue)));
  }
}
