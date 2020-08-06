package org.codeforamerica.shiba.pages.config;

import lombok.Data;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

@Configuration
@PropertySource(value = "classpath:pages-config.yaml", factory = YamlPropertySourceFactory.class)
@ConfigurationProperties(prefix = "shiba-configuration")
@Data
public class ApplicationConfiguration {
    private List<PageConfiguration> pageDefinitions;
    private LandmarkPagesConfiguration landmarkPages;
    private Map<String, PageWorkflowConfiguration> workflow;
    private Map<String, PageGroupConfiguration> pageGroups;

    public PageWorkflowConfiguration getPageWorkflow(String pageName) {
        return this.workflow.get(pageName);
    }
}
