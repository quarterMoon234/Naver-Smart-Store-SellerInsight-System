# SellerInsight

SellerInsight는 판매자의 주문 데이터를 정규화하고, 일별 판매 지표와 운영 인사이트를 제공하는 커머스 분석 백엔드 서비스입니다.

판매자는 주문 데이터를 CSV로 업로드하거나 연동 데이터를 적재할 수 있으며, 서비스는 이를 내부 도메인 모델로 정리한 뒤 매출, 주문 수, 품절 위험, 장기 미판매 상품, 주문 감소와 같은 지표를 분석합니다. 관리자는 일별 파이프라인을 실행하고, 실행 이력과 운영 지표를 Grafana에서 확인할 수 있습니다.

## 주요 기능

- 판매자, 상품, 주문, 주문 아이템 데이터 관리
- 주문 CSV 업로드 및 적재 작업 상태 추적
- 판매자별 일별 지표 집계
- 규칙 기반 인사이트 및 추천 액션 생성
- 판매자 대시보드 조회
- 전체 판매자 대상 일별 집계/인사이트 파이프라인 실행
- 파이프라인 실행 이력 및 판매자별 처리 결과 조회
- 동일 날짜 파이프라인 중복 실행 방지
- stale lock 복구 및 운영자 강제 lock 해제
- Prometheus/Grafana 기반 파이프라인 운영 지표 모니터링

## 서비스 흐름

```text
주문 데이터 입력
  -> 판매자/상품/주문/주문 아이템 정규화
  -> 일별 지표 집계
  -> 규칙 기반 인사이트 생성
  -> 추천 액션 생성
  -> 판매자 대시보드 제공
```

일별 파이프라인은 아래 흐름으로 실행됩니다.

```text
관리자 실행 또는 스케줄러 실행
  -> 파이프라인 실행 lock 획득
  -> 전체 판매자 조회
  -> 판매자별 일별 지표 집계
  -> 판매자별 인사이트/추천 생성
  -> 실행 이력 저장
  -> 운영 metric 발행
  -> Prometheus 수집
  -> Grafana 대시보드/알림 확인
```

## 기술 스택

- Java 17
- Spring Boot 3.5
- Spring Web, Validation, Security, Data JPA, Actuator
- PostgreSQL
- H2 Test DB
- Flyway
- Swagger/OpenAPI
- Micrometer, Prometheus, Grafana, Loki, Alloy
- Docker Compose
- JUnit 5, Spring Boot Test

## 로컬 실행

### 1. 환경 변수 파일 준비

```bash
cp .env.example .env
```

기본 DB 설정:

```text
DB_PORT=5432
DB_NAME=sellerinsight
DB_USERNAME=sellerinsight
DB_PASSWORD=sellerinsight
DB_HOST=postgres
```

로컬 기본 계정:

```text
Admin:  admin / admin-1234
Seller: seller-demo / seller-demo-1234
```

### 2. PostgreSQL 실행

```bash
docker compose up -d
```

### 3. Observability stack 실행

```bash
docker compose -f docker-compose.observability.yml up -d
```

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 5. 테스트 실행

```bash
./gradlew test
```

## 접속 경로

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health Check: `http://localhost:8080/api/v1/health`
- Actuator Health: `http://localhost:8080/actuator/health`
- Prometheus: `http://localhost:9090`
- Prometheus Targets: `http://localhost:9090/targets`
- Grafana: `http://localhost:3000`
- Grafana Dashboard: `http://localhost:3000/d/sellerinsight-daily-pipeline/sellerinsight-daily-pipeline`

Grafana 기본 계정:

```text
admin / admin
```

## Swagger 검증 플로우

Swagger UI에서 `Authorize` 버튼을 눌러 Basic Auth를 설정합니다.

Admin API:

```text
admin / admin-1234
```

Seller API:

```text
seller-demo / seller-demo-1234
```

### 1. 샘플 데이터 생성

```text
POST /api/v1/admin/sample-data/bootstrap?scenario=large
```

응답에서 아래 값을 확인합니다.

- `sellerId`
- `previousMetricDate`
- `targetMetricDate`

### 2. 이전 날짜 파이프라인 실행

```text
POST /api/v1/admin/pipelines/daily?date={previousMetricDate}
```

이전 날짜 지표를 먼저 생성하면, 다음 단계에서 전일 대비 인사이트를 함께 확인할 수 있습니다.

### 3. 대상 날짜 파이프라인 실행

```text
POST /api/v1/admin/pipelines/daily?date={targetMetricDate}
```

정상 실행 기준:

```text
status = SUCCESS
processedSellerCount = totalSellerCount
failedSellerCount = 0
generatedInsightCount > 0
```

### 4. 판매자 대시보드 조회

```text
GET /api/v1/sellers/{sellerId}/dashboard
```

확인 항목:

- 일별 매출/주문 지표
- 최근 인사이트
- 추천 액션

### 5. 인사이트 조회

