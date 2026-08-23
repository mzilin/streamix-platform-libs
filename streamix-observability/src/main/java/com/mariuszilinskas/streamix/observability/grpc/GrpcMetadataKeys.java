package com.mariuszilinskas.streamix.observability.grpc;

import com.mariuszilinskas.streamix.observability.context.LogContext;
import io.grpc.Metadata;

final class GrpcMetadataKeys {

    static final Metadata.Key<String> CORRELATION_ID_KEY =
            Metadata.Key.of(
                    LogContext.CORRELATION_HEADER,
                    Metadata.ASCII_STRING_MARSHALLER
            );

    static final Metadata.Key<String> USER_ID_KEY =
            Metadata.Key.of(
                    LogContext.USER_ID_HEADER,
                    Metadata.ASCII_STRING_MARSHALLER
            );

    private GrpcMetadataKeys() {
    }
}