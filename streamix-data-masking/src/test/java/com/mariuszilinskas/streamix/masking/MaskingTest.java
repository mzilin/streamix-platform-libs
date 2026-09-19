package com.mariuszilinskas.streamix.masking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingTest {

    @Test
    void maskFull_delegatesToService() {
        assertThat(Masking.maskFull("hello")).isEqualTo("*****");
    }

    @Test
    void maskPartial_delegatesToService() {
        assertThat(Masking.maskPartial("hello")).isEqualTo("h***o");
    }

    @Test
    void maskEmail_delegatesToService() {
        assertThat(Masking.maskEmail("user@example.com")).isEqualTo("u***@e******.c**");
    }

}
