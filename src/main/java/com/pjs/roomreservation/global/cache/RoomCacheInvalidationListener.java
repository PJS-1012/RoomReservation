package com.pjs.roomreservation.global.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RoomCacheInvalidationListener {

    @CacheEvict(cacheNames = {CacheNames.ROOMS, CacheNames.AVAILABLE_ROOMS}, allEntries = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evictRoomCaches(RoomChangedEvent event) {
    }
}
