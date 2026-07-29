package com.pjs.roomreservation.global.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RoomCacheEvictListener {

    @CacheEvict(cacheNames = CacheNames.ROOMS, allEntries = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evictRooms(RoomCacheEvictEvent event) {
    }
}
