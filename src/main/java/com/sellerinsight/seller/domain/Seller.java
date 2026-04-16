package com.sellerinsight.seller.domain;

import com.sellerinsight.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Entity
@Table(
        name = "sellers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sellers_external_seller_id",
                        columnNames = "external_seller_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller extends BaseEntity {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");

    @Column(name = "external_seller_id", nullable = false, length = 100)
    private String externalSellerId;

    @Column(name = "seller_name", nullable = false, length = 100)
    private String sellerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SellerStatus status;

    @Column(name = "connected_at", nullable = false)
    private OffsetDateTime connectedAt;

    @Column(name = "last_synced_at")
    private OffsetDateTime lastSyncedAt;

    private Seller(String externalSellerId, String sellerName) {
        this.externalSellerId = externalSellerId;
        this.sellerName = sellerName;
        this.status = SellerStatus.CONNECTED;
        this.connectedAt = OffsetDateTime.now(ASIA_SEOUL);
    }

    public static Seller create(String externalSellerId, String sellerName) {
        return new Seller(externalSellerId, sellerName);
    }

    public void markSynced(OffsetDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public void disconnect() {
        this.status = SellerStatus.DISCONNECTED;
    }
}

