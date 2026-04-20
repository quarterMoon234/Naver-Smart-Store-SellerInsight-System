package com.sellerinsight.insight.application;

import java.util.Optional;

public interface InsightRule {

    Optional<InsightCandidate> evaluate(MetricContext context);
}
