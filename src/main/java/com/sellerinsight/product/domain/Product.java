package com.sellerinsight.product.domain;

import com.sellerinsight.common.entity.BaseEntity;
import com.sellerinsight.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_products_seller_id_external_product_id",
                        columnNames = {"seller_id", "external_product_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seller_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_seller_id")
    )
    private Seller seller;

    @Column(name = "external_product_id", nullable = false, length = 100)
    private String externalProductId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "sale_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "product_status", nullable = false, length = 50)
    private String productStatus;

    private Product(
            Seller seller,
            String externalProductId,
            String productName,
            BigDecimal salePrice,
            Integer stockQuantity,
            String productStatus
    ) {
        this.seller = seller;
        this.externalProductId = externalProductId;
        this.productName = productName;
        this.salePrice = salePrice;
        this.stockQuantity = stockQuantity;
        this.productStatus = productStatus;
    }

    public static Product create(
            Seller seller,
            String externalProductId,
            String productName,
            BigDecimal salePrice,
            Integer stockQuantity,
            String productStatus
    ) {
        return new Product(
                seller,
                externalProductId,
                productName,
                salePrice,
                stockQuantity,
                productStatus
        );
    }

    public void updateFromImport(
            String productName,
            BigDecimal salePrice,
            Integer stockQuantity,
            String productStatus
    ) {
        this.productName = productName;
        this.salePrice = salePrice;
        this.stockQuantity = stockQuantity;
        this.productStatus = productStatus;
    }
}

