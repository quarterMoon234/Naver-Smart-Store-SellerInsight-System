package com.sellerinsight.pipeline.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PipelineRunItemRepository extends JpaRepository<PipelineRunItem, Long> {

    List<PipelineRunItem> findAllByPipelineRunIdOrderByIdAsc(Long pipelineRunId);

    Optional<PipelineRunItem> findFirstBySellerIdOrderByIdDesc(Long sellerId);
}
