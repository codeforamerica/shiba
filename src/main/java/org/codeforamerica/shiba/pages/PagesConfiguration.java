package org.codeforamerica.shiba.pages;

import lombok.Data;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:pages-config.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "shiba-configuration")
@Data
public class PagesConfiguration {
    private Map<String, PageConfiguration> pages = new HashMap<>();
    private FlowConfiguration flow;

    public PageConfiguration getPage(String pageName) {
        return this.pages.get(pageName);
    }

    public NavigationConfiguration getNavigationPage(String pageName) {
        return this.getFlow().getNavigation().get(pageName);
    }
}
