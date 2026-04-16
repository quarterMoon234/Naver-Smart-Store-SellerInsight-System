package com.sellerinsight.seller.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerCredentialRepository extends JpaRepository<SellerCredential, Long> {

    Optional<SellerCredential> findBySellerId(Long sellerId);
}