package com.mariuszilinskas.streamix.cryptography.kms;

import com.mariuszilinskas.streamix.cryptography.exception.CryptographyException;
import com.mariuszilinskas.streamix.cryptography.service.FieldEncryptionService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.services.kms.model.KmsException;

import java.util.Base64;
import java.util.Objects;

public final class KmsFieldEncryptionServiceImpl implements FieldEncryptionService {

    private final KmsClient kmsClient;
    private final String kmsKeyId;

    public KmsFieldEncryptionServiceImpl(KmsClient kmsClient, String kmsKeyId) {
        this.kmsClient = Objects.requireNonNull(kmsClient, "kmsClient must not be null");

        if (kmsKeyId == null || kmsKeyId.isBlank()) {
            throw new IllegalArgumentException("kmsKeyId must not be null or blank");
        }

        this.kmsKeyId = kmsKeyId;
    }

    @Override
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext must not be null");
        }

        try {
            EncryptResponse response = kmsClient.encrypt(request -> request
                    .keyId(kmsKeyId)
                    .plaintext(SdkBytes.fromUtf8String(plaintext)));

            return Base64.getEncoder().encodeToString(response.ciphertextBlob().asByteArray());

        } catch (KmsException e) {
            throw new CryptographyException("KMS encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            throw new IllegalArgumentException("ciphertext must not be null or blank");
        }

        final byte[] ciphertextBytes;

        try {
            ciphertextBytes = Base64.getDecoder().decode(ciphertext);
        } catch (IllegalArgumentException e) {
            throw new CryptographyException("Invalid Base64 ciphertext", e);
        }

        try {
            DecryptResponse response = kmsClient.decrypt(request -> request
                    .keyId(kmsKeyId)
                    .ciphertextBlob(SdkBytes.fromByteArray(ciphertextBytes)));

            return response.plaintext().asUtf8String();

        } catch (KmsException e) {
            throw new CryptographyException("KMS decryption failed", e);
        }
    }
}