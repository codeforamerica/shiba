package org.codeforamerica.shiba.output;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.YamlPropertySourceFactory;
import org.codeforamerica.shiba.output.applicationinputsmappers.DerivedValueApplicationInputsMapper;
import org.codeforamerica.shiba.pages.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
class DerivedValueApplicationInputsMapperTest {

    private final ApplicationData applicationData = new ApplicationData();
    private final PagesData pagesData = new PagesData();
    private final Application application = new Application("someId", ZonedDateTime.now(), applicationData, County.OTHER);

    @TestConfiguration
    @PropertySource(value = "classpath:derived-values-config/test-derived-values-config.yaml", factory = YamlPropertySourceFactory.class)
    static class TestPageConfiguration {
        @Bean
        @ConfigurationProperties(prefix = "test-derived-values")
        public PotentialDerivedValuesConfiguration derivedValuesConfiguration() {
            return new PotentialDerivedValuesConfiguration();
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
        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).contains(new ApplicationInput("groupName1", "value1", List.of("foo"), ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfConditionIsTrue() {
        pagesData.put("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())));

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).contains(new ApplicationInput("groupName2", "value2", List.of("bar"), ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfAnyOfTheConditionsIsTrue() {
        pagesData.put("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())));

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).contains(new ApplicationInput("groupName3", "value3", List.of("baz"), ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldProjectValueIfAllOfTheConditionsAreTrue() {
        pagesData.put("somePage", new PageData(Map.of("someInput", InputData.builder().value(List.of("someValue")).build())));

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).doesNotContain(new ApplicationInput("groupName4", "value4", List.of("fooBar"), ApplicationInputType.SINGLE_VALUE));

        pagesData.put("someOtherPage", new PageData(Map.of("someOtherInput", InputData.builder().value(List.of("someOtherValue")).build())));

        actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).contains(new ApplicationInput("groupName4", "value4", List.of("fooBar"), ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldGetReferencedValueFromPagesData() {
        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).contains(new ApplicationInput("groupName5", "value5", List.of("defaultValue"), ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldNotCreateApplicationInput_WhenConditionForSubworkflow_isFalse() {
        Subworkflows subworkflows = new Subworkflows();
        Subworkflow subworkflow = new Subworkflow();
        PagesData subworkflowPages = new PagesData();
        PageData pageData = new PageData();
        pageData.put("input1", InputData.builder().value(List.of("wrong_value")).build());
        subworkflowPages.put("page1", pageData);
        subworkflow.add(subworkflowPages);
        subworkflows.put("subworkflowName", subworkflow);
        applicationData.setSubworkflows(subworkflows);

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).doesNotContain(new ApplicationInput(
                "groupName8",
                "value8",
                List.of("bar"),
                ApplicationInputType.SINGLE_VALUE
        ));
    }

    @Test
    void shouldCreateApplicationInput_WhenConditionForSubworkflow_isTrue() {
        Subworkflows subworkflows = new Subworkflows();
        Subworkflow subworkflow = new Subworkflow();
        PagesData subworkflowPages = new PagesData();
        PageData pageData = new PageData();
        pageData.put("input1", InputData.builder().value(List.of("right_value")).build());
        subworkflowPages.put("page1", pageData);
        subworkflow.add(subworkflowPages);
        subworkflows.put("subworkflowName", subworkflow);
        applicationData.setSubworkflows(subworkflows);

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).contains(new ApplicationInput(
                "groupName8",
                "value8",
                List.of("bar"),
                ApplicationInputType.SINGLE_VALUE,
                0
        ));
    }

    @Test
    void shouldApplyConditionTypeToAllIterationsOfSubworkflowWhenASpecificIterationIsNotDeclared() {
        Subworkflows subworkflows = new Subworkflows();
        Subworkflow subworkflow = new Subworkflow();
        PagesData subworkflowPages1 = new PagesData();
        PageData pageData1 = new PageData();
        pageData1.put("input1", InputData.builder().value(List.of("wrong_value")).build());
        subworkflowPages1.put("page1", pageData1);
        subworkflow.add(subworkflowPages1);
        PagesData subworkflowPages2 = new PagesData();
        PageData pageData2 = new PageData();
        pageData2.put("input1", InputData.builder().value(List.of("right_value")).build());
        subworkflowPages2.put("page1", pageData2);
        subworkflow.add(subworkflowPages2);
        subworkflows.put("subworkflowName", subworkflow);
        applicationData.setSubworkflows(subworkflows);

        List<ApplicationInput> actual = derivedValueApplicationInputsMapper.map(application, Recipient.CLIENT);

        assertThat(actual).contains(new ApplicationInput(
                "groupName9",
                "value9",
                List.of("bar"),
                ApplicationInputType.SINGLE_VALUE
        ));
    }
}