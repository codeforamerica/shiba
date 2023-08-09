package org.codeforamerica.shiba.application;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.subtle.Hex;

import java.io.IOException;
import java.security.GeneralSecurityException;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StringEncryptor implements Encryptor<String> {

  private final Aead aead;

  public StringEncryptor(@Value("${encryption-key}") String encryptionKey)
      throws GeneralSecurityException, IOException {
    AeadConfig.register();
    aead = CleartextKeysetHandle.read(
        JsonKeysetReader.withString(encryptionKey)).getPrimitive(Aead.class);
  }

  public String encrypt(String data) {
    try {
      return new String(Hex.encode(aead.encrypt(data.getBytes(), null)));
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    }
  }

  public String decrypt(String encryptedData) {
   	byte[] decodedString = Hex.decode(encryptedData);
   	try {
		return new String(aead.decrypt(decodedString, null));
	} catch (GeneralSecurityException e) {
		throw new RuntimeException(e);
	}
  }
}
