package com.sellerinsight.seller.api.dto;

import com.sellerinsight.seller.domain.Seller;
import com.sellerinsight.seller.domain.SellerStatus;

import java.time.OffsetDateTime;

public record SellerResponse(
        Long id,
        String externalSellerId,
        String sellerName,
        SellerStatus status,
        OffsetDateTime connectedAt,
        OffsetDateTime lastSyncedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static SellerResponse from(Seller seller) {
        return new SellerResponse(
                seller.getId(),
                seller.getExternalSellerId(),
                seller.getSellerName(),
                seller.getStatus(),
                seller.getConnectedAt(),
                seller.getLastSyncedAt(),
                seller.getCreatedAt(),
                seller.getUpdatedAt()
        );
    }
}
