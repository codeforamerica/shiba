package org.codeforamerica.shiba;

import static java.util.Collections.emptyList;
import static org.codeforamerica.shiba.testutilities.TestUtils.ADMIN_EMAIL;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparers;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigurationTest {

  MockMvc mockMvc;

  @Autowired
  WebApplicationContext webApplicationContext;

  @MockBean
  DocumentFieldPreparers preparers;

  @MockBean
  ApplicationRepository applicationRepository;

  @MockBean
  FilenameGenerator fileNameGenerator;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
        .build();
    doReturn(Application.builder()
        .id("foo")
        .completedAt(ZonedDateTime.now())
        .applicationData(new ApplicationData())
        .county(null)
        .timeToComplete(null)
        .build()).when(applicationRepository).find(any());
    doReturn(emptyList()).when(preparers).prepareDocumentFields(any(), any(), any());
    doReturn("").when(fileNameGenerator).generatePdfFilename(any(), any());
  }

  @Test
  void requiresAuthenticationAndAuthorizationOnDownloadCafEndpoint() throws Exception {
    mockMvc.perform(get("/download-caf/9870000123"))
        .andExpect(unauthenticated());

    mockMvc.perform(get("/download-caf/9870000123")
            .with(oauth2Login().attributes(attrs -> attrs.put("email", "invalid@x.org"))))
        .andExpect(status().is4xxClientError());

    mockMvc.perform(get("/download-caf/9870000123")
            .with(oauth2Login().attributes(attrs -> attrs.put("email", "invalid@codeforamerica.org"))))
        .andExpect(status().is4xxClientError());

    mockMvc.perform(get("/download-caf/9870000123")
            .with(oauth2Login().attributes(attrs -> attrs.put("email", ADMIN_EMAIL))))
        .andExpect(authenticated())
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void doesNotRequireAuthenticationOnAnyOtherEndpoint() throws Exception {
    mockMvc.perform(get("/download"))
        .andExpect(status().is2xxSuccessful());
  }
}
