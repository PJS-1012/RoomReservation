package com.pjs.roomreservation.service;

import com.pjs.roomreservation.domain.Room;
import com.pjs.roomreservation.domain.User;
import com.pjs.roomreservation.repository.RoomRepository;
import com.pjs.roomreservation.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReservationConcurrencyTest {

    @Autowired ReservationService reservationService;
    @Autowired RoomRepository roomRepository;
    @Autowired UserRepository userRepository;
    @Autowired PlatformTransactionManager txManager;

    @Test
    void Concurrent() throws Exception {
        User user = new User("A", "x@test.com", "1234");
        userRepository.save(user);
        Long userId = user.getId();

        Room room = new Room("A-101", "Seoul", 10);
        roomRepository.save(room);
        Long roomId = room.getId();

        LocalDateTime startAt = LocalDateTime.of(2030, 1, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2030, 1, 1, 11, 0);

        ExecutorService es = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        List<Throwable> errors = new ArrayList<>();
        List<Long> successIds = new ArrayList<>();

        Runnable task = () -> {
            try {
                ready.countDown();
                start.await();

                TransactionTemplate tt = new TransactionTemplate(txManager);
                Long id = tt.execute(status ->
                        reservationService.create(userId, roomId, startAt, endAt)
                );

                synchronized (successIds) {
                    successIds.add(id);
                }
            } catch (Throwable t) {
                synchronized (errors) {
                    errors.add(t);
                }
            } finally {
                done.countDown();
            }
        };

        es.submit(task);
        es.submit(task);

        ready.await();
        start.countDown();
        done.await();
        es.shutdown();

        assertThat(successIds.size()).isEqualTo(1);
        assertThat(errors.size()).isEqualTo(1);
    }
}
