package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
class SubworkflowInputMapperTest {
    private final ApplicationData applicationData = new ApplicationData();
    private final Subworkflows subworkflows = new Subworkflows();

    @TestConfiguration
    @PropertySource(value = "classpath:pages-config/test-sub-workflow.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-subworkflow")
        public ApplicationConfiguration applicationConfiguration() {
            return new ApplicationConfiguration();
        }
    }

    @Autowired
    SubworkflowInputMapper subworkflowInputMapper;

    @Test
    void shouldNameSpaceValuesFromEachIteration() {
        PagesData iteration1 = new PagesData(
                Map.of(
                        "question1",
                        new PageData(Map.of(
                                "input1", InputData.builder().value(List.of("string")).build(),
                                "input2", InputData.builder().value(List.of("coolString")).build())
                        ))
        );

        PagesData iteration2 = new PagesData(
                Map.of(
                        "question1",
                        new PageData(Map.of(
                                "input1", InputData.builder().value(List.of("otherString")).build(),
                                "input2", InputData.builder().value(List.of("weirdString")).build())
                        ))
        );

        PagesData iteration3 = new PagesData(
                Map.of(
                        "question2",
                        new PageData(Map.of("input1", InputData.builder().value(List.of("differentString")).build()))
                )
        );

        subworkflows.addIteration("group1", iteration1);
        subworkflows.addIteration("group1", iteration2);
        subworkflows.addIteration("group2", iteration3);

        applicationData.setSubworkflows(subworkflows);
        assertThat(subworkflowInputMapper.map(applicationData)).containsExactlyInAnyOrder(
                new ApplicationInput(
                        "question1",
                        "input1",
                        List.of("string"),
                        ApplicationInputType.SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
                        "question1",
                        "input2",
                        List.of("coolString"),
                        ApplicationInputType.SINGLE_VALUE,
                        0
                ),
                new ApplicationInput(
                        "question1",
                        "input1",
                        List.of("otherString"),
                        ApplicationInputType.SINGLE_VALUE,
                        1
                ),
                new ApplicationInput(
                        "question1",
                        "input2",
                        List.of("weirdString"),
                        ApplicationInputType.SINGLE_VALUE,
                        1
                ),
                new ApplicationInput(
                        "question2",
                        "input1",
                        List.of("differentString"),
                        ApplicationInputType.SINGLE_VALUE,
                        0
                )
        );
    }
}