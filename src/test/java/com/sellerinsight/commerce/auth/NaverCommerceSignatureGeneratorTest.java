package com.sellerinsight.commerce.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NaverCommerceSignatureGeneratorTest {

    private final NaverCommerceSignatureGenerator signatureGenerator =
            new NaverCommerceSignatureGenerator();

    @Test
    void generateSignature() {
        String signature = signatureGenerator.generate(
                "aaaabbbbcccc",
                "$2a$10$abcdefghijklmnopqrstuv",
                1643961623299L
        );

        assertThat(signature)
                .isEqualTo("JDJhJDEwJGFiY2RlZmdoaWprbG1ub3BxcnN0dXVCVldZSk42T0VPdEx1OFY0cDQxa2IuTnpVaUEzbmsy");
    }
}
