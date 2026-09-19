package com.mariuszilinskas.streamix.cryptography.kms;

import com.mariuszilinskas.streamix.cryptography.exception.CryptographyException;
import com.mariuszilinskas.streamix.cryptography.service.FieldEncryptionService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptResponse;

import java.util.Base64;

public class KmsFieldEncryptionServiceImpl implements FieldEncryptionService {

    private final KmsClient kmsClient;
    private final String kmsKeyId;

    public KmsFieldEncryptionServiceImpl(KmsClient kmsClient, String kmsKeyId) {
        this.kmsClient = kmsClient;
        this.kmsKeyId = kmsKeyId;
    }

    @Override
    public String encrypt(String plaintext) {
        try {
            EncryptResponse response = kmsClient.encrypt(r -> r
                    .keyId(kmsKeyId)
                    .plaintext(SdkBytes.fromUtf8String(plaintext)));
            return Base64.getEncoder().encodeToString(response.ciphertextBlob().asByteArray());
        } catch (Exception e) {
            throw new CryptographyException("KMS encryption failed", e);
        }
    }

    @Override
    public String decrypt(String ciphertext) {
        try {
            DecryptResponse response = kmsClient.decrypt(r -> r
                    .keyId(kmsKeyId)
                    .ciphertextBlob(SdkBytes.fromByteArray(Base64.getDecoder().decode(ciphertext))));
            return response.plaintext().asUtf8String();
        } catch (Exception e) {
            throw new CryptographyException("KMS decryption failed", e);
        }
    }

}
