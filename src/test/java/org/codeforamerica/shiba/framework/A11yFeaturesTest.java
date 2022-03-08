package org.codeforamerica.shiba.framework;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;


import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-input-a11y-features.yaml"})
public class A11yFeaturesTest extends AbstractFrameworkTest {
    final String textAreaPageTitle = "A11y Text Area Test";
    final String textAreaPageHeaderKey = "Header Key";

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        staticMessageSource.addMessage("text-area-page.title", ENGLISH, textAreaPageTitle);
        staticMessageSource.addMessage("text-area-page.headerKey", ENGLISH, textAreaPageHeaderKey);
    }

    @Test
    void shouldHaveAriaLabelledbyOnSingleInputTextAreaPage() throws Exception {
        var page = getFormPage("textAreaPage");
        assertThat(page.getElementById("page-header")).isNotNull();
        assertThat(page.getTextAreaAriaLabelledBy("textAreaInput")).isEqualTo("page-header");
    }
}


