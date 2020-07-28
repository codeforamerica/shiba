package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
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

        when(mockDecider.decide(any())).thenReturn(ExpeditedEligibility.ELIGIBLE);

        assertThat(mapper.map(appData)).containsExactly(
                new ApplicationInput(
                        "expeditedEligibility",
                        "expeditedEligibility",
                        List.of("ELIGIBLE"),
                        ApplicationInputType.ENUMERATED_SINGLE_VALUE
                )
        );
    }

    @Test
    void shouldReturnNotEligibleWhenDeciderDecidesNotEligible() {
        ExpeditedEligibilityMapper mapper = new ExpeditedEligibilityMapper(mockDecider);
        ApplicationData appData = new ApplicationData();
        PagesData pagesData = new PagesData();
        pagesData.put("page1", new PageData());
        appData.setPagesData(pagesData);

        when(mockDecider.decide(any())).thenReturn(ExpeditedEligibility.NOT_ELIGIBLE);

        List<ApplicationInput> result = mapper.map(appData);

        verify(mockDecider).decide(pagesData);
        assertThat(result).containsExactly(
                new ApplicationInput(
                        "expeditedEligibility",
                        "expeditedEligibility",
                        List.of("NOT_ELIGIBLE"),
                        ApplicationInputType.ENUMERATED_SINGLE_VALUE
                )
        );
    }
}