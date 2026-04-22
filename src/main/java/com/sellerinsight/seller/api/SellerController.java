package com.sellerinsight.seller.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.seller.api.dto.SellerResponse;
import com.sellerinsight.seller.application.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Seller", description = "판매자 관리 API")
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @Operation(summary = "현재 인증된 판매자 조회")
    @GetMapping("/me")
    public ApiResponse<SellerResponse> getCurrentSeller() {
        return ApiResponse.ok(sellerService.getCurrentSeller());
    }

    @Operation(summary = "판매자 단건 조회")
    @GetMapping("/{sellerId}")
    public ApiResponse<SellerResponse> get(@PathVariable Long sellerId) {
        return ApiResponse.ok(sellerService.get(sellerId));
    }
}
