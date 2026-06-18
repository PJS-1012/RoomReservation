import { check } from 'k6';
import http from 'k6/http';
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

http.setResponseCallback(http.expectedStatuses({ min: 200, max: 399 }, 409));

export const options = {
  scenarios: {
    reservation_conflict: {
      executor: 'shared-iterations',
      vus: Number(__ENV.VUS || 20),
      iterations: Number(__ENV.ITERATIONS || 20),
      maxDuration: __ENV.MAX_DURATION || '30s',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<2000'],
  },
};

export function setup() {
  const runId = uniqueRunId('conflict');
  const adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  const email = `${runId}@test.com`;

  registerUser(email, USER_PASSWORD, 'k6-conflict-user');
  const userToken = login(email, USER_PASSWORD);
  const roomId = createRoom(adminToken, `k6-conflict-room-${runId}`);
  const slot = futureSlot(0);

  return { userToken, roomId, slot };
}

export default function (data) {
  const res = createReservation(data.userToken, data.roomId, data.slot.startAt, data.slot.endAt);

  check(res, {
    'reservation create either succeeds or conflicts': (r) => r.status === 201 || r.status === 409,
  });
}
