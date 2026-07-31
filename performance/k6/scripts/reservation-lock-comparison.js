import { check } from 'k6';
import http from 'k6/http';
import { Counter, Trend } from 'k6/metrics';
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

const reservationCreateDuration = new Trend('reservation_create_duration', true);
const reservationCreated = new Counter('reservation_created');
const databaseConflicts = new Counter('reservation_database_conflicts');
const lockConflicts = new Counter('reservation_lock_conflicts');
const unexpectedFailures = new Counter('reservation_unexpected_failures');

http.setResponseCallback(http.expectedStatuses(200, 201, 409));

export const options = {
  scenarios: {
    reservation_lock_comparison: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.RATE || 50),
      timeUnit: '1s',
      duration: __ENV.DURATION || '60s',
      preAllocatedVUs: Number(__ENV.PRE_ALLOCATED_VUS || 50),
      maxVUs: Number(__ENV.MAX_VUS || 300),
    },
  },
  thresholds: {
    reservation_create_duration: ['p(95)<2000'],
    reservation_unexpected_failures: ['count==0'],
  },
};

export function setup() {
  const runId = uniqueRunId('lock-comparison');
  const adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);
  const email = `${runId}@test.com`;

  registerUser(email, USER_PASSWORD, 'k6-lock-comparison-user');
  const userToken = login(email, USER_PASSWORD);
  const roomId = createRoom(adminToken, `k6-lock-comparison-room-${runId}`);
  const slot = futureSlot(0);

  return { userToken, roomId, slot };
}

export default function (data) {
  const response = createReservation(data.userToken, data.roomId, data.slot.startAt, data.slot.endAt);
  reservationCreateDuration.add(response.timings.duration);

  if (response.status === 201) {
    reservationCreated.add(1);
  } else if (response.status === 409) {
    recordConflict(response);
  } else {
    unexpectedFailures.add(1);
  }

  check(response, {
    'reservation request returns 201 or 409': (res) => res.status === 201 || res.status === 409,
  });
}

function recordConflict(response) {
  const errorCode = response.json('code');

  if (errorCode === 'Reservation_Lock_Conflict') {
    lockConflicts.add(1);
    return;
  }

  if (errorCode === 'Reservation_Conflict') {
    databaseConflicts.add(1);
    return;
  }

  unexpectedFailures.add(1);
}
