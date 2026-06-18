# Reservation Query Baseline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore user and admin reservation queries to an unoptimized full-list implementation and record a reproducible performance baseline before optimization.

**Architecture:** Both endpoints return JSON arrays without paging. Repository methods load `Reservation` entities, and application code maps them to response DTOs while accessing LAZY `Room` and `User` associations. k6 seeds 200 users with five reservations each for the admin test and measures the same ramping VU stages while Actuator exposes HikariCP pressure.

**Tech Stack:** Java 17, Spring Boot, Spring Data JPA, MockMvc, MySQL, k6, Spring Boot Actuator

---

### Task 1: Define the baseline API contract

**Files:**
- Modify: `src/test/java/com/pjs/roomreservation/controller/ReservationControllerMockTest.java`
- Modify: `src/test/java/com/pjs/roomreservation/controller/AdminRoomReservationControllerMockTest.java`

- [ ] Change both list endpoint assertions from page objects to JSON arrays.
- [ ] Verify the focused tests fail against the paged implementation.

### Task 2: Restore full entity queries

**Files:**
- Modify: `src/main/java/com/pjs/roomreservation/controller/ReservationController.java`
- Modify: `src/main/java/com/pjs/roomreservation/controller/AdminRoomController.java`
- Modify: `src/main/java/com/pjs/roomreservation/service/ReservationService.java`
- Modify: `src/main/java/com/pjs/roomreservation/repository/ReservationRepository.java`

- [ ] Restore the user query to `findAllByUserIdOrderByStartAtDesc`.
- [ ] Add an admin room query returning all matching entities.
- [ ] Map entities to DTOs without paging or projection.
- [ ] Run the full test suite.

### Task 3: Create reproducible baseline load tests

**Files:**
- Modify: `performance/k6/scripts/stress-admin-room-reservations.js`
- Modify: `performance/k6/scripts/stress-reservation-list-1000.js`

- [ ] Seed 200 users with five reservations each for the admin scenario.
- [ ] Update response checks for array responses.
- [ ] Keep the `100 -> 300 -> 500 -> 1000 VU` stress stages.

### Task 4: Measure and document

**Files:**
- Create: `performance/k6/results/baseline-admin-room-reservations-200-users-1000.json`
- Create: `performance/k6/results/baseline-admin-room-reservations-200-users-1000.log`
- Create: `performance/k6/results/baseline-admin-room-reservations-200-users-1000-actuator.json`
- Modify: `performance/k6/results/benchmark-summary.md`
- Modify: `performance/k6/docs/README.md`

- [ ] Start the baseline server.
- [ ] Run k6 and sample HikariCP Actuator metrics.
- [ ] Record P95, average, maximum, throughput, failures, and pool pressure.
- [ ] Stop before applying projection or paging improvements.
