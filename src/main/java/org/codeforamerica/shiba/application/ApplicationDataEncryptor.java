package org.codeforamerica.shiba.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Function;

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
            ApplicationData applicationData = objectMapper.readValue(encryptedData, ApplicationData.class);
            runCryptographicFunctionOnData(stringEncryptor::decrypt, applicationData);
            return applicationData;
        } catch (IOException e) {
            log.error("Error while deserializing application data");
            throw new RuntimeException(e);
        }
    }

    private void runCryptographicFunctionOnData(Function<String, String> encryptFunc, ApplicationData applicationData) {
        String applicantSSN = applicationData.getPagesData().getPageInputFirstValue("personalInfo", "ssn");
        if (applicantSSN != null) {
            String encryptedApplicantSSN = encryptFunc.apply(applicantSSN);
            applicationData.getPagesData().getPage("personalInfo").get("ssn").setValue(encryptedApplicantSSN, 0);

            boolean hasHousehold = applicationData.getSubworkflows().containsKey("household");
            if (hasHousehold) {
                applicationData.getSubworkflows().get("household").forEach(iteration -> {
                    String houseHoldMemberSSN = iteration.getPagesData().getPageInputFirstValue("householdMemberInfo", "ssn");
                    if (houseHoldMemberSSN != null) {
                        String encryptedHouseholdMemberSSN = encryptFunc.apply(houseHoldMemberSSN);
                        iteration.getPagesData().getPage("householdMemberInfo").get("ssn").setValue(encryptedHouseholdMemberSSN, 0);
                    }
                });
            }
        }
    }

}
