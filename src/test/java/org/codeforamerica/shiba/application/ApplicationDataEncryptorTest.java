package org.codeforamerica.shiba.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class ApplicationDataEncryptorTest {
    @Autowired
    ApplicationDataEncryptor applicationDataEncryptor;

    @SpyBean
    ObjectMapper objectMapper;

    @Test
    void itGracefullyHandlesLegacyUploadedDocumentsMap() throws JsonProcessingException {
        String appDataWithLegacyUploadedDocumentsMap = "{\"id\":null,\"startTime\":null,\"flow\":\"UNDETERMINED\",\"pagesData\":{},\"uploadedDocuments\":{},\"subworkflows\":{},\"incompleteIterations\":{},\"ccapapplication\":false,\"cafapplication\":false}";
        when(objectMapper.writeValueAsString(any())).thenReturn(appDataWithLegacyUploadedDocumentsMap);

        assertDoesNotThrow(() -> {
            byte[] encryptedApp = applicationDataEncryptor.encrypt(new ApplicationData());
            applicationDataEncryptor.decrypt(encryptedApp);
        });
    }
}