package com.mariuszilinskas.streamix.cryptography.config;

import com.mariuszilinskas.streamix.cryptography.kms.KmsFieldEncryptionServiceImpl;
import com.mariuszilinskas.streamix.cryptography.local.LocalKeyFieldEncryptionServiceImpl;
import com.mariuszilinskas.streamix.cryptography.properties.CryptographyProperties;
import com.mariuszilinskas.streamix.cryptography.service.FieldEncryptionService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

@AutoConfiguration
@EnableConfigurationProperties(CryptographyProperties.class)
public class StreamixCryptographyAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = "cryptography",
            name = "provider",
            havingValue = "local"
    )
    static class LocalKeyAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(FieldEncryptionService.class)
        public FieldEncryptionService encryptionService(
                CryptographyProperties properties
        ) {
            return new LocalKeyFieldEncryptionServiceImpl(
                    properties.encryptionKey()
            );
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(KmsClient.class)
    @ConditionalOnProperty(
            prefix = "cryptography",
            name = "provider",
            havingValue = "kms"
    )
    static class KmsAutoConfiguration {

        @Bean
        @ConditionalOnMissingBean(KmsClient.class)
        public KmsClient kmsClient(CryptographyProperties properties) {
            return KmsClient.builder()
                    .region(Region.of(properties.awsRegion()))
                    .build();
        }

        @Bean
        @ConditionalOnMissingBean(FieldEncryptionService.class)
        public FieldEncryptionService encryptionService(
                KmsClient kmsClient,
                CryptographyProperties properties
        ) {
            return new KmsFieldEncryptionServiceImpl(
                    kmsClient,
                    properties.kmsKeyId()
            );
        }
    }
}
