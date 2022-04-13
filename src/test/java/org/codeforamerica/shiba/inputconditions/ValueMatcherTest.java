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
}