package org.codeforamerica.shiba.pages.data;

import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.output.Recipient;
import org.codeforamerica.shiba.output.caf.FileNameGenerator;
import org.codeforamerica.shiba.output.pdf.PdfGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.codeforamerica.shiba.County.Hennepin;
import static org.codeforamerica.shiba.TestUtils.getAbsoluteFilepath;
import static org.codeforamerica.shiba.output.Document.CAF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = NONE, properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
@ActiveProfiles("test")
class ApplicationDataSerializationTest {
    private byte[] serializedApplicationDataFromOldSession;

    @MockBean
    private ApplicationRepository applicationRepository;

    @MockBean
    private FileNameGenerator fileNameGenerator;

    @MockBean
    private DocumentRepositoryService documentRepositoryService;

    @Autowired
    private PdfGenerator pdfGenerator;

    private byte[] coverPage;

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
        serializedApplicationDataFromOldSession = Files.readAllBytes(getAbsoluteFilepath("sessionApplicationDataFixture.txt"));
        when(fileNameGenerator.generatePdfFileName(any(), any())).thenReturn("some-file.pdf");

        var image = Files.readAllBytes(getAbsoluteFilepath("shiba+file.jpg"));
        when(documentRepositoryService.get(anyString())).thenReturn(image);
        coverPage = Files.readAllBytes(getAbsoluteFilepath("shiba+file.pdf"));
    }

    @Test
    void canDeserializeOldSessionsAndUseThemToGeneratePdfs() throws IOException, ClassNotFoundException {
        var deserializationResult = deserializeObjectFromByteArray(serializedApplicationDataFromOldSession);
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
            pdfGenerator.generate(application, CAF, Recipient.CASEWORKER);

            var uploadedDocument = application.getApplicationData().getUploadedDocs().get(0);
            pdfGenerator.generateForUploadedDocument(uploadedDocument, 0, application, coverPage);
        }).doesNotThrowAnyException();
    }

    private Object deserializeObjectFromByteArray(byte[] b) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return objectInputStream.readObject();
        }
    }
}
