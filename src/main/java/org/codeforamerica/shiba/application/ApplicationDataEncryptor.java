package org.codeforamerica.shiba.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codeforamerica.shiba.Encryptor;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
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
    public byte[] encrypt(ApplicationData data) {
        try {
            return stringEncryptor.encrypt(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ApplicationData decrypt(byte[] encryptedData) {
        try {
            return objectMapper.readValue(stringEncryptor.decrypt(encryptedData), ApplicationData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
