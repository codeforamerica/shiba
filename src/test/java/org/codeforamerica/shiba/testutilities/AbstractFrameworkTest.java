package org.codeforamerica.shiba.testutilities;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Import({AbstractFrameworkTest.StartTimerController.class})
public class AbstractFrameworkTest extends AbstractStaticMessageSourceFrameworkTest {

  @Autowired
  protected StaticMessageSource staticMessageSource;

  @Override
  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    mockMvc.perform(get("/startTimerForTest").session(session))
        .andExpect(status().isOk()); // start timer
  }

  @Controller
  public static class StartTimerController {

    private final ApplicationData applicationData;

    public StartTimerController(ApplicationData applicationData) {
      this.applicationData = applicationData;
    }

    @GetMapping("/startTimerForTest")
    String setStartTimeForTest() {
      applicationData.setStartTimeOnce(Instant.now());
      return "startTimeIsSet";
    }
  }
}

