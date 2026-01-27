package com.pjs.roomreservation.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Encoders;

import javax.crypto.SecretKey;

public class JwtKeyGeneration {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Key = Encoders.BASE64.encode(key.getEncoded());
        System.out.println("값 : " + base64Key);
    }
}
