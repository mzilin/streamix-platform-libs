package com.mariuszilinskas.streamix.observability.async;

import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public final class MdcTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable task) {
        Map<String, String> capturedContext = MDC.getCopyOfContextMap();

        return () -> {
            try {
                if (capturedContext != null) {
                    MDC.setContextMap(capturedContext);
                } else {
                    MDC.clear();
                }
                task.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
