package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationInputsMappersTest {
    private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    private final ApplicationInputsMappers mappers = new ApplicationInputsMappers(applicationRepository, List.of());

    @BeforeEach
    void setUp() {
        when(applicationRepository.find(any()))
                .thenReturn(new Application("defaultId", ZonedDateTime.now(ZoneOffset.UTC), new ApplicationData()));
    }

    @Test
    void shouldIncludeApplicationIdInput() {
        List<ApplicationInput> applicationInputs = mappers.map("someId");

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "applicationId", List.of("someId"), SINGLE_VALUE));
    }

    @Test
    void shouldIncludeCompletedDateInput() {
        String applicationId = "someId";
        ZonedDateTime completedAt = ZonedDateTime.of(
                LocalDateTime.of(2014, 1, 16, 1, 2, 3),
                ZoneOffset.UTC);
        when(applicationRepository.find(applicationId))
                .thenReturn(new Application(applicationId, completedAt, new ApplicationData()));

        List<ApplicationInput> applicationInputs = mappers.map(applicationId);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "completedDate", List.of("2014-01-16"), SINGLE_VALUE));
    }

    @Test
    void shouldIncludeCompletedDateTimeInput() {
        String applicationId = "someId";
        ZonedDateTime completedAt = ZonedDateTime.of(LocalDateTime.of(2019, 11, 16, 5, 29, 1), ZoneOffset.UTC);
        when(applicationRepository.find(applicationId))
                .thenReturn(new Application(applicationId, completedAt, new ApplicationData()));

        List<ApplicationInput> applicationInputs = mappers.map("someId");

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "completedDateTime", List.of("2019-11-16T05:29:01Z"), SINGLE_VALUE));
    }
}