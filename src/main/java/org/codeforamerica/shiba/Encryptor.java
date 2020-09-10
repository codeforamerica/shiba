package org.codeforamerica.shiba;

public interface Encryptor<T> {
    byte[] encrypt(T data);

    T decrypt(byte[] encryptedData);
}
