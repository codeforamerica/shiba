package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.PageDataBuilder;
import org.codeforamerica.shiba.PagesDataBuilder;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class AdultRequestingChildcareInputsMapperTest {

    @Test
    void shouldReturnEmptyListWhenLivingAlone() {
        ApplicationData appData = new ApplicationData();
        appData.setPagesData(new PagesDataBuilder().build(List.of(
                new PageDataBuilder("doYouLiveAlone", Map.of("liveAlone", List.of("true")))
        )));

        Application application = Application.builder().applicationData(appData).build();
        assertThat(new AdultRequestingChildcareInputsMapper().map(application, Recipient.CLIENT, new SubworkflowIterationScopeTracker())).isEqualTo(emptyList());
    }

    @Test
    void shouldReturnEmptyListWithoutCCAP() {
        ApplicationData appData = new ApplicationData();
        appData.setPagesData(new PagesDataBuilder().build(List.of(
                new PageDataBuilder("choosePrograms", Map.of("programs", List.of("EA", "GRH")))
        )));

        Application application = Application.builder().applicationData(appData).build();
        assertThat(new AdultRequestingChildcareInputsMapper().map(application, Recipient.CLIENT, new SubworkflowIterationScopeTracker())).isEqualTo(emptyList());
    }
}