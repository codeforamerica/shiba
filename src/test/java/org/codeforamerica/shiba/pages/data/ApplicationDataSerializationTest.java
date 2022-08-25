package org.codeforamerica.shiba.pages.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.codeforamerica.shiba.output.Document.CCAP;
import static org.codeforamerica.shiba.output.Recipient.CASEWORKER;
import static org.codeforamerica.shiba.output.Recipient.CLIENT;
import static org.codeforamerica.shiba.testutilities.TestUtils.getFileContentsAsByteArray;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.documents.DocumentRepository;
import org.codeforamerica.shiba.output.caf.FilenameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = NONE)
@ActiveProfiles("test")
class ApplicationDataSerializationTest {

  @MockBean
  private ClientRegistrationRepository repository;
  @MockBean
  private ApplicationRepository applicationRepository;
  @MockBean
  private FilenameGenerator fileNameGenerator;
  @MockBean
  private DocumentRepository documentRepository;
  @Autowired
  private PdfGenerator pdfGenerator;

  private byte[] coverPage;

  private byte[] serializedApplicationDataFromOldSession;

  @BeforeEach
  void setUp() throws IOException {
    /*
     * This fixture is the value of spring_session_attributes.attribute_bytes for one session in the database.
     *
     * That session was created while running the code from this commit: 1088f79c08dab374ebe5d6fb8a113a5c779cf004
     *
     * The application corresponding to that session had made it all the way through the application process and
     * was in the process of uploading documents when its attribute_bytes were captured in this fixture
     */
    serializedApplicationDataFromOldSession = getFileContentsAsByteArray(
        "sessionApplicationDataFixture.txt");
    when(fileNameGenerator.generatePdfFilename(any(), any())).thenReturn("some-file.pdf");

    var image = getFileContentsAsByteArray("shiba+file.jpg");
    when(documentRepository.get(anyString())).thenReturn(image);
    coverPage = getFileContentsAsByteArray("shiba+file.pdf");
  }

  @Test
  void canDeserializeOldSessionsAndUseThemToGeneratePdfs()
      throws IOException, ClassNotFoundException {
    var deserializationResult = deserializeObjectFromByteArray(
        serializedApplicationDataFromOldSession);
    assertThat(deserializationResult).isInstanceOf(ApplicationData.class);

    var application = Application.builder()
        .id("some-id")
        .completedAt(ZonedDateTime.now())
        .applicationData((ApplicationData) deserializationResult)
        .county(Hennepin)
        .timeToComplete(Duration.ofSeconds(12415))
        .build();
    when(applicationRepository.find(anyString())).thenReturn(application);

    assertThatCode(() -> {
      pdfGenerator.generate(application, CAF, CASEWORKER);
      pdfGenerator.generate(application, CCAP, CLIENT);

      var uploadedDocument = application.getApplicationData().getUploadedDocs().get(0);
      pdfGenerator.generateForUploadedDocument(List.of(uploadedDocument), application, coverPage);
    }).doesNotThrowAnyException();
  }

  private Object deserializeObjectFromByteArray(byte[] b)
      throws IOException, ClassNotFoundException {
    try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(b))) {
      return objectInputStream.readObject();
    }
  }
}
