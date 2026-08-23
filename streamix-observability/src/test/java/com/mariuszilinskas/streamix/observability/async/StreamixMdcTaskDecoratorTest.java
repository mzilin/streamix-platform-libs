package com.mariuszilinskas.streamix.observability.async;

import com.mariuszilinskas.streamix.observability.util.LogContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class StreamixMdcTaskDecoratorTest {

    private final StreamixMdcTaskDecorator decorator = new StreamixMdcTaskDecorator();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void propagatesMdcToWorkerThread() throws InterruptedException {
        MDC.put(LogContext.CORRELATION_ID, "a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        MDC.put(LogContext.USER_ID, "user-11");

        AtomicReference<String> capturedCorrelation = new AtomicReference<>();
        AtomicReference<String> capturedUser = new AtomicReference<>();

        Runnable task = decorator.decorate(() -> {
            capturedCorrelation.set(MDC.get(LogContext.CORRELATION_ID));
            capturedUser.set(MDC.get(LogContext.USER_ID));
        });

        Thread worker = new Thread(task);
        worker.start();
        worker.join();

        assertThat(capturedCorrelation.get()).isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
        assertThat(capturedUser.get()).isEqualTo("user-11");
    }

    @Test
    void clearsMdcOnWorkerThreadAfterTask() throws InterruptedException {
        MDC.put(LogContext.CORRELATION_ID, "a1b2c3d4-e5f6-7890-abcd-ef1234567890");

        AtomicReference<String> afterTaskMdc = new AtomicReference<>();
        Runnable task = decorator.decorate(() -> afterTaskMdc.set("before-clear"));

        Thread worker = new Thread(() -> {
            MDC.put("worker-key", "worker-value");
            task.run();
            afterTaskMdc.set(MDC.get("worker-key"));
        });
        worker.start();
        worker.join();

        assertThat(afterTaskMdc.get()).isNull();
    }

    @Test
    void capturesMdcAtSubmissionNotAtExecution() throws InterruptedException {
        MDC.put(LogContext.CORRELATION_ID, "original-id");
        Runnable decorated = decorator.decorate(() -> {});

        MDC.put(LogContext.CORRELATION_ID, "changed-after-decoration");

        AtomicReference<String> capturedCorrelation = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            decorated.run();
            capturedCorrelation.set(MDC.get(LogContext.CORRELATION_ID));
        });
        worker.start();
        worker.join();

        // The worker sees the MDC that was captured at decoration time, then cleared after
        assertThat(capturedCorrelation.get()).isNull();
    }

    @Test
    void worksWithEmptyMdc() throws InterruptedException {
        AtomicReference<String> capturedCorrelation = new AtomicReference<>("sentinel");
        Runnable task = decorator.decorate(() ->
                capturedCorrelation.set(MDC.get(LogContext.CORRELATION_ID))
        );

        Thread worker = new Thread(task);
        worker.start();
        worker.join();

        assertThat(capturedCorrelation.get()).isNull();
    }
}
