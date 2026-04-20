package com.sellerinsight.pipeline.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, Long> {

    List<PipelineRun> findAllByPipelineTypeOrderByIdDesc(PipelineType pipelineType);

    Optional<PipelineRun> findByIdAndPipelineType(Long id, PipelineType pipelineType);
}
