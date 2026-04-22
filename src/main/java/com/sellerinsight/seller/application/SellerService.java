package com.sellerinsight.seller.application;

import com.sellerinsight.common.config.ApiSecurityProperties;
import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.seller.api.dto.SellerResponse;
import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {

    private final SellerRepository sellerRepository;
    private final ApiSecurityProperties apiSecurityProperties;

    public SellerResponse get(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        return SellerResponse.from(seller);
    }

    public SellerResponse getCurrentSeller() {
        Seller seller = sellerRepository.findByExternalSellerId(apiSecurityProperties.sellerExternalSellerId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        return SellerResponse.from(seller);
    }
}
