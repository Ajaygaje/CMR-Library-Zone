
package com.cmrlibrary.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Base64-encoded 32-byte development key
    @Value("${app.jwt.secret:Q01STGxpYnJhcnlab25lRGV2U2VjcmV0S2V5MjAyNjEyMzQ1Njc4OTA=}")
    private String secretKey;

    // Token validity: 24 hours
    private static final long TOKEN_EXPIRATION =
            24 * 60 * 60 * 1000;

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT token
    public String generateToken(String email, String role) {

        Date currentDate = new Date();

        Date expirationDate =
                new Date(currentDate.getTime() + TOKEN_EXPIRATION);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Extract email
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Extract role
    public String extractRole(String token) {
        return extractAllClaims(token)
                .get("role", String.class);
    }

    // Read token claims
    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Validate token
    public boolean isTokenValid(String token, String email) {

        try {
            String tokenEmail = extractEmail(token);

            Date expiration =
                    extractAllClaims(token).getExpiration();

            return tokenEmail.equals(email)
                    && expiration.after(new Date());

        } catch (Exception exception) {
            return false;
        }
    }
}
