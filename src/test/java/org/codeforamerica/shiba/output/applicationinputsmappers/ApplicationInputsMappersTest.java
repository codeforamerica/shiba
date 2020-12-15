package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codeforamerica.shiba.output.ApplicationInputType.SINGLE_VALUE;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApplicationInputsMappersTest {
    private final ApplicationInputsMappers mappers = new ApplicationInputsMappers(List.of());

    @Test
    void shouldIncludeApplicationIdInput() {
        List<ApplicationInput> applicationInputs = mappers.map(Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.Olmsted)
                .timeToComplete(null)
                .build(), CLIENT);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "applicationId", List.of("someId"), SINGLE_VALUE));
    }

    @Test
    void shouldIncludeCompletedDateInput() {
        String applicationId = "someId";
        ZonedDateTime completedAt = ZonedDateTime.of(
                LocalDateTime.of(2020, 9, 3, 1, 2, 3),
                ZoneOffset.UTC);
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(completedAt)
                .applicationData(new ApplicationData())
                .county(null)
                .timeToComplete(null)
                .build();

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "completedDate", List.of("2020-09-02"), SINGLE_VALUE));
    }

    @Test
    void shouldIncludeCompletedDateTimeInput() {
        String applicationId = "someId";
        ZonedDateTime completedAt = ZonedDateTime.of(LocalDateTime.of(2019, 11, 16, 5, 29, 1), ZoneOffset.UTC);
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(completedAt)
                .applicationData(new ApplicationData())
                .county(null)
                .timeToComplete(null)
                .build();

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "completedDateTime", List.of("2019-11-16T05:29:01Z"), SINGLE_VALUE));
    }

    @Test
    void shouldIncludeSubmissionDateTimeInput() {
        String applicationId = "someId";
        ZonedDateTime completedAt = ZonedDateTime.of(
                LocalDateTime.of(2020, 9, 3, 1, 2, 3),
                ZoneId.of("America/Chicago"));
        Application application = Application.builder()
                .id(applicationId)
                .completedAt(completedAt)
                .applicationData(new ApplicationData())
                .county(null)
                .timeToComplete(null)
                .build();

        List<ApplicationInput> applicationInputs = mappers.map(application, CASEWORKER);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "submissionDateTime", List.of("09/03/2020 at 01:02 AM"), SINGLE_VALUE));
    }

    @Test
    void shouldUseMatchingRecipientForMappers() {
        ApplicationInputsMapper mapper = mock(ApplicationInputsMapper.class);

        ApplicationInputsMappers applicationInputsMappers = new ApplicationInputsMappers(List.of(mapper));

        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        applicationInputsMappers.map(application, CASEWORKER);

        verify(mapper).map(eq(application), eq(CASEWORKER), any());
    }
}