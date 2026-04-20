package com.sellerinsight.metric.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMetricRepository extends JpaRepository<DailyMetric, Long> {

    Optional<DailyMetric> findBySellerIdAndMetricDate(Long sellerId, LocalDate metricDate);

    Optional<DailyMetric> findFirstBySellerIdOrderByMetricDateDesc(Long sellerId);

    List<DailyMetric> findAllBySellerIdAndMetricDateBetweenOrderByMetricDateAsc(
            Long sellerId,
            LocalDate from,
            LocalDate to
    );
}
