# k6 예약 조회 성능 테스트

## 현재 단계

현재 코드는 성능 개선 전 기준선이다.

- 사용자 예약 조회: 전체 엔티티 조회
- 관리자 회의실 예약 조회: 전체 엔티티 조회
- 페이징 없음
- DTO Projection 없음
- Fetch Join 없음

성능 문제를 측정하고 기록한 다음, 별도 커밋에서 최적화할 예정이다.

## 준비

1. MySQL을 실행한다.
2. IntelliJ에서 `RoomReservationApplication`을 실행한다.
3. `http://localhost:8080/actuator/health`가 `UP`인지 확인한다.
4. PowerShell에서 프로젝트의 `RoomReservation` 폴더로 이동한다.

IntelliJ 실행 구성에는 유효한 Base64 형식의 `JWT_SECRET` 환경변수가 필요하다.

## 관리자 기준선 실행

```powershell
k6 run `
  --summary-export performance/k6/results/baseline-admin-room-reservations-200-users-1000.json `
  performance/k6/scripts/stress-admin-room-reservations.js
```

기본 데이터는 다음과 같다.

```text
USER_COUNT=200
RESERVATIONS_PER_USER=5
총 예약 수=1,000
```

관리자 API는 특정 회의실의 예약 1,000건 전체를 조회한다.

## 사용자 기준선 실행

```powershell
k6 run `
  --summary-export performance/k6/results/baseline-user-reservation-list-200-users-5-each.json `
  performance/k6/scripts/stress-reservation-list-1000.js
```

각 VU는 200명의 사용자 토큰을 순환 사용하며 각 사용자는 자신의 예약 5건을 조회한다.

## VU 단계

두 테스트 모두 다음 순서로 부하를 증가시킨다.

```text
30초 동안 100 VU 도달 -> 1분 유지
30초 동안 300 VU 도달 -> 1분 유지
30초 동안 500 VU 도달 -> 1분 유지
30초 동안 1000 VU 도달 -> 1분 유지
30초 동안 0 VU로 감소
```

## Actuator 수동 확인

테스트 실행 중 다른 PowerShell 창에서 확인한다.

```powershell
Invoke-RestMethod http://localhost:8080/actuator/metrics/hikaricp.connections.active
Invoke-RestMethod http://localhost:8080/actuator/metrics/hikaricp.connections.pending
Invoke-RestMethod http://localhost:8080/actuator/metrics/hikaricp.connections.max
```

- `active`: 현재 사용 중인 DB 커넥션 수
- `pending`: 커넥션을 얻지 못해 기다리는 요청 수
- `max`: 커넥션 풀 최대 크기

`active`가 `max`에 도달하고 `pending`이 계속 증가하면 DB 커넥션 병목을 의심할 수 있다.

## k6 결과 읽기

- `http_req_duration avg`: 평균 응답시간
- `http_req_duration p(95)`: 요청의 95%가 완료된 시간
- `http_req_failed`: HTTP 요청 실패율
- `http_reqs`: 전체 요청 수와 초당 처리량
- `data_received`: 서버에서 받은 전체 데이터

평균값보다 P95를 우선 확인한다. 평균이 낮아도 P95가 높으면 일부 사용자는 매우 느린 응답을 경험한다.

## 실측 결과

관리자 API는 1000 VU에서 P95 4.27초, 처리량 169.87 req/s, Hikari pending 최대 190을 기록했다.

일반 사용자 API는 1000 VU에서 P95 4.71ms, 처리량 409.04 req/s, Hikari pending 0을 기록했다.

상세 결과와 해석은 `performance/k6/results/benchmark-summary.md`를 확인한다.
