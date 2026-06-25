# Room Reservation Backend

회의실 예약을 관리하는 Spring Boot 기반 백엔드 프로젝트입니다.

사용자는 회원가입과 로그인을 통해 JWT 토큰을 발급받고, 회의실 목록 조회, 예약 생성, 내 예약 조회, 예약 취소를 할 수 있습니다. 관리자는 회의실을 등록/수정/비활성화하고, 특정 회의실의 예약 현황을 조회할 수 있습니다.

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5.9 |
| Database | MySQL, H2 Test DB |
| ORM | Spring Data JPA |
| Security | Spring Security, JWT |
| Docs | Springdoc OpenAPI / Swagger UI |
| Test | JUnit 5, Spring Boot Test |
| Monitoring | Spring Boot Actuator |
| Performance | k6 |

## 주요 기능

- 회원가입, 로그인, 비밀번호 변경, 회원 비활성화
- JWT 기반 인증과 관리자 권한 분리
- 회의실 목록/상세 조회
- 관리자 회의실 등록, 수정, 비활성화
- 회의실 예약 생성, 내 예약 목록 조회, 예약 취소
- 같은 회의실의 시간대 중복 예약 방지
- 페이지네이션 기반 예약 목록 조회
- 공통 예외 응답 처리
- Swagger UI와 Actuator를 통한 API 확인 및 상태 점검

## 프로젝트 구조

```text
src/main/java/com/pjs/roomreservation
├── config        # Security, OpenAPI, 초기 관리자 설정
├── controller    # REST API 엔드포인트
├── domain        # User, Room, Reservation 엔티티
├── dto           # 요청/응답 DTO
├── global        # 전역 예외 처리
├── repository    # Spring Data JPA Repository
├── security      # JWT 필터, 토큰 provider, 인증 principal
└── service       # 회원, 회의실, 예약 비즈니스 로직
```

## 핵심 구현

### JWT 인증

로그인 성공 시 Access Token을 발급합니다. 인증이 필요한 API는 다음 형식의 헤더를 사용합니다.

```http
Authorization: Bearer {accessToken}
```

Spring Security는 세션을 사용하지 않는 stateless 구조로 설정되어 있으며, `/auth/login`, `POST /users`, Swagger, health check를 제외한 API는 인증이 필요합니다.

### 예약 중복 방지

예약 생성 시 같은 회의실의 기존 예약과 시간이 겹치는지 확인합니다.

```text
기존 예약 시작 < 요청 종료
기존 예약 종료 > 요청 시작
```

또한 회의실 조회에 비관적 락(`PESSIMISTIC_WRITE`)을 적용해 동시에 같은 회의실에 예약 요청이 들어오는 상황을 제어합니다.

### 예약 정책

- 시작 시간은 종료 시간보다 빨라야 합니다.
- 과거 시간으로는 예약할 수 없습니다.
- 예약 시간은 최소 30분, 최대 4시간입니다.
- 예약 취소는 본인의 예약만 가능합니다.

### 관리자 계정 초기화

애플리케이션 시작 시 설정된 관리자 이메일이 없으면 초기 관리자 계정을 생성합니다.

```properties
app.bootstrap-admin.email=admin@room.com
app.bootstrap-admin.password=1234
app.bootstrap-admin.name=super
```

## 실행 방법

### 1. MySQL 데이터베이스 생성

```sql
CREATE DATABASE roomr;
```

### 2. 환경 변수 설정

`jwt.secret`은 Base64로 인코딩된 256bit 이상 키를 사용해야 합니다.

PowerShell 예시:

```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="1234"
$env:JWT_SECRET="{base64-encoded-secret}"
$env:JWT_EXPIRATION_SECONDS="3600"
```

### 3. 애플리케이션 실행

```powershell
.\gradlew bootRun
```

기본 서버 주소는 `http://localhost:8080`입니다.

## 테스트

```powershell
.\gradlew test
```

테스트 환경은 H2 인메모리 데이터베이스를 사용합니다.

## API 요약

### 인증 / 사용자

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| POST | `/users` | 회원가입 | X |
| POST | `/auth/login` | 로그인 및 JWT 발급 | X |
| GET | `/users/me` | 내 정보 조회 | O |
| PATCH | `/users/password` | 비밀번호 변경 | O |
| DELETE | `/users` | 회원 비활성화 | O |

### 회의실 / 예약

| Method | URL | 설명 | 인증 |
| --- | --- | --- | --- |
| GET | `/rooms` | 활성 회의실 목록 조회 | O |
| GET | `/rooms/{roomId}` | 회의실 상세 조회 | O |
| POST | `/reservations` | 예약 생성 | O |
| GET | `/reservations?page=0&size=50` | 내 예약 목록 조회 | O |
| DELETE | `/reservations/{reservationId}` | 예약 취소 | O |

### 관리자

| Method | URL | 설명 | 권한 |
| --- | --- | --- | --- |
| GET | `/admin/ping` | 관리자 권한 확인 | ADMIN |
| POST | `/admin` | 회의실 생성 | ADMIN |
| PUT | `/admin/{roomId}` | 회의실 수정 | ADMIN |
| DELETE | `/admin/{roomId}` | 회의실 비활성화 | ADMIN |
| GET | `/admin/rooms/{roomId}/reservations?page=0&size=50` | 회의실별 예약 조회 | ADMIN |

## 요청 예시

### 회원가입

```http
POST /users
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "1234",
  "name": "홍길동"
}
```

### 로그인

```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "1234"
}
```

응답:

```json
{
  "accessToken": "{jwt-token}"
}
```

### 예약 생성

```http
POST /reservations
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "roomId": 1,
  "startAt": "2026-03-01T14:30:00",
  "endAt": "2026-03-01T15:30:00"
}
```

## Swagger / 상태 확인

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health Check: `http://localhost:8080/health`
- Actuator Health: `http://localhost:8080/actuator/health`
- Actuator Metrics: `http://localhost:8080/actuator/metrics`

## 성능 테스트

k6 스크립트는 `performance/k6/scripts`에 있습니다.

```powershell
k6 run performance/k6/scripts/stress-reservation-list-1000.js
k6 run performance/k6/scripts/stress-admin-room-reservations.js
```

예약 목록 조회는 DTO Projection과 페이지네이션을 적용해 필요한 데이터만 조회하도록 개선했습니다. 관련 결과는 `performance/k6/results`에서 확인할 수 있습니다.
