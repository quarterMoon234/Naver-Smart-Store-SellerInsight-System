package com.sellerinsight.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_400", "요청 값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "COMMON_409", "이미 존재하는 리소스입니다."),
    SELLER_CREDENTIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER_404_1", "판매자 연동 정보를 찾을 수 없습니다."),

    NAVER_COMMERCE_SIGNATURE_FAILED(HttpStatus.BAD_REQUEST, "COMMERCE_400_1", "네이버 커머스 clientSecret 형식이 올바르지 않습니다."),
    NAVER_COMMERCE_AUTH_FAILED(HttpStatus.BAD_GATEWAY, "COMMERCE_502_1", "네이버 커머스 인증 토큰 발급에 실패했습니다."),
    NAVER_COMMERCE_AUTH_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "COMMERCE_502_2", "네이버 커머스 인증 응답 형식이 올바르지 않습니다."),

    IMPORT_JOB_NOT_FOUND(HttpStatus.NOT_FOUND, "IMPORT_404_1", "가져오기 작업을 찾을 수 없습니다."),
    CSV_IMPORT_INVALID_FILE(HttpStatus.BAD_REQUEST, "IMPORT_400_1", "CSV 파일이 비어 있거나 형식이 올바르지 않습니다."),
    CSV_IMPORT_FAILED(HttpStatus.BAD_REQUEST, "IMPORT_400_2", "CSV 가져오기에 실패했습니다."),
    CSV_IMPORT_INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "IMPORT_400_3", "허용되지 않은 CSV 파일 형식입니다."),
    CSV_IMPORT_FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "IMPORT_413_1", "업로드 가능한 CSV 파일 크기를 초과했습니다."),

    DAILY_METRIC_NOT_FOUND(HttpStatus.NOT_FOUND, "METRIC_404_1", "일별 지표를 찾을 수 없습니다."),
    INSIGHT_NOT_FOUND(HttpStatus.NOT_FOUND, "INSIGHT_404_1", "인사이트를 찾을 수 없습니다."),
    PIPELINE_RUN_NOT_FOUND(HttpStatus.NOT_FOUND, "PIPELINE_404_1", "파이프라인 실행 이력을 찾을 수 없습니다."),
    PIPELINE_ALREADY_RUNNING(HttpStatus.CONFLICT, "PIPELINE_409_1", "동일한 날짜의 파이프라인이 이미 실행 중입니다."),

    DB_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "HEALTH_503", "데이터베이스 연결에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
