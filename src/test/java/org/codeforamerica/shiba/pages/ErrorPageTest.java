package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.AbstractBasePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorPageTest extends AbstractBasePageTest {

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl);
    }

    @Test
    void shouldShow404PageIfPageDoesNotExist() {
        driver.navigate().to(baseUrl + "/foo");
        assertThat(driver.getTitle()).isEqualTo("404 Error");
    }

    @Test
    void shouldShow500PageIfErrorOtherThan404() {
        driver.navigate().to(baseUrl + "/;");
        assertThat(driver.getTitle()).isEqualTo("500 Error");
    }
}
