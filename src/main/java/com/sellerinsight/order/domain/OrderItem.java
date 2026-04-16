package com.sellerinsight.order.domain;

import com.sellerinsight.common.entity.BaseEntity;
import com.sellerinsight.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "order_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_order_items_order_id_external_order_item_no",
                        columnNames = {"order_id", "external_order_item_no"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order_id")
    )
    private CustomerOrder customerOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_product_id")
    )
    private Product product;

    @Column(name = "external_order_item_no", nullable = false, length = 100)
    private String externalOrderItemNo;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "item_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal itemAmount;

    private OrderItem(
            CustomerOrder customerOrder,
            Product product,
            String externalOrderItemNo,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal itemAmount
    ) {
        this.customerOrder = customerOrder;
        this.product = product;
        this.externalOrderItemNo = externalOrderItemNo;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemAmount = itemAmount;
    }

    public static OrderItem create(
            CustomerOrder customerOrder,
            Product product,
            String externalOrderItemNo,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal itemAmount
    ) {
        return new OrderItem(
                customerOrder,
                product,
                externalOrderItemNo,
                quantity,
                unitPrice,
                itemAmount
        );
    }

    public void updateFromImport(
            Product product,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal itemAmount
    ) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemAmount = itemAmount;
    }
}

