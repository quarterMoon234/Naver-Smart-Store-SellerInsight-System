package com.sellerinsight.insight.domain;

import com.sellerinsight.common.entity.BaseEntity;
import com.sellerinsight.seller.domain.Seller;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "insights",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_insights_seller_id_metric_date_insight_type",
                        columnNames = {"seller_id", "metric_date", "insight_type"}
                )
        }
)
@NoArgsConstructor
public class Insight extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "seller_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_insights_seller_id")
    )
    private Seller seller;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "insight_type", nullable = false, length = 50)
    private InsightType insightType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private InsightSeverity severity;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "summary", nullable = false, length = 1000)
    private String summary;

    @Column(name = "evidence_json", nullable = false, columnDefinition = "text")
    private String evidenceJson;

    @Column(name = "generated_by", nullable = false, length = 100)
    private String generatedBy;

    private Insight(
            Seller seller,
            LocalDate metricDate,
            InsightType insightType,
            InsightSeverity severity,
            String title,
            String summary,
            String evidenceJson,
            String generatedBy
    ) {
        this.seller = seller;
        this.metricDate = metricDate;
        this.insightType = insightType;
        this.severity = severity;
        this.title = title;
        this.summary = summary;
        this.evidenceJson = evidenceJson;
        this.generatedBy = generatedBy;
    }

    public static Insight create(
            Seller seller,
            LocalDate metricDate,
            InsightType insightType,
            InsightSeverity severity,
            String title,
            String summary,
            String evidenceJson,
            String generatedBy
    ) {
        return new Insight(
                seller,
                metricDate,
                insightType,
                severity,
                title,
                summary,
                evidenceJson,
                generatedBy
        );
    }

    public void update(
            InsightSeverity severity,
            String title,
            String summary,
            String evidenceJson,
            String generatedBy
    ) {
        this.severity = severity;
        this.title = title;
        this.summary = summary;
        this.evidenceJson = evidenceJson;
        this.generatedBy = generatedBy;
    }
}
