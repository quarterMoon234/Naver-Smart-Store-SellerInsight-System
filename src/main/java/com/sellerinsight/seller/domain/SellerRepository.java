package com.sellerinsight.seller.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    boolean existsByExternalSellerId(String externalSellerId);
}
