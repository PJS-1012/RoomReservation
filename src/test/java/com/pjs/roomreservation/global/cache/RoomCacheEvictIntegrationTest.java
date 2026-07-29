package com.pjs.roomreservation.global.cache;

import com.pjs.roomreservation.service.RoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RoomCacheEvictIntegrationTest {

    @Autowired
    private RoomService roomService;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void clearCache() {
        cacheManager.getCache(CacheNames.ROOMS).clear();
    }

    @Test
    void create_evictsRoomListCache_afterTransactionCommit() {
        Cache cache = cacheManager.getCache(CacheNames.ROOMS);
        cache.put("active", "stale-value");

        roomService.create("Cache Eviction Room", "3F", 4);

        assertThat(cache.get("active")).isNull();
    }
}
