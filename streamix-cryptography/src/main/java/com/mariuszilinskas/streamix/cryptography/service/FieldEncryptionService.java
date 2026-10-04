package com.mariuszilinskas.streamix.cryptography.service;

public interface FieldEncryptionService {

    String encrypt(String plaintext);

    String decrypt(String ciphertext);

}
