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
    reservation_list_stress: {
      executor: 'ramping-vus',
      stages: [
        { duration: __ENV.RAMP_DURATION || '30s', target: 10 },
        { duration: __ENV.HOLD_DURATION || '1m', target: 10 },
        { duration: __ENV.RAMP_DURATION || '30s', target: 30 },
        { duration: __ENV.HOLD_DURATION || '1m', target: 30 },
        { duration: __ENV.RAMP_DURATION || '30s', target: 50 },
        { duration: __ENV.HOLD_DURATION || '1m', target: 50 },
        { duration: __ENV.RAMP_DURATION || '30s', target: 100 },
        { duration: __ENV.HOLD_DURATION || '1m', target: 100 },
        { duration: __ENV.RAMP_DOWN_DURATION || '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
    'http_req_duration{load_stage:stage_010}': ['p(95)<1000'],
    'http_req_duration{load_stage:stage_030}': ['p(95)<1000'],
    'http_req_duration{load_stage:stage_050}': ['p(95)<1000'],
    'http_req_duration{load_stage:stage_100}': ['p(95)<1000'],
    'http_req_failed{load_stage:stage_010}': ['rate<0.01'],
    'http_req_failed{load_stage:stage_030}': ['rate<0.01'],
    'http_req_failed{load_stage:stage_050}': ['rate<0.01'],
    'http_req_failed{load_stage:stage_100}': ['rate<0.01'],
  },
};

export function setup() {
  const runId = uniqueRunId('stress-list');
  const adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  const email = `${runId}@test.com`;

  registerUser(email, USER_PASSWORD, 'k6-stress-list-user');
  const userToken = login(email, USER_PASSWORD);
  const roomId = createRoom(adminToken, `k6-stress-list-room-${runId}`);

  const seedReservations = Number(__ENV.SEED_RESERVATIONS || 1000);
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
  const params = jsonHeaders(data.userToken);
  params.tags = { load_stage: currentLoadStage() };
  const pageSize = Number(__ENV.PAGE_SIZE || 50);

  const res = http.get(`${BASE_URL}/reservations?page=0&size=${pageSize}`, params);

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

function currentLoadStage() {
  const vus = exec.instance.vusActive;

  if (vus <= 10) {
    return 'stage_010';
  }

  if (vus <= 30) {
    return 'stage_030';
  }

  if (vus <= 50) {
    return 'stage_050';
  }

  return 'stage_100';
}
