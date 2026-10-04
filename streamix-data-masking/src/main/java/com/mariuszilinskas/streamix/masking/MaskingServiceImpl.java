package com.mariuszilinskas.streamix.masking;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class MaskingServiceImpl implements MaskingService {

    private static final String MASK = "*";

    @Override
    public String maskFull(String value) {
        if (value == null) return null;
        return MASK.repeat(value.length());
    }

    @Override
    public String maskPartial(String value) {
        if (value == null) return null;
        if (value.isBlank()) return MASK.repeat(value.length());

        int length = value.length();

        if (length == 1) {
            return MASK;
        }

        if (length == 2) {
            return value.charAt(0) + MASK;
        }

        return value.charAt(0)
                + MASK.repeat(length - 2)
                + value.charAt(length - 1);
    }

    @Override
    public String maskEmail(String value) {
        if (value == null) return null;
        if (value.isBlank()) return MASK.repeat(value.length());

        String[] parts = value.split("@", 2);

        if (parts.length < 2) {
            return maskPartial(value);
        }

        String localPart = Arrays.stream(parts[0].split("\\.", -1))
                .map(this::maskSegment)
                .collect(Collectors.joining("."));

        String domainPart = Arrays.stream(parts[1].split("\\.", -1))
                .map(this::maskSegment)
                .collect(Collectors.joining("."));

        return localPart + "@" + domainPart;
    }

    private String maskSegment(String segment) {
        if (segment.isEmpty()) {
            return segment;
        }

        if (segment.length() == 1) {
            return MASK;
        }

        return segment.charAt(0) + MASK.repeat(segment.length() - 1);
    }
}