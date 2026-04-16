package com.sellerinsight.seller.application;

import com.sellerinsight.common.error.BusinessException;
import com.sellerinsight.common.error.ErrorCode;
import com.sellerinsight.seller.api.dto.CreateSellerRequest;
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

    @Transactional
    public SellerResponse create(CreateSellerRequest request) {
        if (sellerRepository.existsByExternalSellerId(request.externalSellerId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }

        Seller seller = Seller.create(request.externalSellerId(), request.sellerName());
        Seller savedSeller = sellerRepository.save(seller);

        return SellerResponse.from(savedSeller);
    }

    public SellerResponse get(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        return SellerResponse.from(seller);
    }
}
