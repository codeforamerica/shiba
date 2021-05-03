package org.codeforamerica.shiba.output.applicationinputsmappers;

import org.codeforamerica.shiba.County;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.output.ApplicationInput;
import org.codeforamerica.shiba.output.Document;
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
import static org.mockito.Mockito.*;

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
                .build(), null, CLIENT);

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

        List<ApplicationInput> applicationInputs = mappers.map(application, null, CASEWORKER);

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

        List<ApplicationInput> applicationInputs = mappers.map(application, null, CASEWORKER);

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

        List<ApplicationInput> applicationInputs = mappers.map(application, null, CASEWORKER);

        assertThat(applicationInputs).contains(new ApplicationInput("nonPagesData", "submissionDateTime", List.of("09/03/2020 at 01:02 AM"), SINGLE_VALUE));
    }

    @Test
    void shouldUseMatchingRecipientAndDocumentForMappers() {
        ApplicationInputsMapper mapper = mock(ApplicationInputsMapper.class);

        ApplicationInputsMappers applicationInputsMappers = new ApplicationInputsMappers(List.of(mapper));

        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();
        applicationInputsMappers.map(application, Document.CAF, CASEWORKER);

        verify(mapper).map(eq(application), eq(Document.CAF), eq(CASEWORKER), any());
    }

    @Test
    void shouldStillSuccessfullyMapEvenWithExceptionsInIndividualMappers() {
        ApplicationInputsMapper successfulMapper = mock(ApplicationInputsMapper.class);
        ApplicationInputsMapper failingMapper = mock(ApplicationInputsMapper.class);
        ApplicationInputsMappers applicationInputsMappers = new ApplicationInputsMappers(List.of(failingMapper, successfulMapper));
        Application application = Application.builder()
                .id("someId")
                .completedAt(ZonedDateTime.now())
                .applicationData(new ApplicationData())
                .county(County.Olmsted)
                .timeToComplete(null)
                .build();

        List<ApplicationInput> mockOutput = List.of(new ApplicationInput("group", "name", List.of("value"), null));
        when(successfulMapper.map(eq(application), eq(Document.CAF), eq(CASEWORKER), any())).thenReturn(mockOutput);
        when(failingMapper.map(eq(application), eq(Document.CAF), eq(CASEWORKER), any())).thenThrow(IllegalArgumentException.class);

        List<ApplicationInput> actualOutput = applicationInputsMappers.map(application, Document.CAF, CASEWORKER);
        assertThat(actualOutput).isNotEmpty();
        verify(successfulMapper).map(eq(application), eq(Document.CAF), eq(CASEWORKER), any());
        verify(failingMapper).map(eq(application), eq(Document.CAF), eq(CASEWORKER), any());
    }
}