```text
GET /api/v1/sellers/{sellerId}/insights?date={targetMetricDate}
```

### 6. 파이프라인 실행 이력 조회

```text
GET /api/v1/admin/pipelines/daily/runs?limit=5
GET /api/v1/admin/pipelines/daily/runs/{runId}
```

### 7. Grafana 대시보드 확인

파이프라인 실행 후 Grafana에서 아래 패널이 갱신되는지 확인합니다.

- `Pipeline Runs`
- `Successful Runs`
- `Failed Runs`
- `Processed Sellers`
- `Failed Sellers`
- `Generated Insights`
- `Average Duration`
- `Pipeline Runs by Status`

## CSV 업로드 플로우

판매자 주문 CSV 파일을 업로드해 주문 데이터를 적재할 수 있습니다.

```text
POST /api/v1/sellers/{sellerId}/import-jobs/orders/csv
Content-Type: multipart/form-data
file: CSV 파일
```

업로드 작업 조회:

```text
GET /api/v1/sellers/{sellerId}/import-jobs/{importJobId}
```

CSV 업로드 후 개별 집계와 인사이트 생성을 실행할 수 있습니다.

```text
POST /api/v1/sellers/{sellerId}/daily-metrics/aggregate?date={metricDate}
POST /api/v1/sellers/{sellerId}/insights/generate?date={metricDate}
```

전체 판매자 기준 운영 흐름에서는 관리자 일별 파이프라인 API를 사용합니다.

## 주요 API

| 구분 | Method | Path |
| --- | --- | --- |
| Health | `GET` | `/api/v1/health` |
| Seller | `POST` | `/api/v1/sellers` |
| Seller | `GET` | `/api/v1/sellers/{sellerId}` |
| CSV Import | `POST` | `/api/v1/sellers/{sellerId}/import-jobs/orders/csv` |
| CSV Import | `GET` | `/api/v1/sellers/{sellerId}/import-jobs/{importJobId}` |
| Daily Metric | `POST` | `/api/v1/sellers/{sellerId}/daily-metrics/aggregate` |
| Daily Metric | `GET` | `/api/v1/sellers/{sellerId}/daily-metrics/{metricDate}` |
| Insight | `POST` | `/api/v1/sellers/{sellerId}/insights/generate` |
| Insight | `GET` | `/api/v1/sellers/{sellerId}/insights` |
| Dashboard | `GET` | `/api/v1/sellers/{sellerId}/dashboard` |
| Sample Data | `POST` | `/api/v1/admin/sample-data/bootstrap` |
| Pipeline | `POST` | `/api/v1/admin/pipelines/daily` |
| Pipeline | `GET` | `/api/v1/admin/pipelines/daily/runs` |
| Pipeline | `GET` | `/api/v1/admin/pipelines/daily/runs/{runId}` |
| Pipeline Lock | `DELETE` | `/api/v1/admin/pipelines/daily/locks/{metricDate}` |

## 운영 지표

일별 파이프라인 실행 결과는 Micrometer metric으로 발행됩니다.

- `daily.pipeline.runs`
- `daily.pipeline.duration`
- `daily.pipeline.processed.sellers`
- `daily.pipeline.failed.sellers`
- `daily.pipeline.generated.insights`

Prometheus에서는 아래 이름으로 확인할 수 있습니다.

- `daily_pipeline_runs_total`
- `daily_pipeline_duration_seconds_count`
- `daily_pipeline_duration_seconds_sum`
- `daily_pipeline_duration_seconds_max`
- `daily_pipeline_processed_sellers_total`
- `daily_pipeline_failed_sellers_total`
- `daily_pipeline_generated_insights_total`

Grafana dashboard:

```text
SellerInsight Daily Pipeline
```

Alert rule:

- 파이프라인 실패 실행 감지
- 판매자 단위 부분 실패 감지
- 실행 시간 증가 감지

## 트러블슈팅

### Swagger에서 401 또는 403이 발생하는 경우

- Admin API는 `admin / admin-1234` 계정으로 인증합니다.
- Seller API는 `seller-demo / seller-demo-1234` 또는 Admin 계정으로 인증합니다.
- Swagger 우측 상단 `Authorize` 설정을 다시 확인합니다.

### Grafana 대시보드가 비어 있는 경우

- 애플리케이션이 `8080` 포트에서 실행 중인지 확인합니다.
- `http://localhost:8080/actuator/prometheus`가 열리는지 확인합니다.
- Prometheus targets에서 `sellerinsight-app` 상태가 `UP`인지 확인합니다.
- 일별 파이프라인을 한 번 이상 실행했는지 확인합니다.

### 파이프라인 lock 충돌이 발생하는 경우

동일 날짜 파이프라인이 이미 실행 중이거나 stale lock이 남아 있을 수 있습니다.

운영자 강제 해제 API:

```text
DELETE /api/v1/admin/pipelines/daily/locks/{metricDate}
```

### 로컬 DB를 초기화해야 하는 경우

```bash
docker compose down -v
docker compose up -d
```
