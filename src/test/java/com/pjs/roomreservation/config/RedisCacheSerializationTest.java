package com.pjs.roomreservation.config;

import com.pjs.roomreservation.dto.room.RoomResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RedisCacheSerializationTest {

    private final GenericJackson2JsonRedisSerializer serializer = new CacheConfig().redisCacheValueSerializer();

    @Test
    void serializeAndDeserialize_roomResponseList() {
        List<RoomResponseDto> rooms = new ArrayList<>();
        rooms.add(new RoomResponseDto(1L, "Room A", "3F", 4, true, LocalDateTime.of(2030, 2, 20, 10, 0)));

        Object deserialized = serializer.deserialize(serializer.serialize(rooms));

        assertThat(deserialized).isInstanceOf(List.class);
        assertThat((List<?>) deserialized).hasSize(1);
        assertThat(((List<?>) deserialized).get(0)).isInstanceOf(RoomResponseDto.class);
    }
}
