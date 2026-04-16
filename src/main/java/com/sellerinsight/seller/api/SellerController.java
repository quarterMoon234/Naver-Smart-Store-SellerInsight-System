package com.sellerinsight.seller.api;

import com.sellerinsight.common.api.ApiResponse;
import com.sellerinsight.seller.api.dto.CreateSellerRequest;
import com.sellerinsight.seller.api.dto.SellerResponse;
import com.sellerinsight.seller.application.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Seller", description = "판매자 관리 API")
@RestController
@RequestMapping("/api/v1/sellers")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @Operation(summary = "판매자 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<SellerResponse>> create(
            @Valid @RequestBody CreateSellerRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(sellerService.create(request)));
    }

    @Operation(summary = "판매자 단건 조회")
    @GetMapping("/{sellerId}")
    public ApiResponse<SellerResponse> get(@PathVariable Long sellerId) {
        return ApiResponse.ok(sellerService.get(sellerId));
    }
}
