package com.mariuszilinskas.streamix.observability.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LogContextManagerTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void putShouldStoreValidKeyAndValue() {
        LogContextManager.put("test-key", "test-value");

        assertEquals("test-value", MDC.get("test-key"));
    }

    @Test
    void putShouldIgnoreBlankKey() {
        LogContextManager.put("   ", "test-value");

        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
    }

    @Test
    void putShouldIgnoreNullValue() {
        LogContextManager.put("test-key", null);

        assertNull(MDC.get("test-key"));
    }

    @Test
    void putShouldIgnoreBlankValue() {
        LogContextManager.put("test-key", "   ");

        assertNull(MDC.get("test-key"));
    }

    @Test
    void removeShouldRemoveExistingValue() {
        MDC.put("test-key", "test-value");

        LogContextManager.remove("test-key");

        assertNull(MDC.get("test-key"));
    }

    @Test
    void removeShouldIgnoreNullKey() {
        MDC.put("test-key", "test-value");

        LogContextManager.remove(null);

        assertEquals("test-value", MDC.get("test-key"));
    }

    @Test
    void removeShouldIgnoreBlankKey() {
        MDC.put("test-key", "test-value");

        LogContextManager.remove("   ");

        assertEquals("test-value", MDC.get("test-key"));
    }

    @Test
    void resolveCorrelationIdShouldReturnValidCorrelationIdUnchanged() {
        String correlationId = UUID.randomUUID().toString();

        String result = LogContextManager.resolveCorrelationId(correlationId);

        assertEquals(correlationId, result);
    }

    @Test
    void resolveCorrelationIdShouldGenerateNewIdForNullValue() {
        String result = LogContextManager.resolveCorrelationId(null);

        assertNotNull(result);
        assertTrue(LogContextManager.isValidCorrelationId(result));
    }

    @Test
    void resolveCorrelationIdShouldGenerateNewIdForBlankValue() {
        String result = LogContextManager.resolveCorrelationId("   ");

        assertNotNull(result);
        assertTrue(LogContextManager.isValidCorrelationId(result));
    }

    @Test
    void resolveCorrelationIdShouldGenerateNewIdForInvalidValue() {
        String result = LogContextManager.resolveCorrelationId("not-a-uuid");

        assertNotNull(result);
        assertTrue(LogContextManager.isValidCorrelationId(result));
    }

    @Test
    void isValidCorrelationIdShouldReturnTrueForValidUuid() {
        String correlationId = UUID.randomUUID().toString();

        assertTrue(LogContextManager.isValidCorrelationId(correlationId));
    }

    @Test
    void isValidCorrelationIdShouldReturnFalseForNull() {
        assertFalse(LogContextManager.isValidCorrelationId(null));
    }

    @Test
    void isValidCorrelationIdShouldReturnFalseForBlankValue() {
        assertFalse(LogContextManager.isValidCorrelationId(""));
        assertFalse(LogContextManager.isValidCorrelationId("   "));
    }

    @Test
    void isValidCorrelationIdShouldReturnFalseForInvalidUuid() {
        assertFalse(LogContextManager.isValidCorrelationId("not-a-uuid"));
        assertFalse(LogContextManager.isValidCorrelationId("12345"));
        assertFalse(LogContextManager.isValidCorrelationId("550e8400-e29b-41d4-a716"));
    }

    @Test
    void isValidCorrelationIdShouldAcceptUuidWithUppercaseCharacters() {
        String correlationId = UUID.randomUUID().toString().toUpperCase();

        assertTrue(LogContextManager.isValidCorrelationId(correlationId));
    }
}