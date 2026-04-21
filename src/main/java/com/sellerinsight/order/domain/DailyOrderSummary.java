package com.sellerinsight.order.domain;

import java.math.BigDecimal;

public record DailyOrderSummary(
        Long orderCount,
        BigDecimal salesAmount
) {

    public int orderCountAsInt() {
        if (orderCount == null) {
            return 0;
        }

        return Math.toIntExact(orderCount);
    }

    public BigDecimal salesAmountOrZero() {
        return salesAmount == null ? BigDecimal.ZERO : salesAmount;
    }
}
