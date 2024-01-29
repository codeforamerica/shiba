package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.codeforamerica.shiba.testutilities.AbstractBasePageTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = { "server.servlet.session.timeout = 1m" }) // 1 minute session timeout for the test
public class SessionErrorPageTest extends AbstractBasePageTest {

    @Override
    @BeforeEach
    protected void setUp() throws IOException {
        super.setUp();
        driver.navigate().to(baseUrl);
    }

    @Test
    void shouldDisplaySessionTimeoutPage() throws InterruptedException {
        testPage.clickButton("Apply now");
        TimeUnit time = TimeUnit.SECONDS;
        time.sleep(70); // Sleep for a minute + a margin
        testPage.clickButton("Continue");
        assertThat(driver.getTitle()).isEqualTo("Timeout");
    }
    
    @Test
    void shouldDisplayErrorUploadTimeoutPage() throws InterruptedException {
        testPage.clickButton("Upload documents");
        TimeUnit time = TimeUnit.SECONDS;
        time.sleep(70); // Sleep for a minute + a margin
        testPage.clickButton("Continue");
        assertThat(driver.getTitle()).isEqualTo("Doc Upload Timeout");
    }
}
