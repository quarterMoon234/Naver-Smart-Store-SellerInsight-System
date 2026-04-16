package com.sellerinsight.seller.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.seller.api.dto.SellerAccessTokenResponse;
import com.sellerinsight.seller.application.SellerAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Seller Auth", description = "판매자 인증 API")
@RestController
@RequestMapping("/api/v1/sellers/{sellerId}/auth")
@RequiredArgsConstructor
public class SellerAuthController {

    private final SellerAuthService sellerAuthService;

    @Operation(summary = "네이버 커머스 SELLER 액세스 토큰 발급")
    @PostMapping("/token")
    public ApiResponse<SellerAccessTokenResponse> issueSellerToken(
            @PathVariable Long sellerId
    ) {
        return ApiResponse.ok(sellerAuthService.issueSellerToken(sellerId));
    }
}
