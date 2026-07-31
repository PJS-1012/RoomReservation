# 예약 조회 부하 테스트

## 테스트 조건

- 일반 사용자 200명
- 사용자당 예약 5건
- 특정 회의실의 누적 예약 1,000건
- 부하 단계: 100, 300, 500, 1,000 VU
- 각 VU는 요청 후 1초 대기
- 관리자 목록 페이지 크기: 50건

## 실행

먼저 IntelliJ에서 `RoomReservationApplication`을 실행한다.

관리자 회의실 예약 조회:

```powershell
k6 run `
  --summary-export performance/k6/results/after-optimization-admin-room-reservations-200-users-1000.json `
  performance/k6/scripts/stress-admin-room-reservations.js
```

일반 사용자 예약 조회:

```powershell
k6 run `
  --summary-export performance/k6/results/after-optimization-user-reservation-list-200-users-5-each.json `
  performance/k6/scripts/stress-reservation-list-1000.js
```

## Actuator 확인

테스트 중 다른 터미널에서 실행한다.

```powershell
Invoke-RestMethod http://localhost:8080/actuator/metrics/hikaricp.connections.active
Invoke-RestMethod http://localhost:8080/actuator/metrics/hikaricp.connections.pending
Invoke-RestMethod http://localhost:8080/actuator/metrics/hikaricp.connections.max
```

- `active`: 사용 중인 DB 커넥션
- `pending`: 커넥션을 기다리는 요청
- `max`: 커넥션 풀 최대 크기

## 관리자 API 결과

| VU | 개선 전 P95 | 개선 후 P95 |
| ---: | ---: | ---: |
| 100 | 56.13ms | 6.20ms |
| 300 | 467.98ms | 7.36ms |
| 500 | 2.10s | 7.79ms |
| 1,000 | 4.27s | 13.76ms |

| 지표 | 개선 전 | 개선 후 |
| --- | ---: | ---: |
| 전체 평균 | 1.31s | 6.54ms |
| 전체 P95 | 3.55s | 10.76ms |
| 처리량 | 169.87 req/s | 402.66 req/s |
| 실패율 | 0% | 0% |
| Hikari pending 최대 | 190 | 0 |
| 수신 데이터 | 24.11GB | 2.99GB |

적용한 변경:

- 예약 엔티티 전체 조회를 DTO Projection으로 변경
- 사용자와 회의실 정보를 JOIN해 한 번에 조회
- 서버 사이드 페이징 적용
- 한 요청의 응답을 최대 50건으로 제한

## 일반 사용자 API 결과

사용자 한 명의 예약이 5건뿐이라 개선 전에도 병목은 없었다.

| 지표 | 개선 전 | 개선 후 |
| --- | ---: | ---: |
| 1,000 VU P95 | 4.71ms | 4.87ms |
| 처리량 | 409.04 req/s | 408.11 req/s |
| Hikari pending 최대 | 0 | 0 |

이 결과는 페이징과 Projection이 항상 응답시간을 크게 줄이는 것은 아니라는 점도 보여준다. 조회 데이터가 적으면 페이지 count 쿼리 비용 때문에 차이가 거의 없을 수 있다.

## 결과 파일

`baseline-`으로 시작하는 파일은 개선 전 결과다.

`after-optimization-`으로 시작하는 파일은 개선 후 결과다.

## Redis Lock 비교

Redis Lock 비교는 Docker Compose로 app-1, app-2, Nginx, MySQL, Redis, Prometheus, Grafana가 실행 중인 상태에서 수행한다.

```powershell
k6 run `
  --summary-export performance/k6/results/redis-lock-off-50rps.json `
  performance/k6/scripts/reservation-lock-comparison.js
```

동일 조건에서 `.env`의 `RESERVATION_LOCK_ENABLED=true`로 변경한 뒤 app-1, app-2만 재생성하고 다시 실행한다.

```powershell
docker compose up -d --force-recreate app-1 app-2

k6 run `
  --summary-export performance/k6/results/redis-lock-on-50rps.json `
  performance/k6/scripts/reservation-lock-comparison.js
```

기본값은 50 req/s, 60초다. `RATE`, `DURATION`, `PRE_ALLOCATED_VUS`, `MAX_VUS` 환경 변수로 부하를 조절한다.

```powershell
$env:RATE=150
$env:DURATION='60s'
k6 run performance/k6/scripts/reservation-lock-comparison.js
```

비교 시 `reservation_create_duration`, `reservation_database_conflicts`, `reservation_lock_conflicts`, HikariCP pending, Redis Lock outcome을 함께 기록한다.

## App Rolling Restart

두 애플리케이션 인스턴스를 동시에 재생성하면 Nginx가 처리할 upstream이 없어 일시적으로 `502 Bad Gateway`가 발생할 수 있다. 앱 이미지를 교체할 때는 한 인스턴스가 `healthy`가 된 뒤 다음 인스턴스를 교체한다.

```powershell
docker compose up -d --force-recreate --no-deps app-1
docker compose ps app-1

# app-1이 healthy가 된 것을 확인한 후
docker compose up -d --force-recreate --no-deps app-2
docker compose ps app-2
```

Spring Boot graceful shutdown과 Docker `stop_grace_period`가 진행 중 요청의 종료 시간을 제공한다. Nginx는 단일 연결 실패만으로 upstream을 제외하지 않도록 `max_fails=3`으로 설정한다.
