package org.codeforamerica.shiba.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
import org.codeforamerica.shiba.testutilities.TestApplicationDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

@JsonTest
class ApplicationDataEncryptorTest {

  @SpyBean
  private ObjectMapper objectMapper;

  @MockBean
  private StringEncryptor stringEncryptor;
  private ApplicationDataEncryptor applicationDataEncryptor;

  @BeforeEach
  void setUp() {
    when(stringEncryptor.encrypt(any())).thenReturn("encryptedSsn").thenReturn("otherSsn");
    applicationDataEncryptor = new ApplicationDataEncryptor(objectMapper, stringEncryptor);
  }

  @Test
  void itGracefullyHandlesLegacyUploadedDocumentsMap() throws IOException {
    String appDataWithLegacyUploadedDocumentsMap = """
                {"id":null,"startTime":null,"flow":"UNDETERMINED","pagesData":{},"uploadedDocuments":{},
                "subworkflows":{},"incompleteIterations":{},"ccapapplication":false,"cafapplication":false}
        """;
    when(objectMapper.writeValueAsString(any())).thenReturn(appDataWithLegacyUploadedDocumentsMap);

    assertDoesNotThrow(() -> {
      String encryptedApp = applicationDataEncryptor.encrypt(new ApplicationData());
      applicationDataEncryptor.decrypt(encryptedApp);
    });
  }

  @Test
  void encryptsApplicantSsn() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "ssn", "123-45-6789")
        .build();
    String encryptedApp = applicationDataEncryptor.encrypt(applicationData);
    assertThat(encryptedApp)
        .contains("\"pagesData\":{\"personalInfo\":{\"ssn\":{\"value\":[\"encryptedSsn\"]}}}");
  }

  @Test
  void encryptsApplicantSsnInLaterDocs() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("matchInfo", "ssn", "123-45-6789")
        .build();
    String encryptedApp = applicationDataEncryptor.encrypt(applicationData);
    assertThat(encryptedApp)
        .contains("\"pagesData\":{\"matchInfo\":{\"ssn\":{\"value\":[\"encryptedSsn\"]}}}");
  }

  @Test
  void doNotEncryptApplicantSsnWhenBlank() {
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withPageData("personalInfo", "ssn", "")
        .build();
    String encryptedApp = applicationDataEncryptor.encrypt(applicationData);
    assertThat(encryptedApp)
        .contains("\"pagesData\":{\"personalInfo\":{\"ssn\":{\"value\":[\"\"]}}}");
  }

  @Test
  void encryptSsnForHouseholdMembersWhenFilledOut() {
    ApplicationDataEncryptor applicationDataEncryptor = new ApplicationDataEncryptor(objectMapper,
        stringEncryptor);
    ApplicationData applicationData = new TestApplicationDataBuilder()
        .withSubworkflow("household",
            new PagesDataBuilder()
                .withPageData("householdMemberInfo", "ssn", "123-45-5678").build(),
            new PagesDataBuilder()
                .withPageData("householdMemberInfo", "ssn", "").build())
        .build();
    String encryptedApp = applicationDataEncryptor.encrypt(applicationData);
    assertThat(encryptedApp).contains(
        "\"pagesData\":{\"householdMemberInfo\":{\"ssn\":{\"value\":[\"encryptedSsn\"]}}}");
    assertThat(encryptedApp)
        .contains("\"pagesData\":{\"householdMemberInfo\":{\"ssn\":{\"value\":[\"\"]}}}");
  }
}
