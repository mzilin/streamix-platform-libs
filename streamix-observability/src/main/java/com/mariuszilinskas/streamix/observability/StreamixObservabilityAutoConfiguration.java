package com.mariuszilinskas.streamix.observability;

import com.mariuszilinskas.streamix.observability.reactive.StreamixReactiveObservabilityAutoConfiguration;
import com.mariuszilinskas.streamix.observability.servlet.StreamixServletObservabilityAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        StreamixServletObservabilityAutoConfiguration.class,
        StreamixReactiveObservabilityAutoConfiguration.class
})
public class StreamixObservabilityAutoConfiguration {
}