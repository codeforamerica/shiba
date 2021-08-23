package org.codeforamerica.shiba.application;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.jupiter.api.Test;

class StringEncryptorTest {

  @Test
  void shouldEncryptAndDecryptToSameValue() throws GeneralSecurityException, IOException {
    StringEncryptor stringEncryptor = new StringEncryptor(System.getenv("ENCRYPTION_KEY"));
    String hexEncodedEncrypted = stringEncryptor.encrypt("abc");
    String hexDecodedDecrypted = stringEncryptor.decrypt(hexEncodedEncrypted);
    assert (hexDecodedDecrypted.equals("abc"));
  }
}
