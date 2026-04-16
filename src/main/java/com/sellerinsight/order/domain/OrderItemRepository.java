package com.sellerinsight.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    Optional<OrderItem> findByCustomerOrderIdAndExternalOrderItemNo(Long customerOrderId, String externalOrderItemNo);
}
