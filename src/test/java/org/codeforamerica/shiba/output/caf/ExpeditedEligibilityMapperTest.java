package org.codeforamerica.shiba.output.caf;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
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
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(appData)
                .county(County.Other)
                .timeToComplete(null)
                .build();

        when(mockDecider.decide(any())).thenReturn(ExpeditedEligibility.ELIGIBLE);

        assertThat(mapper.map(application, Recipient.CLIENT, null)).containsExactly(
                new ApplicationInput(
                        "expeditedEligibility",
                        "expeditedEligibility",
                        List.of("Expedited"),
                        ApplicationInputType.SINGLE_VALUE
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
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(appData)
                .county(County.Other)
                .timeToComplete(null)
                .build();

        when(mockDecider.decide(any())).thenReturn(ExpeditedEligibility.NOT_ELIGIBLE);

        List<ApplicationInput> result = mapper.map(application, Recipient.CLIENT, null);

        verify(mockDecider).decide(appData);
        assertThat(result).containsExactly(
                new ApplicationInput(
                        "expeditedEligibility",
                        "expeditedEligibility",
                        List.of("Non-Expedited"),
                        ApplicationInputType.SINGLE_VALUE
                )
        );
    }
}