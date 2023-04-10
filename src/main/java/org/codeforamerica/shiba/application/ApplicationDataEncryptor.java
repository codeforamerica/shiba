package org.codeforamerica.shiba.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.InputData;
import org.codeforamerica.shiba.pages.data.PageData;
import org.codeforamerica.shiba.pages.data.PagesData;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationDataEncryptor implements Encryptor<ApplicationData> {

  private final ObjectMapper objectMapper;
  private final Encryptor<String> stringEncryptor;

  public ApplicationDataEncryptor(
      ObjectMapper objectMapper,
      Encryptor<String> stringEncryptor) {
    this.objectMapper = objectMapper;
    this.stringEncryptor = stringEncryptor;
  }

  @Override
  public String encrypt(ApplicationData applicationData) {
    try {
      runCryptographicFunctionOnData(stringEncryptor::encrypt, applicationData);
      return objectMapper.writeValueAsString(applicationData);
    } catch (JsonProcessingException e) {
      log.error("Unable to encrypt application data: applicationID=" + applicationData.getId());
      throw new RuntimeException(e);
    }
  }

  @Override
  public ApplicationData decrypt(String encryptedData) {
    try {
      ApplicationData applicationData = objectMapper
          .readValue(encryptedData, ApplicationData.class);
      runCryptographicFunctionOnData(stringEncryptor::decrypt, applicationData);
      return applicationData;
    } catch (IOException e) {
      log.error("Error while deserializing application data");
      throw new RuntimeException(e);
    }
  }

  private void runCryptographicFunctionOnData(Function<String, String> encryptFunc,
      ApplicationData applicationData) {
    PagesData pagesData = applicationData.getPagesData();

    setEncryptedSSNValue(encryptFunc, pagesData, "personalInfo");
    setEncryptedSSNValue(encryptFunc, pagesData, "matchInfo");
    setEncryptedSSNValue(encryptFunc, pagesData, "healthcareRenewalMatchInfo");

    boolean hasHousehold = applicationData.getSubworkflows().containsKey("household");
    if (hasHousehold) {
      applicationData.getSubworkflows().get("household")
          .forEach(iteration -> setEncryptedSSNValue(encryptFunc, iteration.getPagesData(),
              "householdMemberInfo"));
    }
  }

  /**
   * Run encrypt function on value and replace PageData with the resulting value.
   *
   * @param encryptFunc encrypt or decrypt
   * @param pagesData   pages data to get ssn
   * @param pageName    page to replace
   */
  private void setEncryptedSSNValue(Function<String, String> encryptFunc, PagesData pagesData,
      String pageName) {
    String applicantSSN = pagesData.getPageInputFirstValue(pageName, "ssn");
    if (applicantSSN != null && !applicantSSN.isBlank()) {
      String encryptedApplicantSSN = encryptFunc.apply(applicantSSN);
      PageData encryptedPage = new PageData();
      encryptedPage.putAll(pagesData.getPage(pageName));
      encryptedPage.put("ssn", new InputData(List.of(encryptedApplicantSSN)));
      pagesData.putPage(pageName, encryptedPage);
    }
  }

}
