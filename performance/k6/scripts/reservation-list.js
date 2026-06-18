import http from 'k6/http';
import { check, sleep } from 'k6';
import {
  ADMIN_EMAIL,
  ADMIN_PASSWORD,
  BASE_URL,
  USER_PASSWORD,
  createReservation,
  createRoom,
  futureSlot,
  jsonHeaders,
  login,
  registerUser,
  uniqueRunId,
} from '../lib/helpers.js';

export const options = {
  scenarios: {
    reservation_list: {
      executor: 'ramping-vus',
      stages: [
        { duration: __ENV.RAMP_UP_LOW_DURATION || '1m', target: Number(__ENV.LOW_VUS || 10) },
        { duration: __ENV.LOW_HOLD_DURATION || '3m', target: Number(__ENV.LOW_VUS || 10) },
        { duration: __ENV.RAMP_UP_HIGH_DURATION || '1m', target: Number(__ENV.HIGH_VUS || 30) },
        { duration: __ENV.HIGH_HOLD_DURATION || '3m', target: Number(__ENV.HIGH_VUS || 30) },
        { duration: __ENV.RAMP_DOWN_DURATION || '1m', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export function setup() {
  const runId = uniqueRunId('list');
  const adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  const email = `${runId}@test.com`;

  registerUser(email, USER_PASSWORD, 'k6-list-user');
  const userToken = login(email, USER_PASSWORD);
  const roomId = createRoom(adminToken, `k6-list-room-${runId}`);

  const seedReservations = Number(__ENV.SEED_RESERVATIONS || 200);
  for (let i = 0; i < seedReservations; i += 1) {
    const slot = futureSlot(i);
    const res = createReservation(userToken, roomId, slot.startAt, slot.endAt);

    check(res, {
      'seed reservation returns 201': (r) => r.status === 201,
    });
  }

  return { userToken, seedReservations };
}

export default function (data) {
  const pageSize = Number(__ENV.PAGE_SIZE || 50);
  const res = http.get(`${BASE_URL}/reservations?page=0&size=${pageSize}`, jsonHeaders(data.userToken));

  check(res, {
    'reservation list returns 200': (r) => r.status === 200,
    'reservation page has content': (r) => {
      const body = r.json();
      return Array.isArray(body.content) && body.content.length > 0 && body.content.length <= pageSize;
    },
    'reservation page reports total elements': (r) => {
      const body = r.json();
      return body.totalElements >= data.seedReservations;
    },
  });

  sleep(1);
}
