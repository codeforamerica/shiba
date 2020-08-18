package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
class ApplicationInputsMappersTest {
    @TestConfiguration
    @PropertySource(value = "classpath:test-output-mapping-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-output-map")
        public OutputMappingConfiguration outputMappingConfiguration() {
            return new OutputMappingConfiguration();
        }
    }

    @MockBean(name = "subworkflowInputMapper")
    ApplicationInputsMapper mockMapper;

    @Autowired
    ApplicationInputsMappers applicationInputsMappers;

    @Test
    void shouldMapOutputBasedOnConfiguration() {
        when(mockMapper.map(any())).thenReturn(List.of(new ApplicationInput(
                "page1",
                "input1",
                List.of("SOME_VALUE"),
                ApplicationInputType.SINGLE_VALUE,
                0
        )));

        List<ApplicationInput> result = applicationInputsMappers.map(new ApplicationData());

        assertThat(result).contains(new ApplicationInput("page1", "input1", List.of("Some value"), ApplicationInputType.SINGLE_VALUE, 0));
    }
}