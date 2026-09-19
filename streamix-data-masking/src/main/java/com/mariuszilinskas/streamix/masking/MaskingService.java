package com.mariuszilinskas.streamix.masking;

public interface MaskingService {
    String maskFull(String value);
    String maskPartial(String value);
    String maskEmail(String value);
}
