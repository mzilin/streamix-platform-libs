package com.mariuszilinskas.streamix.cryptography.local;

import com.mariuszilinskas.streamix.cryptography.exception.CryptographyException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalKeyFieldEncryptionServiceImplTest {

    private static final String VALID_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private static final String OTHER_KEY = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private final LocalKeyFieldEncryptionServiceImpl service =
            new LocalKeyFieldEncryptionServiceImpl(VALID_KEY);

    @Test
    void encryptThenDecrypt_returnsOriginalPlaintext() {
        String plaintext = "my-secret-password";

        assertThat(service.decrypt(service.encrypt(plaintext)))
                .isEqualTo(plaintext);
    }

    @Test
    void encrypt_producesDifferentCiphertextEachCall() {
        String plaintext = "my-secret-password";

        assertThat(service.encrypt(plaintext))
                .isNotEqualTo(service.encrypt(plaintext));
    }

    @Test
    void decrypt_withInvalidBase64_throwsCryptographyException() {
        assertThatThrownBy(() -> service.decrypt("not-valid-base64!!!"))
                .isInstanceOf(CryptographyException.class)
                .hasMessage("Invalid Base64 ciphertext");
    }

    @Test
    void decrypt_withInvalidEncryptedValue_throwsCryptographyException() {
        String invalidEncryptedValue = Base64.getEncoder()
                .encodeToString(new byte[12]);

        assertThatThrownBy(() -> service.decrypt(invalidEncryptedValue))
                .isInstanceOf(CryptographyException.class)
                .hasMessage("Invalid encrypted value");
    }

    @Test
    void constructor_withInvalidKey_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new LocalKeyFieldEncryptionServiceImpl("not-a-key"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Encryption key must be valid Base64");
    }

    @Test
    void constructor_withInvalidKeyLength_throwsIllegalArgumentException() {
        String invalidKey = Base64.getEncoder()
                .encodeToString(new byte[10]);

        assertThatThrownBy(() -> new LocalKeyFieldEncryptionServiceImpl(invalidKey))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Encryption key must be 128, 192, or 256 bits");
    }

    @Test
    void decrypt_withWrongKey_throwsCryptographyException() {
        String ciphertext = service.encrypt("hello");
        LocalKeyFieldEncryptionServiceImpl wrongKeyService =
                new LocalKeyFieldEncryptionServiceImpl(OTHER_KEY);

        assertThatThrownBy(() -> wrongKeyService.decrypt(ciphertext))
                .isInstanceOf(CryptographyException.class);
    }
}