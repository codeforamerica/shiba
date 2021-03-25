package org.codeforamerica.shiba.application;

public interface Encryptor<T> {
    String encrypt(T data);

    T decrypt(String encryptedData);
}
