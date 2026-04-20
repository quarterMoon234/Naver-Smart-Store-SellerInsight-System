package com.sellerinsight.insight.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    List<Recommendation> findAllByInsightIdOrderByPriorityAsc(Long insightId);

    void deleteAllByInsightId(Long insightId);
}
