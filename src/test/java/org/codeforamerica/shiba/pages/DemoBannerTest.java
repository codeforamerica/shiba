package org.codeforamerica.shiba.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.codeforamerica.shiba.testutilities.AbstractBasePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "demo = true" }) // Demo on, for test
public class DemoBannerTest extends AbstractBasePageTest {

  @Override
  @BeforeEach
  protected void setUp() throws IOException {
    super.setUp();
    driver.navigate().to(baseUrl);
  }

  @Test
  void shouldDisplayBannerWhenDemo() {
    assertEquals(driver.findElement(By.className("demo-banner")).isDisplayed(), true); // Verify Banner
  }
}
