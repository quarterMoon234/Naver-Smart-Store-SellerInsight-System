package com.sellerinsight.seller.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSellerRequest(
        @NotBlank(message = "externalSellerId는 필수입니다.")
        @Size(max = 100, message = "externalSellerId는 100자 이하여야 합니다.")
        String externalSellerId,

        @NotBlank(message = "sellerName은 필수입니다.")
        @Size(max = 100, message = "sellerName은 100자 이하여야 합니다.")
        String sellerName
) {
}
