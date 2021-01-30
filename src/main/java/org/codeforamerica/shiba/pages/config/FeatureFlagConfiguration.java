package org.codeforamerica.shiba.pages.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "feature-flag")
public class FeatureFlagConfiguration extends HashMap<String, FeatureFlag> {

    public FeatureFlagConfiguration(Map<String, FeatureFlag> featureFlags) {
        super(featureFlags);
    }

    @Override
    public FeatureFlag get(Object flag) {
        return super.get(flag) != null ? super.get(flag) : FeatureFlag.OFF;
    }
}
