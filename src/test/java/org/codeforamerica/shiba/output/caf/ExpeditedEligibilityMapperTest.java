package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.ApplicationData;
import org.codeforamerica.shiba.pages.InputDataMap;
import org.codeforamerica.shiba.pages.PagesData;
import org.junit.jupiter.api.Test;

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
        pagesData.put("page1", new InputDataMap());
        appData.setPagesData(pagesData);

        when(mockDecider.decide(any())).thenReturn(false);

        List<ApplicationInput> result = mapper.map(appData);

        verify(mockDecider).decide(pagesData);
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