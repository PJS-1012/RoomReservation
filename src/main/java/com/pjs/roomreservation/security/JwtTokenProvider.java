package com.pjs.roomreservation.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.sql.Date;
import java.time.Instant;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationSec;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-seconds}") long expirationSec ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationSec = expirationSec;
    }

    public String createAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expirationSec);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
