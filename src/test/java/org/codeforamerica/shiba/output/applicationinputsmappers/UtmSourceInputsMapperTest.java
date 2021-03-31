package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.ApplicationInputType;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.applicationinputsmappers.UtmSourceInputsMapper.CHILDCARE_WAITING_LIST_UTM_SOURCE;

public class UtmSourceInputsMapperTest {
    private final UtmSourceInputsMapper mapper = new UtmSourceInputsMapper();

    @Test
    void shouldMapRecognizedUtmSource() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setUtmSource(CHILDCARE_WAITING_LIST_UTM_SOURCE);

        List<ApplicationInput> result = mapper.map(Application.builder()
                .applicationData(applicationData)
                .build(), null, null);

        assertThat(result).containsExactlyInAnyOrder(
                new ApplicationInput(
                        "nonPagesData",
                        "utmSource",
                        List.of("FROM BSF WAITING LIST"),
                        ApplicationInputType.SINGLE_VALUE));
    }

    @Test
    void shouldMapUnrecognizedUtmSourceToEmpty() {
        ApplicationData applicationData = new ApplicationData();
        applicationData.setUtmSource("somewhere_unknown");

        List<ApplicationInput> result = mapper.map(Application.builder()
                .applicationData(applicationData)
                .build(), null, null);

        assertThat(result).containsExactlyInAnyOrder(
                new ApplicationInput(
                        "nonPagesData",
                        "utmSource",
                        List.of(""),
                        ApplicationInputType.SINGLE_VALUE));
    }
}