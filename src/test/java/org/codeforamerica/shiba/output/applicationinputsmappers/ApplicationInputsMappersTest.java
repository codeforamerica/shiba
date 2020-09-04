package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApplicationInputsMappersTest {
    private final ApplicationInputsMappers mappers = new ApplicationInputsMappers(List.of());

    @Test
    void shouldIncludeApplicationIdInput() {
        List<ApplicationInput> applicationInputs = mappers.map(new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED), CLIENT);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "applicationId", List.of("someId"), SINGLE_VALUE));
    }

    @Test
    void shouldIncludeCompletedDateInput() {
        String applicationId = "someId";
        ZonedDateTime completedAt = ZonedDateTime.of(
                LocalDateTime.of(2020, 9, 3, 1, 2, 3),
                ZoneOffset.UTC);
        Application application = new Application(applicationId, completedAt, new ApplicationData(), null);

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "completedDate", List.of("2020-09-02"), SINGLE_VALUE));
    }

    @Test
    void shouldIncludeCompletedDateTimeInput() {
        String applicationId = "someId";
        ZonedDateTime completedAt = ZonedDateTime.of(LocalDateTime.of(2019, 11, 16, 5, 29, 1), ZoneOffset.UTC);
        Application application = new Application(applicationId, completedAt, new ApplicationData(), null);

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "completedDateTime", List.of("2019-11-16T05:29:01Z"), SINGLE_VALUE));
    }

    @Test
    void shouldUseMatchingRecipientForMappers() {
        ApplicationInputsMapper mapper = mock(ApplicationInputsMapper.class);

        ApplicationInputsMappers applicationInputsMappers = new ApplicationInputsMappers(List.of(mapper));

        Application application = new Application("someId", ZonedDateTime.now(), new ApplicationData(), County.OLMSTED);
        applicationInputsMappers.map(application, CASEWORKER);

        verify(mapper).map(application, CASEWORKER);
    }
}