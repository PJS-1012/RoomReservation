# RoomReservation Backend

회의실 예약 서비스 백엔드 프로젝트입니다.  
JWT 인증 기반으로 사용자별 예약 관리 기능을 구현했으며,  
동시성 제어를 통해 동일 시간대 중복 예약 문제를 해결했습니다.

---

## 기술 스택

- Java 17
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT
- MySQL

---

## 주요 기능

- 회원가입 / 로그인 (JWT 인증)
- 회의실 목록 및 상세 조회
- 회의실 예약 생성
- 내 예약 목록 조회
- 예약 취소
- 관리자 회의실 관리

---

## 핵심 구현

### 1. JWT 기반 인증

로그인 시 Access Token 발급 후,  
모든 인증 API 요청에서 Header 기반 인증 처리


Authorization: Bearer {accessToken}


-> Stateless 구조로 서버 확장성 확보

---

### 2. 예약 시간 중복 검증


기존 시작 < 요청 종료
기존 종료 > 요청 시작


-> 시간 겹침 판단 후 예약 차단

---

### 3. 동시성 제어 (비관적 락)


@Lock(PESSIMISTIC_WRITE)


- 동일 회의실 예약 요청 동시 발생 시
- 트랜잭션 단위로 순차 처리

-> 중복 예약 완전 방지

---

## API 예시


- POST /auth/login
- GET /rooms
- POST /reservations
- GET /reservations
- DELETE /reservations/{id}


---

## 한 줄 요약

JWT 인증 + 동시성 제어를 통해  
실제 서비스에서 발생할 수 있는 예약 데이터 정합성 문제를 해결한 백엔드 프로젝트
