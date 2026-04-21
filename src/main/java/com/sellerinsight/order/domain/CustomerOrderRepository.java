package com.sellerinsight.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findBySellerIdAndExternalOrderNo(Long sellerId, String externalOrderNo);

    @Query("""
            select new com.sellerinsight.order.domain.DailyOrderSummary(
                count(o),
                sum(o.totalAmount)
            )
            from CustomerOrder o
            where o.seller.id = :sellerId
              and o.orderedAt >= :start
              and o.orderedAt < :end
            """)
    DailyOrderSummary summarizeDailyOrders(
            @Param("sellerId") Long sellerId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );
}
