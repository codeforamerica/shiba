package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.ApplicationData;
import org.codeforamerica.shiba.pages.FormData;
import org.codeforamerica.shiba.pages.InputData;
import org.codeforamerica.shiba.pages.PagesData;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpeditedEligibilityMapperTest {
    ExpeditedEligibilityDecider mockDecider = mock(ExpeditedEligibilityDecider.class);

    @Test
    void shouldReturnEligibleWhenDeciderDecidesEligible() {
        ExpeditedEligibilityMapper mapper = new ExpeditedEligibilityMapper(mockDecider);
        ApplicationData appData = new ApplicationData();

        when(mockDecider.decide(any())).thenReturn(true);

        assertThat(mapper.map(appData)).containsExactly(
                new ApplicationInput(
                        "expeditedEligibility",
                        List.of("ELIGIBLE"),
                        "expeditedEligibility",
                        ApplicationInputType.ENUMERATED_SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldReturnNotEligibleWhenDeciderDecidesNotEligible() {
        ExpeditedEligibilityMapper mapper = new ExpeditedEligibilityMapper(mockDecider);
        ApplicationData appData = new ApplicationData();
        PagesData pagesData = new PagesData();
        FormData formData1 = new FormData();
        InputData inputData1 = new InputData(List.of(""));
        formData1.put("someInputName", inputData1);

        FormData formData2 = new FormData();
        InputData inputData2 = new InputData(List.of(""));
        formData2.put("someInputName", inputData2);

        pagesData.put("page1", formData1);
        pagesData.put("page2", formData2);

        appData.setPagesData(pagesData);

        when(mockDecider.decide(any())).thenReturn(false);

        HashMap<String, InputData> expectedData = new HashMap<>();
        expectedData.put("page1_someInputName", inputData1);
        expectedData.put("page2_someInputName", inputData2);

        List<ApplicationInput> result = mapper.map(appData);

        verify(mockDecider).decide(expectedData);
        assertThat(result).containsExactly(
                new ApplicationInput(
                        "expeditedEligibility",
                        List.of("NOT_ELIGIBLE"),
                        "expeditedEligibility",
                        ApplicationInputType.ENUMERATED_SINGLE_VALUE
                )
        );
    }
}