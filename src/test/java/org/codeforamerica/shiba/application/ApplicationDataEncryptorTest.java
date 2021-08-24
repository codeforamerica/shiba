package org.codeforamerica.shiba.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.Subworkflow;
import org.codeforamerica.shiba.pages.data.Subworkflows;
import org.codeforamerica.shiba.testutilities.PageDataBuilder;
import org.codeforamerica.shiba.testutilities.PagesDataBuilder;
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
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder().build(List.of(
        new PageDataBuilder("personalInfo",
            Map.of("ssn", new ArrayList<>(Collections.singleton("123-45-6789"))))
    )));
    String encryptedApp = applicationDataEncryptor.encrypt(applicationData);
    assertThat(encryptedApp)
        .contains("\"pagesData\":{\"personalInfo\":{\"ssn\":{\"value\":[\"encryptedSsn\"]}}}");
  }

  @Test
  void encryptsApplicantSsnInLaterDocs() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder().build(List.of(
        new PageDataBuilder("matchInfo",
            Map.of("ssn", new ArrayList<>(Collections.singleton("123-45-6789"))))
    )));
    String encryptedApp = applicationDataEncryptor.encrypt(applicationData);
    assertThat(encryptedApp)
        .contains("\"pagesData\":{\"matchInfo\":{\"ssn\":{\"value\":[\"encryptedSsn\"]}}}");
  }

  @Test
  void doNotEncryptApplicantSsnWhenBlank() {
    ApplicationData applicationData = new ApplicationData();
    applicationData.setPagesData(new PagesDataBuilder().build(List.of(
        new PageDataBuilder("personalInfo",
            Map.of("ssn", new ArrayList<>(Collections.singleton(""))))))
    );
    String encryptedApp = applicationDataEncryptor.encrypt(applicationData);
    assertThat(encryptedApp)
        .contains("\"pagesData\":{\"personalInfo\":{\"ssn\":{\"value\":[\"\"]}}}");
  }

  @Test
  void encryptSsnForHouseholdMembersWhenFilledOut() {
    ApplicationDataEncryptor applicationDataEncryptor = new ApplicationDataEncryptor(objectMapper,
        stringEncryptor);
    ApplicationData applicationData = new ApplicationData();
    applicationData.setSubworkflows(new Subworkflows(Map.of("household", new Subworkflow(List.of(
        new PagesDataBuilder().build(List.of(new PageDataBuilder("householdMemberInfo",
            Map.of("ssn", new ArrayList<>(Collections.singleton("123-45-5678")))))),
        new PagesDataBuilder().build(List.of(new PageDataBuilder("householdMemberInfo",
            Map.of("ssn", new ArrayList<>(Collections.singleton(""))))))
    )))));
    String encryptedApp = applicationDataEncryptor.encrypt(applicationData);
    assertThat(encryptedApp).contains(
        "\"pagesData\":{\"householdMemberInfo\":{\"ssn\":{\"value\":[\"encryptedSsn\"]}}}");
    assertThat(encryptedApp)
        .contains("\"pagesData\":{\"householdMemberInfo\":{\"ssn\":{\"value\":[\"\"]}}}");
  }
}
