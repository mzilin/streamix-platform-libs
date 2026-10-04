package com.mariuszilinskas.streamix.cryptography.config;

import com.mariuszilinskas.streamix.cryptography.kms.KmsFieldEncryptionServiceImpl;
import com.mariuszilinskas.streamix.cryptography.local.LocalKeyFieldEncryptionServiceImpl;
import com.mariuszilinskas.streamix.cryptography.service.FieldEncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.kms.KmsClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StreamixCryptographyAutoConfigurationTest {

    private static final String DUMMY_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
    private static final String DUMMY_KMS_KEY_ID = "arn:aws:kms:eu-west-1:123456789:key/test";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StreamixCryptographyAutoConfiguration.class));

    @Test
    void registersLocalKeyServiceWhenProviderIsLocal() {
        contextRunner
                .withPropertyValues(
                        "cryptography.provider=local",
                        "cryptography.encryption-key=" + DUMMY_KEY
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(FieldEncryptionService.class);
                    assertThat(context.getBean(FieldEncryptionService.class))
                            .isInstanceOf(LocalKeyFieldEncryptionServiceImpl.class);
                });
    }

    @Test
    void registersKmsServiceWhenProviderIsKms() {
        contextRunner
                .withPropertyValues(
                        "cryptography.provider=kms",
                        "cryptography.kms-key-id=" + DUMMY_KMS_KEY_ID,
                        "cryptography.aws-region=eu-west-1"
                )
                .withBean(KmsClient.class, () -> mock(KmsClient.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(FieldEncryptionService.class);
                    assertThat(context.getBean(FieldEncryptionService.class))
                            .isInstanceOf(KmsFieldEncryptionServiceImpl.class);
                });
    }

    @Test
    void doesNotRegisterServiceWhenProviderIsNotConfigured() {
        contextRunner
                .withPropertyValues(
                        "cryptography.encryption-key=" + DUMMY_KEY
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(FieldEncryptionService.class);
                });
    }

    @Test
    void doesNotRegisterServiceWhenProviderIsUnknown() {
        contextRunner
                .withPropertyValues("cryptography.provider=unknown")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(FieldEncryptionService.class);
                });
    }

}
