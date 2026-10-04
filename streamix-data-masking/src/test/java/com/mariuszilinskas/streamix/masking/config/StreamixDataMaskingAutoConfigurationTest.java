package com.mariuszilinskas.streamix.masking.config;

import com.mariuszilinskas.streamix.masking.MaskingServiceImpl;
import com.mariuszilinskas.streamix.masking.MaskingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StreamixDataMaskingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StreamixDataMaskingAutoConfiguration.class));

    @Test
    void registersMaskingServiceBeanWhenNoCustomBeanPresent() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(MaskingService.class);
            assertThat(context.getBean(MaskingService.class))
                    .isInstanceOf(MaskingServiceImpl.class);
        });
    }

    @Test
    void doesNotRegisterDefaultBeanWhenCustomMaskingServiceIsPresent() {
        contextRunner
                .withBean(MaskingService.class, () -> mock(MaskingService.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(MaskingService.class);
                    assertThat(context.getBean(MaskingService.class))
                            .isNotInstanceOf(MaskingServiceImpl.class);
                });
    }

}
