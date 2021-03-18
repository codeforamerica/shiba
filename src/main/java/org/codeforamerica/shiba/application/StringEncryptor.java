package org.codeforamerica.shiba.application;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.config.TinkConfig;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Component
public class StringEncryptor implements Encryptor<String> {
    private final Aead aead;

    public StringEncryptor(@Value("${encryption-key}") String encryptionKey) throws GeneralSecurityException, IOException {
        TinkConfig.register();
        aead = CleartextKeysetHandle.read(
                JsonKeysetReader.withString(encryptionKey)).getPrimitive(Aead.class);
    }


    public byte[] encrypt(String data) {
        try {
            return aead.encrypt(data.getBytes(), null);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public String hexEncodeEncrypt(String data) {
        try {
            return String.valueOf(Hex.encodeHex(aead.encrypt(data.getBytes(), null)));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public String hexDecodeDecrypt(String encryptedData) {
        try {
            return new String(aead.decrypt(Hex.decodeHex(encryptedData.toCharArray()), null));
        } catch (GeneralSecurityException | DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(byte[] encryptedData) {
        try {
            return new String(aead.decrypt(encryptedData, null));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
