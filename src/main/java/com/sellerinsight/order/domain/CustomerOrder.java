package com.sellerinsight.order.domain;

import com.sellerinsight.common.entity.BaseEntity;
import com.sellerinsight.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Entity
@Table(
        name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_orders_seller_id_external_order_no",
                        columnNames = {"seller_id", "external_order_no"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seller_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orders_seller_id")
    )
    private Seller seller;

    @Column(name = "external_order_no", nullable = false, length = 100)
    private String externalOrderNo;

    @Column(name = "ordered_at", nullable = false)
    private OffsetDateTime orderedAt;

    @Column(name = "order_status", nullable = false, length = 50)
    private String orderStatus;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    private CustomerOrder(
            Seller seller,
            String externalOrderNo,
            OffsetDateTime orderedAt,
            String orderStatus,
            BigDecimal totalAmount
    ) {
        this.seller = seller;
        this.externalOrderNo = externalOrderNo;
        this.orderedAt = orderedAt;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
    }

    public static CustomerOrder create(
            Seller seller,
            String externalOrderNo,
            OffsetDateTime orderedAt,
            String orderStatus,
            BigDecimal totalAmount
    ) {
        return new CustomerOrder(
                seller,
                externalOrderNo,
                orderedAt,
                orderStatus,
                totalAmount
        );
    }

    public void updateFromImport(
            OffsetDateTime orderedAt,
            String orderStatus,
            BigDecimal totalAmount
    ) {
        this.orderedAt = orderedAt;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
    }
}
