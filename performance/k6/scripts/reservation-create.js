import { check, sleep } from 'k6';
import exec from 'k6/execution';
import {
  ADMIN_EMAIL,
  ADMIN_PASSWORD,
  USER_PASSWORD,
  createReservation,
  createRoom,
  futureSlot,
  login,
  registerUser,
  uniqueRunId,
} from '../lib/helpers.js';

export const options = {
  scenarios: {
    reservation_create: {
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
  const runId = uniqueRunId('create');
  const adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  const email = `${runId}@test.com`;
  const roomCount = Number(__ENV.ROOM_COUNT || 5);
  const roomIds = [];

  registerUser(email, USER_PASSWORD, 'k6-create-user');
  const userToken = login(email, USER_PASSWORD);

  for (let i = 0; i < roomCount; i += 1) {
    roomIds.push(createRoom(adminToken, `k6-create-room-${runId}-${i}`));
  }

  return { userToken, roomIds };
}

export default function (data) {
  const iteration = exec.scenario.iterationInTest;
  const roomId = data.roomIds[iteration % data.roomIds.length];
  const slotIndex = Math.floor(iteration / data.roomIds.length);
  const slot = futureSlot(slotIndex);
  const res = createReservation(data.userToken, roomId, slot.startAt, slot.endAt);

  check(res, {
    'reservation create returns 201': (r) => r.status === 201,
  });

  sleep(1);
}
