package org.codeforamerica.shiba.framework;

import static java.util.Locale.ENGLISH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.codeforamerica.shiba.testutilities.AbstractFrameworkTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"pagesConfig=pages-config/test-people-inputs.yaml"})
public class PeopleSelectInputsTest extends AbstractFrameworkTest {

  @Override
  @BeforeEach
  protected void setUp() throws Exception {
    super.setUp();
    staticMessageSource.addMessage("option1", ENGLISH, "option 1");
    staticMessageSource.addMessage("option2", ENGLISH, "option 2");
    staticMessageSource.addMessage("option3", ENGLISH, "option 3");
    staticMessageSource
        .addMessage("parent-not-at-home.none-of-the-children-have-parents-living-outside-the-home",
            ENGLISH,
            "None of the children have parents living outside the home");
  }

  @Test
  void shouldRenderInputsBasedOnSelectionsFromPreviousPage() throws Exception {
    var option1 = "Fake Person1 c6624883";
    var option2 = "Fake Person2 jre55443";
    var option3 = "Fake Person3 fafd2345";
    var page = postAndFollowRedirect("firstPage", "peopleSelect", List.of(option1, option2));
    assertThat(page.getElementById(option1)).isNotNull();
    assertThat(page.getElementById(option2)).isNotNull();
    assertThat(page.getElementById(option3)).isNull();
    assertThat(page.getElementById("none__checkbox")).isNotNull();
  }
}
