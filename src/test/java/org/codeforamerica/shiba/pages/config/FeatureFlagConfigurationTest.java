package org.codeforamerica.shiba.pages.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class FeatureFlagConfigurationTest {

  @Test
  void getShouldReturnFeatureFlagWhenPresent() {
    FeatureFlagConfiguration featureFlags = new FeatureFlagConfiguration(
        Map.of("flag", FeatureFlag.OFF));
    assertThat(featureFlags.get("flag").isOff()).isTrue();
  }

  @Test
  void getShouldReturnFeatureFlagOffIfNotPresent() {
    FeatureFlagConfiguration featureFlags = new FeatureFlagConfiguration(
        Map.of("flag", FeatureFlag.ON));
    assertThat(featureFlags.get("nonexistent-flag").isOn()).isFalse();
    assertThat(featureFlags.get("nonexistent-flag").isOff()).isTrue();
  }
}
