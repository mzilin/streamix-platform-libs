package com.mariuszilinskas.streamix.observability.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ObservabilityUtilsTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void putShouldStoreValidKeyAndValue() {
        ObservabilityUtils.put("test-key", "test-value");

        assertEquals("test-value", MDC.get("test-key"));
    }

    @Test
    void putShouldIgnoreBlankKey() {
        ObservabilityUtils.put("   ", "test-value");

        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
    }

    @Test
    void putShouldIgnoreNullValue() {
        ObservabilityUtils.put("test-key", null);

        assertNull(MDC.get("test-key"));
    }

    @Test
    void putShouldIgnoreBlankValue() {
        ObservabilityUtils.put("test-key", "   ");

        assertNull(MDC.get("test-key"));
    }

    @Test
    void removeShouldRemoveExistingValue() {
        MDC.put("test-key", "test-value");

        ObservabilityUtils.remove("test-key");

        assertNull(MDC.get("test-key"));
    }

    @Test
    void removeShouldIgnoreNullKey() {
        MDC.put("test-key", "test-value");

        ObservabilityUtils.remove(null);

        assertEquals("test-value", MDC.get("test-key"));
    }

    @Test
    void removeShouldIgnoreBlankKey() {
        MDC.put("test-key", "test-value");

        ObservabilityUtils.remove("   ");

        assertEquals("test-value", MDC.get("test-key"));
    }

    @Test
    void resolveCorrelationIdShouldReturnValidCorrelationIdUnchanged() {
        String correlationId = UUID.randomUUID().toString();

        String result = ObservabilityUtils.resolveCorrelationId(correlationId);

        assertEquals(correlationId, result);
    }

    @Test
    void resolveCorrelationIdShouldGenerateNewIdForNullValue() {
        String result = ObservabilityUtils.resolveCorrelationId(null);

        assertNotNull(result);
        assertTrue(ObservabilityUtils.isValidCorrelationId(result));
    }

    @Test
    void resolveCorrelationIdShouldGenerateNewIdForBlankValue() {
        String result = ObservabilityUtils.resolveCorrelationId("   ");

        assertNotNull(result);
        assertTrue(ObservabilityUtils.isValidCorrelationId(result));
    }

    @Test
    void resolveCorrelationIdShouldGenerateNewIdForInvalidValue() {
        String result = ObservabilityUtils.resolveCorrelationId("not-a-uuid");

        assertNotNull(result);
        assertTrue(ObservabilityUtils.isValidCorrelationId(result));
    }

    @Test
    void isValidCorrelationIdShouldReturnTrueForValidUuid() {
        String correlationId = UUID.randomUUID().toString();

        assertTrue(ObservabilityUtils.isValidCorrelationId(correlationId));
    }

    @Test
    void isValidCorrelationIdShouldReturnFalseForNull() {
        assertFalse(ObservabilityUtils.isValidCorrelationId(null));
    }

    @Test
    void isValidCorrelationIdShouldReturnFalseForBlankValue() {
        assertFalse(ObservabilityUtils.isValidCorrelationId(""));
        assertFalse(ObservabilityUtils.isValidCorrelationId("   "));
    }

    @Test
    void isValidCorrelationIdShouldReturnFalseForInvalidUuid() {
        assertFalse(ObservabilityUtils.isValidCorrelationId("not-a-uuid"));
        assertFalse(ObservabilityUtils.isValidCorrelationId("12345"));
        assertFalse(ObservabilityUtils.isValidCorrelationId("550e8400-e29b-41d4-a716"));
    }

    @Test
    void isValidCorrelationIdShouldAcceptUuidWithUppercaseCharacters() {
        String correlationId = UUID.randomUUID().toString().toUpperCase();

        assertTrue(ObservabilityUtils.isValidCorrelationId(correlationId));
    }
}