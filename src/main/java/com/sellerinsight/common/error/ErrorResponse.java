package com.sellerinsight.common.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        Map<String, String> details,
        OffsetDateTime timestamp
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                null,
                OffsetDateTime.now()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, Map<String, String> details) {
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                details,
                OffsetDateTime.now()
        );
    }
}
