package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.codeforamerica.shiba.testutilities.AbstractBasePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ErrorPageTest extends AbstractBasePageTest {

  @Override
  @BeforeEach
  protected void setUp() throws IOException {
    super.setUp();
    driver.navigate().to(baseUrl);
  }

  @Test
  void shouldDisplayTheSameErrorPageForDifferentClassesOfErrors() {
    driver.navigate().to(baseUrl + "/foo"); // not found
    assertThat(driver.getTitle()).isEqualTo("Error");
    assertEquals(driver.findElementByTagName("h1").getText(), "Something went wrong!");
    driver.navigate().to(baseUrl + "/;"); // internal server error
    assertThat(driver.getTitle()).isEqualTo("Error");
    assertEquals(driver.findElementByTagName("h1").getText(), "Something went wrong!");
  }
}
