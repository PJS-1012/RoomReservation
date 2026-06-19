import exec from 'k6/execution';
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
    admin_room_reservations_stress: {
      executor: 'ramping-vus',
      stages: [
        { duration: __ENV.RAMP_DURATION || '30s', target: 100 },
        { duration: __ENV.HOLD_DURATION || '1m', target: 100 },
        { duration: __ENV.RAMP_DURATION || '30s', target: 300 },
        { duration: __ENV.HOLD_DURATION || '1m', target: 300 },
        { duration: __ENV.RAMP_DURATION || '30s', target: 500 },
        { duration: __ENV.HOLD_DURATION || '1m', target: 500 },
        { duration: __ENV.RAMP_DURATION || '30s', target: 1000 },
        { duration: __ENV.HOLD_DURATION || '1m', target: 1000 },
        { duration: __ENV.RAMP_DOWN_DURATION || '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<5000'],
    'http_req_duration{load_stage:stage_100}': ['p(95)<5000'],
    'http_req_duration{load_stage:stage_300}': ['p(95)<5000'],
    'http_req_duration{load_stage:stage_500}': ['p(95)<5000'],
    'http_req_duration{load_stage:stage_1000}': ['p(95)<5000'],
  },
};

export function setup() {
  const runId = uniqueRunId('admin-room-reservations');
  const adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  const roomId = createRoom(adminToken, `k6-admin-room-${runId}`);
  const userCount = Number(__ENV.USER_COUNT || 200);
  const reservationsPerUser = Number(__ENV.RESERVATIONS_PER_USER || 5);
  const seedReservations = Number(
    __ENV.SEED_RESERVATIONS || userCount * reservationsPerUser
  );
  const userTokens = [];

  for (let i = 0; i < userCount; i += 1) {
    const email = `${runId}-user-${i}@test.com`;
    registerUser(email, USER_PASSWORD, `k6-user-${i}`);
    userTokens.push(login(email, USER_PASSWORD));
  }

  for (let i = 0; i < seedReservations; i += 1) {
    const slot = futureSlot(i);
    const userIndex = Math.floor(i / reservationsPerUser) % userTokens.length;
    const token = userTokens[userIndex];
    const res = createReservation(token, roomId, slot.startAt, slot.endAt);

    check(res, {
      'seed reservation returns 201': (r) => r.status === 201,
    });
  }

  return {
    adminToken,
    roomId,
    seedReservations,
    pageSize: Number(__ENV.PAGE_SIZE || 50),
  };
}

export default function (data) {
  const params = jsonHeaders(data.adminToken);
  params.tags = { load_stage: currentLoadStage() };

  const res = http.get(
    `${BASE_URL}/admin/rooms/${data.roomId}/reservations?page=0&size=${data.pageSize}`,
    params
  );

  check(res, {
    'admin room reservations returns 200': (r) => r.status === 200,
    'admin room reservations returns one page': (r) => {
      if (r.status !== 200) {
        return false;
      }

      const body = r.json();
      return Array.isArray(body.content) && body.content.length === data.pageSize;
    },
    'admin room reservations reports total elements': (r) => {
      if (r.status !== 200) {
        return false;
      }

      return r.json().totalElements === data.seedReservations;
    },
    'admin room reservations returns target room only': (r) => {
      if (r.status !== 200) {
        return false;
      }

      const body = r.json();
      return body.content.every((reservation) => reservation.roomId === data.roomId);
    },
  });

  sleep(1);
}

function currentLoadStage() {
  const vus = exec.instance.vusActive;

  if (vus <= 100) {
    return 'stage_100';
  }

  if (vus <= 300) {
    return 'stage_300';
  }

  if (vus <= 500) {
    return 'stage_500';
  }

  return 'stage_1000';
}
