package com.sellerinsight.insight.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InsightRepository extends JpaRepository<Insight, Long> {

    Optional<Insight> findBySellerIdAndMetricDateAndInsightType(
            Long sellerId,
            LocalDate metricDate,
            InsightType insightType
    );

    List<Insight> findAllBySellerIdAndMetricDateOrderByIdAsc(Long sellerId, LocalDate metricDate);

    List<Insight> findAllBySellerId(Long sellerId, Pageable pageable);

    long countBySellerIdAndSeverity(Long sellerId, InsightSeverity severity);

    Optional<Insight> findByIdAndSellerId(Long id, Long sellerId);
}
