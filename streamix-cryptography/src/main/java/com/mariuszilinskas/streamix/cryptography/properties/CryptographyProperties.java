package com.mariuszilinskas.streamix.cryptography.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cryptography")
public record CryptographyProperties(
        String provider,
        String encryptionKey,
        String kmsKeyId,
        String awsRegion
) {}
