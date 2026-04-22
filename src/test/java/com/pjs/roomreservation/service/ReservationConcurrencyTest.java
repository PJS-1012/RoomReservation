package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.ReservationRepository;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.repository.UserRepository;
import com.pjs.roomreservation.service.exception.ReservationConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReservationConcurrencyTest {

    @Autowired ReservationService reservationService;
    @Autowired ReservationRepository reservationRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired UserRepository userRepository;
    @Autowired PlatformTransactionManager txManager;

    @Test
    void createReservationConcurrently_onlyOneSucceeds() throws Exception {
        User user = new User("A", "x@test.com", "1234");
        userRepository.save(user);
        Long userId = user.getId();

        Room room = new Room("A-101", "Seoul", 10);
        roomRepository.save(room);
        Long roomId = room.getId();

        LocalDateTime startAt = LocalDateTime.of(2030, 1, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2030, 1, 1, 11, 0);

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        List<Long> successIds = new CopyOnWriteArrayList<>();
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Runnable task = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                TransactionTemplate transactionTemplate = new TransactionTemplate(txManager);
                Long reservationId = transactionTemplate.execute(status ->
                        reservationService.create(userId, roomId, startAt, endAt)
                );

                successIds.add(reservationId);
            } catch (Throwable t) {
                errors.add(t);
            } finally {
                doneLatch.countDown();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(task);
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        assertThat(successIds).hasSize(1);
        assertThat(errors).hasSize(1);
        assertThat(reservationRepository.count()).isEqualTo(1);

        Throwable error = errors.get(0);
        assertThat(error).isInstanceOf(ReservationConflictException.class);
    }
}