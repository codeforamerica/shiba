package org.codeforamerica.shiba.pages.config;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "feature-flag")
public class FeatureFlagConfiguration extends HashMap<String, FeatureFlag> {

  @Serial
  private static final long serialVersionUID = -961416391895421339L;

  @Autowired
  public FeatureFlagConfiguration(Map<String, FeatureFlag> featureFlags) {
    super(featureFlags);
  }

  @Override
  public FeatureFlag get(Object flag) {
    return super.get(flag) != null ? super.get(flag) : FeatureFlag.OFF;
  }
}
