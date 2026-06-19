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
    reservation_list_stress_1000: {
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
    http_req_duration: ['p(95)<5000'],
    'http_req_duration{load_stage:stage_100}': ['p(95)<5000'],
    'http_req_duration{load_stage:stage_300}': ['p(95)<5000'],
    'http_req_duration{load_stage:stage_500}': ['p(95)<5000'],
    'http_req_duration{load_stage:stage_1000}': ['p(95)<5000'],
    http_req_failed: ['rate<0.10'],
    'http_req_failed{load_stage:stage_100}': ['rate<0.10'],
    'http_req_failed{load_stage:stage_300}': ['rate<0.10'],
    'http_req_failed{load_stage:stage_500}': ['rate<0.10'],
    'http_req_failed{load_stage:stage_1000}': ['rate<0.10'],
  },
};

export function setup() {
  const runId = uniqueRunId('stress-list-1000');
  const adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  const roomId = createRoom(adminToken, `k6-stress-list-1000-room-${runId}`);
  const userCount = Number(__ENV.USER_COUNT || 200);
  const reservationsPerUser = Number(__ENV.RESERVATIONS_PER_USER || 5);
  const userTokens = [];

  for (let userIndex = 0; userIndex < userCount; userIndex += 1) {
    const email = `${runId}-user-${userIndex}@test.com`;
    registerUser(email, USER_PASSWORD, `k6-list-user-${userIndex}`);
    const userToken = login(email, USER_PASSWORD);
    userTokens.push(userToken);

    for (let reservationIndex = 0; reservationIndex < reservationsPerUser; reservationIndex += 1) {
      const slotIndex = userIndex * reservationsPerUser + reservationIndex;
      const slot = futureSlot(slotIndex);
      const res = createReservation(userToken, roomId, slot.startAt, slot.endAt);

      check(res, {
        'seed reservation returns 201': (r) => r.status === 201,
      });
    }
  }

  return { userTokens, reservationsPerUser };
}

export default function (data) {
  const userIndex = (exec.vu.idInTest - 1) % data.userTokens.length;
  const params = jsonHeaders(data.userTokens[userIndex]);
  params.tags = { load_stage: currentLoadStage() };

  const res = http.get(`${BASE_URL}/reservations?page=0&size=50`, params);

  check(res, {
    'reservation list returns 200': (r) => r.status === 200,
    'reservation list returns the current users reservations': (r) => {
      if (r.status !== 200) {
        return false;
      }

      const body = r.json();
      return Array.isArray(body.content)
        && body.content.length === data.reservationsPerUser
        && body.totalElements === data.reservationsPerUser;
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
