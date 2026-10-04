package com.mariuszilinskas.streamix.cryptography.kms;

import com.mariuszilinskas.streamix.cryptography.exception.CryptographyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.services.kms.model.KmsException;

import java.util.Base64;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KmsFieldEncryptionServiceImplTest {

    private static final String KMS_KEY_ID =
            "arn:aws:kms:eu-west-1:123456789:key/test";

    @Mock
    private KmsClient kmsClient;

    private KmsFieldEncryptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new KmsFieldEncryptionServiceImpl(kmsClient, KMS_KEY_ID);
    }

    @Test
    @SuppressWarnings("unchecked")
    void encrypt_returnsBase64EncodedCiphertext() {
        byte[] rawCiphertext = new byte[]{1, 2, 3, 4};

        EncryptResponse response = EncryptResponse.builder()
                .ciphertextBlob(SdkBytes.fromByteArray(rawCiphertext))
                .build();

        when(kmsClient.encrypt(any(Consumer.class))).thenReturn(response);

        String result = service.encrypt("hello");

        assertThat(result)
                .isEqualTo(Base64.getEncoder().encodeToString(rawCiphertext));
    }

    @Test
    @SuppressWarnings("unchecked")
    void decrypt_returnsPlaintext() {
        String expected = "hello";

        DecryptResponse response = DecryptResponse.builder()
                .plaintext(SdkBytes.fromUtf8String(expected))
                .build();

        when(kmsClient.decrypt(any(Consumer.class))).thenReturn(response);

        String ciphertext = Base64.getEncoder()
                .encodeToString(new byte[]{1, 2, 3, 4});

        assertThat(service.decrypt(ciphertext))
                .isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    void encrypt_whenKmsThrows_throwsCryptographyException() {
        when(kmsClient.encrypt(any(Consumer.class)))
                .thenThrow(KmsException.builder().message("KMS unavailable").build());

        assertThatThrownBy(() -> service.encrypt("hello"))
                .isInstanceOf(CryptographyException.class)
                .hasMessage("KMS encryption failed");
    }

    @Test
    @SuppressWarnings("unchecked")
    void decrypt_whenKmsThrows_throwsCryptographyException() {
        when(kmsClient.decrypt(any(Consumer.class)))
                .thenThrow(KmsException.builder().message("KMS unavailable").build());

        String ciphertext = Base64.getEncoder()
                .encodeToString(new byte[]{1, 2, 3, 4});

        assertThatThrownBy(() -> service.decrypt(ciphertext))
                .isInstanceOf(CryptographyException.class)
                .hasMessage("KMS decryption failed");
    }

    @Test
    void decrypt_whenCiphertextIsInvalidBase64_throwsCryptographyException() {
        assertThatThrownBy(() -> service.decrypt("not-valid-base64!!!"))
                .isInstanceOf(CryptographyException.class)
                .hasMessage("Invalid Base64 ciphertext");
    }

    @Test
    void encrypt_whenPlaintextIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.encrypt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("plaintext must not be null");
    }

    @Test
    void decrypt_whenCiphertextIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.decrypt(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ciphertext must not be null or blank");
    }

    @Test
    void decrypt_whenCiphertextIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> service.decrypt(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ciphertext must not be null or blank");
    }

    @Test
    void constructor_whenKmsClientIsNull_throwsNullPointerException() {
        assertThatThrownBy(() -> new KmsFieldEncryptionServiceImpl(null, KMS_KEY_ID))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("kmsClient must not be null");
    }

    @Test
    void constructor_whenKmsKeyIdIsNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new KmsFieldEncryptionServiceImpl(kmsClient, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("kmsKeyId must not be null or blank");
    }

    @Test
    void constructor_whenKmsKeyIdIsBlank_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new KmsFieldEncryptionServiceImpl(kmsClient, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("kmsKeyId must not be null or blank");
    }
}