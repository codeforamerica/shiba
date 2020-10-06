package org.codeforamerica.shiba;

import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import java.util.Map;

import static org.mockito.Mockito.*;

class SessionListenerTest {
    ResearchDataRepository researchDataRepository = mock(ResearchDataRepository.class);
    ResearchDataParser researchDataParser = mock(ResearchDataParser.class);
    SessionListener sessionListener = new SessionListener(researchDataRepository, researchDataParser);
    HttpSessionEvent mockSessionEvent = mock(HttpSessionEvent.class);
    ApplicationData applicationData = new ApplicationData();

    @BeforeEach
    void setUp() {
        HttpSession session = new MockHttpSession();
        session.setAttribute("scopedTarget.applicationData", applicationData);
        when(mockSessionEvent.getSession()).thenReturn(session);
    }

    @Test
    void shouldSaveResearchData() {
        applicationData.setPagesData(new PagesData(Map.of("somePage", new PageData())));
        ResearchData researchData = ResearchData.builder().build();
        when(researchDataParser.parse(applicationData)).thenReturn(researchData);

        sessionListener.sessionDestroyed(mockSessionEvent);

        verify(researchDataRepository).save(researchData);
    }

    @Test
    void shouldNotSaveResearchDataWhenPagesDataIsEmpty() {
        sessionListener.sessionDestroyed(mockSessionEvent);

        verifyNoInteractions(researchDataRepository);
    }

    @Test
    void shouldNotSaveResearchDataWhenApplicationDataHasNotBeenInitialized() {
        HttpSession session = new MockHttpSession();
        when(mockSessionEvent.getSession()).thenReturn(session);
        sessionListener.sessionDestroyed(mockSessionEvent);

        verifyNoInteractions(researchDataRepository);
    }
}