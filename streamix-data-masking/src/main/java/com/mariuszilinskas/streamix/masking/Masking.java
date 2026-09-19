package com.mariuszilinskas.streamix.masking;

public final class Masking {

    private static final MaskingService SERVICE = new MaskingServiceImpl();

    private Masking() {}

    public static String maskFull(String value) { return SERVICE.maskFull(value); }
    public static String maskPartial(String value) { return SERVICE.maskPartial(value); }
    public static String maskEmail(String value) { return SERVICE.maskEmail(value); }

}
