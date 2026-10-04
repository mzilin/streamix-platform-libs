package com.mariuszilinskas.streamix.masking.config;

import com.mariuszilinskas.streamix.masking.MaskingServiceImpl;
import com.mariuszilinskas.streamix.masking.MaskingService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class StreamixDataMaskingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MaskingService.class)
    public MaskingService maskingService() {
        return new MaskingServiceImpl();
    }

}
