package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
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

    private final ApplicationData applicationData = new ApplicationData();
    private final PagesData pagesData = new PagesData();

    @TestConfiguration
    @PropertySource(value = "classpath:derived-values-config/test-derived-values-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-derived-values")
        public DerivedValuesConfiguration derivedValuesConfiguration() {
            return new DerivedValuesConfiguration();
        }
    }

    @Autowired
    DerivedValueApplicationInputsMapper derivedValueApplicationInputsMapper;

    @BeforeEach
    void setUp() {
        pagesData.put("defaultPage", new PageData(Map.of("defaultInput", InputData.builder().value(List.of("defaultValue")).build())));
        applicationData.setPagesData(pagesData);
    }

    @Test
    void shouldProjectValue() {
        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).contains(new ApplicationInput("groupName1", List.of("foo"), "value1", ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfConditionIsTrue() {
        pagesData.put("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())));

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).contains(new ApplicationInput("groupName2", List.of("bar"), "value2", ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfAnyOfTheConditionsIsTrue() {
        pagesData.put("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())));

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).contains(new ApplicationInput("groupName3", List.of("baz"), "value3", ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfAllOfTheConditionsAreTrue() {
        pagesData.put("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())));

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).doesNotContain(new ApplicationInput("groupName4", List.of("fooBar"), "value4", ApplicationInputType.SINGLE_VALUE));

        pagesData.put("someOtherPage", new PageData(Map.of("someOtherInput", InputData.builder().value(List.of("someOtherValue")).build())));

        actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).contains(new ApplicationInput("groupName4", List.of("fooBar"), "value4", ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldGetReferencedValueFromPagesData() {
        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(applicationData);

        assertThat(actual).contains(new ApplicationInput("groupName5", List.of("defaultValue"), "value5", ApplicationInputType.SINGLE_VALUE));
    }
}