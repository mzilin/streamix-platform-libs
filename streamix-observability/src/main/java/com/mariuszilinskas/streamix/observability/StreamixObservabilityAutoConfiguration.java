package com.mariuszilinskas.streamix.observability;

import com.mariuszilinskas.streamix.observability.logging.MdcThreadLocalAccessor;
import com.mariuszilinskas.streamix.observability.reactive.StreamixReactiveObservabilityAutoConfiguration;
import com.mariuszilinskas.streamix.observability.servlet.StreamixServletObservabilityAutoConfiguration;
import io.micrometer.context.ContextRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        StreamixServletObservabilityAutoConfiguration.class,
        StreamixReactiveObservabilityAutoConfiguration.class
})
public class StreamixObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnClass(ContextRegistry.class)
    @ConditionalOnMissingBean(MdcThreadLocalAccessor.class)
    MdcThreadLocalAccessor mdcThreadLocalAccessor() {
        MdcThreadLocalAccessor accessor = new MdcThreadLocalAccessor();
        ContextRegistry.getInstance().registerThreadLocalAccessor(accessor);
        return accessor;
    }
}