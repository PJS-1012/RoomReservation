package com.pjs.roomreservation.global.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("availableRoomKeyGenerator")
public class AvailableRoomKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        LocalDateTime startAt = (LocalDateTime) params[0];
        LocalDateTime endAt = (LocalDateTime) params[1];
        Integer capacity = (Integer) params[2];

        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(startAt)
                + "|"
                + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(endAt)
                + "|"
                + (capacity == null ? "all" : capacity);
    }
}
