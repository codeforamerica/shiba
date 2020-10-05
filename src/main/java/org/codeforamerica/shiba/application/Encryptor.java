package org.codeforamerica.shiba.application;

public interface Encryptor<T> {
    byte[] encrypt(T data);

    T decrypt(byte[] encryptedData);
}
