package org.codeforamerica.shiba.inputconditions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ValueMatcherTest {

  @Test
  void containsOnlyReturnsTrueWhenListContainsOnlyExactExpectedValues() {
    ValueMatcher containsOnlyMatcher = ValueMatcher.CONTAINS_ONLY;
    assertThat(containsOnlyMatcher.matches(List.of("foo", "bar"), "foo")).isFalse();
    assertThat(containsOnlyMatcher.matches(List.of("bar"), "foo")).isFalse();
    assertThat(containsOnlyMatcher.matches(List.of("foo"), "foo")).isTrue();
  }
}