import http from 'k6/http';
import { check, fail } from 'k6';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@room.com';
export const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || '1234';
export const USER_PASSWORD = __ENV.USER_PASSWORD || '1234';

export function jsonHeaders(token) {
  const headers = {
    'Content-Type': 'application/json',
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return { headers };
}

export function login(email, password) {
  const res = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email, password }),
    jsonHeaders()
  );

  check(res, {
    'login returns 200': (r) => r.status === 200,
    'login returns accessToken': (r) => Boolean(r.json('accessToken')),
  });

  if (res.status !== 200) {
    fail(`Login failed for ${email}. status=${res.status}, body=${res.body}`);
  }

  return res.json('accessToken');
}

export function registerUser(email, password = USER_PASSWORD, name = 'k6-user') {
  const res = http.post(
    `${BASE_URL}/users`,
    JSON.stringify({ email, password, name }),
    jsonHeaders()
  );

  check(res, {
    'register returns 201 or already exists': (r) => r.status === 201 || r.status === 409,
  });

  if (res.status !== 201 && res.status !== 409) {
    fail(`Register failed for ${email}. status=${res.status}, body=${res.body}`);
  }
}

export function createRoom(adminToken, name, location = 'k6-floor', capacity = 8) {
  const res = http.post(
    `${BASE_URL}/admin`,
    JSON.stringify({ name, location, capacity }),
    jsonHeaders(adminToken)
  );

  check(res, {
    'create room returns 201 or duplicate': (r) => r.status === 201 || r.status === 409,
  });

  if (res.status === 201) {
    return Number(res.body);
  }

  fail(`Room creation needs a unique room name. status=${res.status}, body=${res.body}`);
}

export function createReservation(token, roomId, startAt, endAt) {
  return http.post(
    `${BASE_URL}/reservations`,
    JSON.stringify({ roomId, startAt, endAt }),
    jsonHeaders(token)
  );
}

export function futureSlot(slotIndex, durationMinutes = 30) {
  const base = new Date(Date.UTC(2030, 0, 1, 0, 0, 0));
  const start = new Date(base.getTime() + slotIndex * 60 * 60 * 1000);
  const end = new Date(start.getTime() + durationMinutes * 60 * 1000);

  return {
    startAt: toLocalDateTime(start),
    endAt: toLocalDateTime(end),
  };
}

export function uniqueRunId(prefix) {
  return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}`;
}

function toLocalDateTime(date) {
  return date.toISOString().slice(0, 19);
}
