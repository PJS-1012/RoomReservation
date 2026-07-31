# Room Reservation Backend

회의실 예약의 정합성과 운영 관측성을 함께 다룬 Spring Boot 백엔드입니다.

동일 시간대 예약 경합에서는 MySQL 비관적 락을 최종 정합성 수단으로 사용하고, Redis Cache Aside와 선택적 Redis 분산 락을 적용했습니다. Docker Compose로 다중 애플리케이션 인스턴스, Nginx, MySQL, Redis, Prometheus, Grafana, Loki를 함께 실행할 수 있습니다.

## Architecture

```text
Client
  -> Nginx (round-robin)
  -> app-1 / app-2 (Spring Boot, JWT)
       -> MySQL (source of truth, Flyway, PESSIMISTIC_WRITE)
       -> Redis (Cache Aside, optional Redisson lock)
       -> Actuator -> Prometheus -> Grafana
       -> JSON logs -> Alloy -> Loki -> Grafana
```

## Key Decisions

### Reservation consistency

- 예약 생성 시 회의실 행을 `PESSIMISTIC_WRITE`로 잠그고, 잠금 안에서 중복 시간을 검증합니다.
- 중복 조건은 `existing.startAt < request.endAt AND existing.endAt > request.startAt`입니다.
- Redis 분산 락은 `RESERVATION_LOCK_ENABLED=true`일 때만 DB 락 앞단의 빠른 경합 차단기로 동작합니다.
- Redis 락 실패 또는 Redis 장애 시에도 DB 비관적 락 경로가 최종 정합성을 보장합니다.

### Cache strategy

- `rooms`: 활성 회의실 목록을 Cache Aside로 저장합니다.
- `availableRooms`: 시작/종료 시각과 수용 인원을 포함한 키로 예약 가능 회의실 조회 결과를 저장합니다.
- 회의실 생성·수정·비활성화 또는 예약 생성·취소가 커밋된 뒤 도메인 이벤트로 관련 캐시 전체를 evict합니다.
- TTL은 캐시 장애나 evict 누락에 대비하는 최후의 안전장치이며, 최신성은 명시적 evict로 보장합니다.

### Database and query performance

- Flyway가 스키마 변경을 버전 SQL로 관리하고 Hibernate는 `validate`만 수행합니다.
- 예약 충돌 조회: `(room_id, canceled, start_at, end_at)`
- 사용자 예약 목록: `(user_id, start_at)`
- 관리자 회의실 예약 목록: `(room_id, created_at, id)`
- MySQL `EXPLAIN`으로 충돌 조회의 `range` 스캔과 목록 조회의 `Backward index scan`을 확인했습니다.

## Tech Stack

| Area | Technology |
| --- | --- |
| Language / Framework | Java 17, Spring Boot 3.5.9 |
| Persistence | Spring Data JPA, MySQL 8.4, Flyway |
| Security | Spring Security, JWT, BCrypt |
| Cache / Lock | Redis, Spring Cache, Redisson |
| Infra | Docker Compose, Nginx |
| Monitoring | Actuator, Micrometer, Prometheus, Grafana, Redis Exporter |
| Logging | Logstash JSON, Grafana Alloy, Loki |
| Test / Load test | JUnit 5, MockMvc, CountDownLatch, k6 |

## Run with Docker Compose

1. 환경 파일을 준비합니다.

```powershell
Copy-Item .env.example .env
```

`.env`의 MySQL 비밀번호, JWT Secret, Grafana 비밀번호를 실제 값으로 교체합니다. JWT Secret은 Base64 인코딩된 256비트 이상 키를 사용합니다.

최초 관리자 계정이 필요하면 `BOOTSTRAP_ADMIN_EMAIL`, `BOOTSTRAP_ADMIN_PASSWORD`, `BOOTSTRAP_ADMIN_NAME`도 설정합니다. 세 값 중 하나라도 비어 있으면 초기 관리자 생성을 건너뜁니다.

2. 전체 환경을 기동합니다.

```powershell
docker compose up -d --build
docker compose ps
```

3. 접속합니다.

| Service | URL |
| --- | --- |
| API / Nginx | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |
| Loki API | `http://localhost:3100` |

