package org.codeforamerica.shiba;

import static java.util.Collections.emptyList;
import static org.codeforamerica.shiba.Program.CASH;
import static org.codeforamerica.shiba.Program.SNAP;
import static org.codeforamerica.shiba.testutilities.TestUtils.ADMIN_EMAIL;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.output.ApplicationFile;
import org.codeforamerica.shiba.output.FileDownloadController;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.documentfieldpreparers.DocumentFieldPreparers;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.codeforamerica.shiba.output.xml.XmlGenerator;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigurationTest {

  MockMvc mockMvc;
  XmlGenerator xmlGenerator = mock(XmlGenerator.class);
  PdfGenerator pdfGenerator = mock(PdfGenerator.class);
  Application application;
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

    ApplicationData applicationData = new ApplicationData();
    applicationData.setId("9870000123");
    applicationData.setPagesData(new PagesDataBuilder()
        .withPageData("contactInfo", Map.of(
            "email", List.of("test@example.com"),
            "phoneOrEmail", List.of("EMAIL")))
        .withPageData("employmentStatus", "areYouWorking", "true")
        .withPageData("choosePrograms", "programs", List.of(SNAP, CASH))
        .build());
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
        .build();
    doReturn(Application.builder()
        .id("9870000123")
        .completedAt(ZonedDateTime.now())
        .applicationData(applicationData)
        .county(null)
        .timeToComplete(null)
        .build()).when(applicationRepository).find(any());
    doReturn(emptyList()).when(preparers).prepareDocumentFields(any(), any(), any());
    doReturn("").when(fileNameGenerator).generatePdfFilename(any(), any());

//    application = Application.builder()
//        .applicationData(applicationData)
//        .id("9870000123")
//        .completedAt(ZonedDateTime.now()).build();
//
//    mockMvc = MockMvcBuilders.standaloneSetup(
//            new FileDownloadController(
//                xmlGenerator,
//                pdfGenerator,
//                applicationData,
//                applicationRepository
//            ))
//        .setViewResolvers(new InternalResourceViewResolver("", "suffix"))
//        .apply(springSecurity())
//        .build();
  }

  @Test
  void requiresAuthenticationAndAuthorizationOnDownloadByApplicationIdEndpoint() throws Exception {
    String applicationId = "9870000123";
    mockMvc.perform(get("/download/9870000123"))
        .andExpect(unauthenticated());

    mockMvc.perform(get("/download/9870000123")
            .with(oauth2Login().attributes(attrs -> attrs.put("email", "invalid@x.org"))))
        .andExpect(status().is4xxClientError());

    mockMvc.perform(get("/download/9870000123")
            .with(oauth2Login().attributes(attrs -> attrs.put("email", "invalid@codeforamerica.org"))))
        .andExpect(status().is4xxClientError());

    mockMvc.perform(get("/download/9870000123")
            .with(oauth2Login().attributes(attrs -> attrs.put("email", ADMIN_EMAIL))))
        .andExpect(authenticated())
        .andExpect(status().is2xxSuccessful())
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
        String.format("filename=\"%s\"", "MNB_application_" + applicationId + ".zip")));
  }

  @Test
  void doesNotRequireAuthenticationOnDownloadEndpoint() throws Exception {
    when(pdfGenerator.generate(anyString(), any(), any())).thenReturn(new ApplicationFile("TEST".getBytes(), ""));
//    when(applicationRepository.find(any())).thenReturn(application);
    //Check that download is set to unauthenticated
    mockMvc.perform(get("/download"))
        .andExpect(unauthenticated());
    //Check that download is allowed for the current application when the user is not authenticated
    mockMvc.perform(get("/download"))
        .andExpect(status().is2xxSuccessful());
//    verify(pdfGenerator).generateCoverPageForUploadedDocs(application);
//    verify(pdfGenerator)
//        .generateForUploadedDocument(uploadedDoc, 0, application, imageFile.getFileBytes());
//
//    byte[] actualBytes = result.getResponse().getContentAsByteArray();
//
//    assertThat(actualBytes).hasSizeGreaterThan(22);
  }
}
