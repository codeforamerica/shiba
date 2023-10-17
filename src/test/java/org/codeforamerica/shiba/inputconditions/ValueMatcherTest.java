package org.codeforamerica.shiba.inputconditions;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValueMatcherTest {

  @Test
  void doesNotEqualReturnsTrueWhenOneValueDoesNotEqualACompleteList(){
    ValueMatcher doesNotEqualMatcher = ValueMatcher.DOES_NOT_EQUAL;
    assertThat(doesNotEqualMatcher.matches(List.of("foo"), "foo")).isFalse();
    assertThat(doesNotEqualMatcher.matches(List.of("foo"), "bar")).isTrue();
    assertThat(doesNotEqualMatcher.matches(List.of("foo", "bar"), "foo")).isTrue();
  }
  
  @Test
  void testMilleLacsRuralCounties() {
	  ValueMatcher valueMatcher = ValueMatcher.IS_MILLE_LACS_RURAL_COUNTY;
	  assertThat(valueMatcher.matches(List.of("Aitkin"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Benton"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Crow Wing"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Morrison"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Mille Lacs"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Pine"), null)).isTrue();
	  //anything else is false
	  assertThat(valueMatcher.matches(List.of("Hennepin"), null)).isFalse();
	  assertThat(valueMatcher.matches(List.of("Lake of the Woods"), null)).isFalse();
	  assertThat(valueMatcher.matches(List.of("foobar"), null)).isFalse();
  }
  

  @Test 
  void testUrbanTribalNationCounties() {
	  ValueMatcher valueMatcher = ValueMatcher.IS_URBAN_TRIBAL_NATION_COUNTY;
	  assertThat(valueMatcher.matches(List.of("Hennepin"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Anoka"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Ramsey"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Chisago"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Kanabec"), null)).isTrue();
	  //anything else is false
	  assertThat(valueMatcher.matches(List.of("Aitkin"), null)).isFalse();
	  assertThat(valueMatcher.matches(List.of("Yellow Medicine"), null)).isFalse();
	  assertThat(valueMatcher.matches(List.of("foobar"), null)).isFalse();
  }
  
  @Test 
  void testIsMilleLacsServicedTribe() {
	  ValueMatcher valueMatcher = ValueMatcher.IS_URBAN_TRIBAL_NATION_MEMBER;
	  assertThat(valueMatcher.matches(List.of("Bois Forte"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Grand Portage"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Leech Lake"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Mille Lacs Band of Ojibwe"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("White Earth Nation"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Fond Du Lac"), null)).isTrue();
	  //anything else is false
	  assertThat(valueMatcher.matches(List.of("Shakopee Mdewakanton"), null)).isFalse();
	  assertThat(valueMatcher.matches(List.of("Prairie Island"), null)).isFalse();
	  assertThat(valueMatcher.matches(List.of("foobar"), null)).isFalse();
  }
  
  @Test 
  void testIsWhiteEarthServicedTribe() {
	  ValueMatcher valueMatcher = ValueMatcher.IS_WHITE_EARTH_COUNTY;
	  assertThat(valueMatcher.matches(List.of("Becker"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Mahnomen"), null)).isTrue();
	  assertThat(valueMatcher.matches(List.of("Clearwater"), null)).isTrue();
	  //anything else is false
	  assertThat(valueMatcher.matches(List.of("Aitkin"), null)).isFalse();
	  assertThat(valueMatcher.matches(List.of("Yellow Medicine"), null)).isFalse();
	  assertThat(valueMatcher.matches(List.of("foobar"), null)).isFalse();
  }
}