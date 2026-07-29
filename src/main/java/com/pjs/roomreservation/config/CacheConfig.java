package com.pjs.roomreservation.config;

import com.pjs.roomreservation.global.cache.CacheNames;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    public GenericJackson2JsonRedisSerializer redisCacheValueSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType("com.pjs.roomreservation.")
                        .allowIfSubType("java.time.")
                        .allowIfSubType("java.util.")
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            GenericJackson2JsonRedisSerializer redisCacheValueSerializer
    ) {
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ROOM_CACHE_TTL)
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(redisCacheValueSerializer));

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
