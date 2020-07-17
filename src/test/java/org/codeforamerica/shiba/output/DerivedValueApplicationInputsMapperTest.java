package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.ApplicationData;
import org.codeforamerica.shiba.pages.FormData;
import org.codeforamerica.shiba.pages.InputData;
import org.codeforamerica.shiba.pages.PagesData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
class DerivedValueApplicationInputsMapperTest {
    @TestConfiguration
    @PropertySource(value = "classpath:test-derived-values-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-derived-values")
        public DerivedValueConfiguration derivedValueConfiguration() {
            return new DerivedValueConfiguration();
        }
    }

    @Autowired
    DerivedValueApplicationInputsMapper derivedValueApplicationInputsMapper;

    @Test
    void shouldProjectValue() {
        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(new ApplicationData());

        assertThat(actual).containsExactly(new ApplicationInput("groupName1", List.of("foo"), "value1", ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfConditionIsTrue() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        pagesData.put("somePage", new FormData(Map.of("someInput", new InputData(List.of("someValue")))));
        applicationData.setPagesData(pagesData);

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).contains(new ApplicationInput("groupName2", List.of("bar"), "value2", ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfAnyOfTheConditionsIsTrue() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        pagesData.put("somePage", new FormData(Map.of("someInput", new InputData(List.of("someValue")))));
        applicationData.setPagesData(pagesData);

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).contains(new ApplicationInput("groupName3", List.of("baz"), "value3", ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfAllOfTheConditionsAreTrue() {
        ApplicationData applicationData = new ApplicationData();
        PagesData pagesData = new PagesData();
        pagesData.put("somePage", new FormData(Map.of("someInput", new InputData(List.of("someValue")))));
        applicationData.setPagesData(pagesData);

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).doesNotContain(new ApplicationInput("groupName4", List.of("fooBar"), "value4", ApplicationInputType.SINGLE_VALUE));

        pagesData.put("someOtherPage", new FormData(Map.of("someOtherInput", new InputData(List.of("someOtherValue")))));

        actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).contains(new ApplicationInput("groupName4", List.of("fooBar"), "value4", ApplicationInputType.SINGLE_VALUE));
    }
}