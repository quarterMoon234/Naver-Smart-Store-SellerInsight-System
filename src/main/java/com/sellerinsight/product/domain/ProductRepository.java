package com.sellerinsight.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySellerIdAndExternalProductId(Long sellerId, String externalProductid);
}
