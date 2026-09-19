package com.mariuszilinskas.streamix.cryptography.local;

import com.mariuszilinskas.streamix.cryptography.exception.CryptographyException;
import com.mariuszilinskas.streamix.cryptography.service.FieldEncryptionService;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

public final class LocalKeyFieldEncryptionServiceImpl implements FieldEncryptionService {

    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom;

    public LocalKeyFieldEncryptionServiceImpl(String base64EncodedKey) {
        this.secretKey = createSecretKey(base64EncodedKey);
        this.secureRandom = new SecureRandom();
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext must not be null");
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            );

            byte[] ciphertext = cipher.doFinal(
                    plaintext.getBytes(StandardCharsets.UTF_8)
            );

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (GeneralSecurityException e) {
            throw new CryptographyException("AES-GCM encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isBlank()) {
            throw new IllegalArgumentException("encrypted value must not be null or blank");
        }

        try {
            byte[] combined = Base64.getDecoder().decode(encrypted);

            if (combined.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException("Invalid encrypted value");
            }

            byte[] iv = new byte[IV_LENGTH_BYTES];
            byte[] ciphertext = new byte[combined.length - IV_LENGTH_BYTES];

            System.arraycopy(combined, 0, iv, 0, IV_LENGTH_BYTES);
            System.arraycopy(
                    combined,
                    IV_LENGTH_BYTES,
                    ciphertext,
                    0,
                    ciphertext.length
            );

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            );

            return new String(
                    cipher.doFinal(ciphertext),
                    StandardCharsets.UTF_8
            );

        } catch (IllegalArgumentException e) {
            throw new CryptographyException("Invalid encrypted value", e);
        } catch (GeneralSecurityException e) {
            throw new CryptographyException("AES-GCM decryption failed", e);
        }
    }

    private static SecretKeySpec createSecretKey(String base64EncodedKey) {
        if (base64EncodedKey == null || base64EncodedKey.isBlank()) {
            throw new IllegalArgumentException("Encryption key must not be null or blank");
        }

        final byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(base64EncodedKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Encryption key must be valid Base64", e);
        }

        if (keyBytes.length != 16
                && keyBytes.length != 24
                && keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Encryption key must be 128, 192, or 256 bits"
            );
        }

        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }
}