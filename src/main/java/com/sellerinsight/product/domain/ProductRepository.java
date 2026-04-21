package com.sellerinsight.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySellerIdAndExternalProductId(Long sellerId, String externalProductid);

    long countBySellerIdAndStockQuantityLessThanEqual(Long sellerId, Integer stockQuantity);

    @Query("""
            select count(p)
            from Product p
            where p.seller.id = :sellerId
              and not exists (
                  select oi.id
                  from OrderItem oi
                  join oi.customerOrder o
                  where oi.product.id = p.id
                    and o.seller.id = :sellerId
                    and o.orderedAt >= :start
                    and o.orderedAt < :end
              )
            """)
    long countStaleProductsBySellerIdAndOrderedAtBetween(
            @Param("sellerId") Long sellerId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}
