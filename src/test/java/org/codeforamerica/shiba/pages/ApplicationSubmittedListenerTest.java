package org.codeforamerica.shiba.pages;

import org.codeforamerica.shiba.Application;
import org.codeforamerica.shiba.ApplicationRepository;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.mockito.Mockito.*;

class ApplicationSubmittedListenerTest {
    MnitDocumentConsumer mnitDocumentConsumer = mock(MnitDocumentConsumer.class);
    ApplicationRepository applicationRepository = mock(ApplicationRepository.class);

    ApplicationSubmittedListener applicationSubmittedListener = new ApplicationSubmittedListener(
            mnitDocumentConsumer,
            applicationRepository
    );

    @Test
    @Disabled
    void shouldProcessSubmittedApplication() {
        String applicationId = "someId";
        Application application = new Application(applicationId, ZonedDateTime.now(), null, null);
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(application, applicationId);
        when(applicationRepository.find(applicationId)).thenReturn(application);

        applicationSubmittedListener.handleApplicationSubmittedEvent(event);

        verify(mnitDocumentConsumer).process(application);
    }
}