package com.sellerinsight.importjob.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    Optional<ImportJob> findByIdAndSellerId(Long id, Long sellerId);
}
