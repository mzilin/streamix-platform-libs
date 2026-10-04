package com.mariuszilinskas.streamix.masking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskingServiceImplTest {

    private final MaskingServiceImpl service = new MaskingServiceImpl();

    // ----- maskFull -----

    @Test
    void maskFull_returnsNullForNullInput() {
        assertThat(service.maskFull(null)).isNull();
    }

    @Test
    void maskFull_returnsUnchangedForEmptyString() {
        assertThat(service.maskFull("")).isEmpty();
    }

    @Test
    void maskFull_masksBlankString() {
        assertThat(service.maskFull("   ")).isEqualTo("***");
    }

    @Test
    void maskFull_replacesAllCharsWithAsterisks() {
        assertThat(service.maskFull("hello")).isEqualTo("*****");
    }

    @Test
    void maskFull_workForSingleChar() {
        assertThat(service.maskFull("a")).isEqualTo("*");
    }

    // ----- maskPartial -----

    @Test
    void maskPartial_returnsNullForNullInput() {
        assertThat(service.maskPartial(null)).isNull();
    }

    @Test
    void maskPartial_returnsUnchangedForEmptyString() {
        assertThat(service.maskPartial("")).isEmpty();
    }

    @Test
    void maskPartial_masksBlankString() {
        assertThat(service.maskPartial("   ")).isEqualTo("***");
    }

    @Test
    void maskPartial_returnsSingleAsteriskForOneChar() {
        assertThat(service.maskPartial("a")).isEqualTo("*");
    }

    @Test
    void maskPartial_masksSecondCharForTwoCharString() {
        assertThat(service.maskPartial("he")).isEqualTo("h*");
    }

    @Test
    void maskPartial_showsFirstAndLastWithMaskedMiddle() {
        assertThat(service.maskPartial("hello")).isEqualTo("h***o");
    }

    @Test
    void maskPartial_workForLongString() {
        assertThat(service.maskPartial("1234567890")).isEqualTo("1********0");
    }

    // ----- maskEmail -----

    @Test
    void maskEmail_returnsNullForNullInput() {
        assertThat(service.maskEmail(null)).isNull();
    }

    @Test
    void maskEmail_returnsUnchangedForEmptyString() {
        assertThat(service.maskEmail("")).isEmpty();
    }

    @Test
    void maskEmail_masksBlankString() {
        assertThat(service.maskEmail("   ")).isEqualTo("***");
    }

    @Test
    void maskEmail_masksStandardEmail() {
        assertThat(service.maskEmail("lmarie.ali@gmail.com")).isEqualTo("l*****.a**@g****.c**");
    }

    @Test
    void maskEmail_masksEmailWithMultipleDotsInLocalPart() {
        assertThat(service.maskEmail("a.b.c@sub.domain.com")).isEqualTo("*.*.*@s**.d*****.c**");
    }

    @Test
    void maskEmail_masksEmailWithSubdomain() {
        assertThat(service.maskEmail("user@mail.example.co.uk")).isEqualTo("u***@m***.e******.c*.u*");
    }

    @Test
    void maskEmail_masksEmailWithNoDotInLocalPart() {
        assertThat(service.maskEmail("user@domain.com")).isEqualTo("u***@d*****.c**");
    }

    @Test
    void maskEmail_fallsBackToPartialMaskForInvalidFormat() {
        assertThat(service.maskEmail("invalid-email")).isEqualTo("i***********l");
    }

}
