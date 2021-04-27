package org.codeforamerica.shiba.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@JsonTest
class ApplicationDataEncryptorTest {
    @SpyBean
    private ObjectMapper objectMapper;

    @Test
    void itGracefullyHandlesLegacyUploadedDocumentsMap() throws IOException, GeneralSecurityException {
        String appDataWithLegacyUploadedDocumentsMap = "{\"id\":null,\"startTime\":null,\"flow\":\"UNDETERMINED\",\"pagesData\":{},\"uploadedDocuments\":{},\"subworkflows\":{},\"incompleteIterations\":{},\"ccapapplication\":false,\"cafapplication\":false}";
        when(objectMapper.writeValueAsString(any())).thenReturn(appDataWithLegacyUploadedDocumentsMap);
        StringEncryptor stringEncryptor = new StringEncryptor(System.getenv("ENCRYPTION_KEY"));

        assertDoesNotThrow(() -> {
            ApplicationDataEncryptor applicationDataEncryptor = new ApplicationDataEncryptor(objectMapper, stringEncryptor);
            String encryptedApp = applicationDataEncryptor.encrypt(new ApplicationData());
            applicationDataEncryptor.decrypt(encryptedApp);
        });
    }
}