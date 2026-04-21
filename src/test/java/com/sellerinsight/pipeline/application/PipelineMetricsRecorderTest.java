package com.sellerinsight.pipeline.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.sellerinsight.pipeline.domain.PipelineRun;
import com.sellerinsight.pipeline.domain.PipelineRunStatus;
import com.sellerinsight.pipeline.domain.PipelineTriggerType;
import com.sellerinsight.pipeline.domain.PipelineType;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class PipelineMetricsRecorderTest {

    @Test
    void recordDailyPipelineRun() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PipelineMetricsRecorder recorder = new PipelineMetricsRecorder(meterRegistry);

        PipelineRun pipelineRun = PipelineRun.start(
                PipelineType.DAILY,
                PipelineTriggerType.MANUAL,
                LocalDate.of(2026, 4, 20)
        );

        pipelineRun.complete(
                PipelineRunStatus.SUCCESS,
                30,
                30,
                0,
                120
        );

        recorder.recordDailyPipelineRun(pipelineRun);

        assertThat(meterRegistry.get("daily.pipeline.runs").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("daily.pipeline.processed.sellers").counter().count()).isEqualTo(30.0);
        assertThat(meterRegistry.get("daily.pipeline.failed.sellers").counter().count()).isEqualTo(0.0);
        assertThat(meterRegistry.get("daily.pipeline.generated.insights").counter().count()).isEqualTo(120.0);

        Timer durationTimer = meterRegistry.get("daily.pipeline.duration").timer();
        assertThat(durationTimer.count()).isEqualTo(1);
        assertThat(durationTimer.totalTime(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(0.0);
    }
}
