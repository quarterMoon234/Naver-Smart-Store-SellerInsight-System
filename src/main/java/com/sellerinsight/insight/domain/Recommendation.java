package com.sellerinsight.insight.domain;

import com.sellerinsight.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "recommendations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recommendation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "insight_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_recommendations_insight_id")
    )
    private Insight insight;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Column(name = "action_code", nullable = false, length = 100)
    private String actionCode;

    @Column(name = "action_title", nullable = false, length = 200)
    private String actionTitle;

    @Column(name = "action_message", nullable = false, length = 1000)
    private String actionMessage;

    @Column(name = "source_rule", nullable = false, length = 100)
    private String sourceRule;

    private Recommendation(
            Insight insight,
            int priority,
            String actionCode,
            String actionTitle,
            String actionMessage,
            String sourceRule
    ) {
        this.insight = insight;
        this.priority = priority;
        this.actionCode = actionCode;
        this.actionTitle = actionTitle;
        this.actionMessage = actionMessage;
        this.sourceRule = sourceRule;
    }

    public static Recommendation create(
            Insight insight,
            int priority,
            String actionCode,
            String actionTitle,
            String actionMessage,
            String sourceRule
    ) {
        return new Recommendation(
                insight,
                priority,
                actionCode,
                actionTitle,
                actionMessage,
                sourceRule
        );
    }
}
