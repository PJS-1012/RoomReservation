package com.pjs.roomreservation.config;

import com.pjs.roomreservation.global.cache.CacheNames;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {
    private static final Duration ROOM_CACHE_TTL = Duration.ofMinutes(10);
    private static final Duration AVAILABLE_ROOM_CACHE_TTL = Duration.ofMinutes(1);

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ROOM_CACHE_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        RedisCacheConfiguration availableRoomConfiguration = defaultConfiguration
                .entryTtl(AVAILABLE_ROOM_CACHE_TTL);

        return builder -> builder
                .cacheDefaults(defaultConfiguration)
                .withInitialCacheConfigurations(Map.of(
                        CacheNames.AVAILABLE_ROOMS,
                        availableRoomConfiguration
                ));
    }
}
