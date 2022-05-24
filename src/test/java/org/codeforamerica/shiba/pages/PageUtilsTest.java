package org.codeforamerica.shiba.pages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class PageUtilsTest {

  @Test
  void shouldConstructPageTitle_withASingleProgram() {
    String titleString = PageUtils.getTitleString(List.of("emergency"));

    assertThat(titleString).isEqualTo("emergency");
  }

  @Test
  void shouldConstructPageTitle_withTwoPrograms() {
    String titleString = PageUtils.getTitleString(List.of("emergency", "child care"));

    assertThat(titleString).isEqualTo("emergency and child care");
  }

  @Test
  void shouldConstructPageTitle_withMoreThanTwoPrograms() {
    String titleString = PageUtils
        .getTitleString(List.of("emergency", "child care", "cash", "food"));

    assertThat(titleString).isEqualTo("emergency, child care, cash and food");
  }
  
	@Test
	void shouldFindPersonNameInList() {
		String[] namesArray = { "Julian Doyle applicant", "Aquila Graves d259c623-395e-4f7a-ba19-56b43447354b",
				"Jayme Lamb 00942599-a681-41d2-8625-8a6538f560f7" };
		List<String> namesList = Arrays.asList(namesArray);
		Boolean isPresent = PageUtils.listOfNamesContainsName(namesList, "Aquila Graves");
		assertTrue(isPresent);
	}

	@Test
	void shouldNotFindPersonNameInList() {
		String[] namesArray = { "Julian Doyle applicant", "Aquila Graves d259c623-395e-4f7a-ba19-56b43447354b",
				"Jayme Lamb 00942599-a681-41d2-8625-8a6538f560f7" };
		List<String> namesList = Arrays.asList(namesArray);
		Boolean isPresent = PageUtils.listOfNamesContainsName(namesList, "Peter Graves");
		assertFalse(isPresent);
	}
    
}