애플리케이션을 교체할 때는 두 인스턴스를 동시에 내리지 않고 순차 재기동합니다.

```powershell
docker compose up -d --force-recreate --no-deps app-1
docker compose ps app-1
docker compose up -d --force-recreate --no-deps app-2
docker compose ps app-2
```

## API Summary

| Method | URL | Description | Auth |
| --- | --- | --- | --- |
| POST | `/users` | 회원가입 | Public |
| POST | `/auth/login` | 로그인 및 JWT 발급 | Public |
| GET | `/rooms` | 활성 회의실 목록 | User |
| GET | `/rooms/available?startAt=...&endAt=...&capacity=...` | 조건 기반 예약 가능 회의실 조회 | User |
| POST | `/reservations` | 회의실 예약 | User |
| GET | `/reservations` | 내 예약 목록 | User |
| DELETE | `/reservations/{reservationId}` | 내 예약 취소 | User |
| POST / PUT / DELETE | `/admin/**` | 회의실 관리 | Admin |
| GET | `/admin/rooms/{roomId}/reservations` | 회의실별 예약 조회 | Admin |

상세 요청과 응답 스키마는 Swagger UI에서 확인합니다.

## Monitoring and Logs

- `/actuator/health`, `/actuator/prometheus`는 외부 헬스체크와 Prometheus scrape을 위해 공개합니다.
- `/actuator/metrics`는 JWT 인증이 필요합니다.
- Grafana dashboard는 예약 생성 처리량, p95/p99 지연 시간, HikariCP 활성/대기 연결, JVM Heap, Redis 메모리, Redis 락 결과를 보여줍니다.
- Grafana Explore에서 Loki를 선택한 뒤 아래 LogQL을 사용할 수 있습니다.

```logql
{service=~"app-1|app-2"} |= "reservation_created"
{service=~"app-1|app-2", level="ERROR"}
```

요청마다 `X-Trace-Id`가 없으면 서버가 생성하고 응답 헤더와 JSON 로그 MDC에 남깁니다. `traceId`는 Loki label이 아닌 JSON 필드로 보관해 label cardinality 폭증을 방지합니다.

## Tests

```powershell
.\gradlew.bat clean build
```

- 서비스 정책 및 예외 테스트
- MockMvc 기반 인증·권한·API 테스트
- Cache evict 통합 테스트
- Redis 락 활성/비활성·장애 fallback 테스트
- `CountDownLatch` 동시 예약 테스트

GitHub Actions CI와 CD 빌드는 모두 테스트를 포함한 `clean build`를 수행합니다.

## k6 Lock Comparison

```powershell
k6 run performance/k6/scripts/reservation-lock-comparison.js
```

동일 회의실·동일 시간대 경합에서 DB 락 단독과 Redis 락 + DB 락 하이브리드를 비교합니다. 50/150/300 RPS, 각 3회 결과는 `performance/k6/results`에 보관합니다.

- 50 RPS에서는 Redis 락의 이점이 거의 없고 평균 p95가 소폭 증가했습니다.
- 300 RPS에서는 DB 충돌 일부를 Redis에서 빠르게 거절했지만, Redis 왕복 비용 때문에 p95는 오히려 증가했습니다.
- 따라서 기본값은 DB 락 단독이며, Redis 락은 다중 인스턴스·높은 경합이 확인된 경우에만 선택적으로 활성화합니다.

## Operational Notes

- `spring.jpa.hibernate.ddl-auto=validate`: 운영 스키마 변경은 `src/main/resources/db/migration`의 다음 Flyway 버전 SQL로만 추가합니다.
- 기존 DB 전환을 위해 현재 환경은 Flyway baseline version `1`을 기록했습니다. 신규 DB에서는 `V1__create_initial_schema.sql`이 실행됩니다.
- Flyway 11.7.2는 MySQL 8.4에 대해 지원 버전 경고를 남길 수 있습니다. 현재 마이그레이션과 Hibernate 검증은 통과했으며, 운영 전에는 Spring Boot BOM과 Flyway의 호환 버전을 함께 검토합니다.
- Redis 락과 캐시는 MySQL의 정합성 보장을 대체하지 않습니다. MySQL은 항상 source of truth입니다.
